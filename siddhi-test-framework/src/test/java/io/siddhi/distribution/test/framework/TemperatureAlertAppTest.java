/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.distribution.test.framework;

import com.google.common.io.Resources;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.distribution.test.framework.util.DatabaseClient;
import io.siddhi.distribution.test.framework.util.NatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TemperatureAlertAppTest {
    //todo add info on the testsuite, jars, and resources, copy jars from maven , add as class comment
    private static final Logger log = LoggerFactory.getLogger(TemperatureAlertAppTest.class);

    private MySQLContainer mySQLContainer;
    private NatsContainer natsContainer;
    private SiddhiRunnerContainer siddhiRunnerContainer;

    private NatsClient natsClient;
    private WaitingConsumer siddhiLogConsumer = new WaitingConsumer();


    private static final String DATABSE_NAME = "EnergyDB";
    private static final String DATABSE_HOST = "mysqldb";
    private static final String NATS_CLUSTER_ID = "EnergyCluster";
    private static final String NATS_CLUSTER_HOST = "nats-streaming";
    private static final String NATS_INPUT_DESTINATION = "Energy-Alert-App_DevicePowerStream";
    private static final String NATS_OUTPUT_DESTINATION = "Energy-Alert-App_AlertStream";

    @BeforeClass
    public void setUpCluster() throws IOException, InterruptedException {
        URL appUrl = Resources.getResource("TemperatureAlertApp/app");
        URL extraJarsUrl = Resources.getResource("TemperatureAlertApp/jars");
        URL configUrl = Resources.getResource("TemperatureAlertApp/config/TemperatureDB_Datasource.yaml");
        Network network = Network.newNetwork();

        mySQLContainer = new MySQLContainer()
                .withDatabaseName(DATABSE_NAME)
                .withNetworkAliases(DATABSE_HOST)
                .withNetwork(network);
        mySQLContainer.start();

        natsContainer = new NatsContainer()
                .withNetwork(network)
                .withClusterId(NATS_CLUSTER_ID)
                .withNetworkAliases(NATS_CLUSTER_HOST);
        natsContainer.start();
        natsClient = new NatsClient(NATS_CLUSTER_ID, natsContainer.getBootstrapServerUrl());
        natsClient.connect();

        Map<String, String> envMap = new HashMap<>();
        envMap.put("CLUSTER_ID", NATS_CLUSTER_ID);
        envMap.put("INPUT_DESTINATION", NATS_INPUT_DESTINATION);
        envMap.put("OUTPUT_DESTINATION", NATS_OUTPUT_DESTINATION);
        envMap.put("NATS_URL", natsContainer.getNetworkedBootstrapServerUrl());
        envMap.put("DATABASE_URL", mySQLContainer.getNetworkedJdbcUrl());
        envMap.put("USERNAME", mySQLContainer.getUsername());
        envMap.put("PASSWORD", mySQLContainer.getPassword());
        envMap.put("JDBC_DRIVER_NAME", mySQLContainer.getDriverClassName());

        siddhiRunnerContainer = new SiddhiRunnerContainer()
                .withSiddhiApps(appUrl.getPath())
                .withJars(extraJarsUrl.getPath())
                .withConfig(configUrl.getPath())
                .withNetwork(network)
                .withEnv(envMap)
                .withLogConsumer(new Slf4jLogConsumer(log));
        siddhiRunnerContainer.start();
        siddhiRunnerContainer.followOutput(siddhiLogConsumer, OutputFrame.OutputType.STDOUT);
    }

    @AfterClass
    public void shutdownCluster() {
        if (natsContainer != null) {
            natsContainer.stop();
        }
        if (mySQLContainer != null) {
            mySQLContainer.stop();
        }
        if (siddhiRunnerContainer != null) {
            siddhiRunnerContainer.stop();
        }
    }

    @Test
    public void testMessageConsumption() throws InterruptedException, IOException, TimeoutException {
        natsClient.publish("Energy-Alert-App_DevicePowerStream", "{\n" +
                "    \"event\": {\n" +
                "        \"type\": \"dummyType\",\n" +
                "        \"deviceID\": \"dummyDeviceID\",\n" +
                "        \"temp\": 50.0,\n" +
                "        \"roomID\": \"dummyRoomID\"\n" +
                "    }\n" +
                "}");
        try {
            siddhiLogConsumer.waitUntil(frame ->
                            frame.getUtf8String().contains("data=[dummyType, dummyDeviceID, 50.0, dummyRoomID]"),
                    5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Assert.fail("Message consumption acknowledgement is not available in Siddhi Runner logs.");
        }
    }

    @Test
    public void testDBPersistence() throws SQLException, InterruptedException, IOException, TimeoutException,
            ConnectionUnavailableException {
        natsClient.publish("Energy-Alert-App_DevicePowerStream", "{\n" +
                "    \"event\": {\n" +
                "        \"type\": \"internal\",\n" +
                "        \"deviceID\": \"C250i\",\n" +
                "        \"temp\": 30.5,\n" +
                "        \"roomID\": \"F2-Conference\"\n" +
                "    }\n" +
                "}");
        ResultSet resultSet = null;
        try {
            resultSet = DatabaseClient.executeQuery(mySQLContainer,"SELECT * FROM InternalDevicesTempTable");
            Assert.assertNotNull(resultSet);
            Assert.assertEquals("C250i",resultSet.getString(2));
            Assert.assertEquals(30,resultSet.getInt(3));
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    @Test
    public void testAppOutput() throws SQLException, InterruptedException, IOException, TimeoutException {
        NatsClient.ResultHolder resultHolder = new NatsClient.ResultHolder(1, 3);
        NatsClient natsClient = new NatsClient(NATS_CLUSTER_ID, "stan_test1",
                natsContainer.getBootstrapServerUrl(), resultHolder);
        natsClient.connect();
        natsClient.subscribeFromNow(NATS_OUTPUT_DESTINATION);
        natsClient.publish("Energy-Alert-App_DevicePowerStream", "{\n" +
                "    \"event\": {\n" +
                "        \"type\": \"monitored\",\n" +
                "        \"deviceID\": \"C001\",\n" +
                "        \"temp\": 40.2,\n" +
                "        \"roomID\": \"F2-Conference\"\n" +
                "    }\n" +
                "}");

        natsClient.publish("Energy-Alert-App_DevicePowerStream", "{\n" +
                "    \"event\": {\n" +
                "        \"type\": \"monitored\",\n" +
                "        \"deviceID\": \"C001\",\n" +
                "        \"temp\": 60.0,\n" +
                "        \"roomID\": \"F2-Conference\"\n" +
                "    }\n" +
                "}");
        natsClient.publish("Energy-Alert-App_DevicePowerStream", "{\n" +
                "    \"event\": {\n" +
                "        \"type\": \"monitored\",\n" +
                "        \"deviceID\": \"C001\",\n" +
                "        \"temp\": 80.0,\n" +
                "        \"roomID\": \"F2-Conference\"\n" +
                "    }\n" +
                "}");
        natsClient.publish("Energy-Alert-App_DevicePowerStream", "{\n" +
                "    \"event\": {\n" +
                "        \"type\": \"monitored\",\n" +
                "        \"deviceID\": \"C001\",\n" +
                "        \"temp\": 30.0,\n" +
                "        \"roomID\": \"F2-Conference\"\n" +
                "    }\n" +
                "}");
        Assert.assertTrue(((ArrayList<String>)resultHolder.waitAndGetResults()).get(0).contains("\"peakTemp\":80.0"));
    }
}
