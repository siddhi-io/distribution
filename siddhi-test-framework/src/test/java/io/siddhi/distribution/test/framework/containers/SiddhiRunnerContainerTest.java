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
package io.siddhi.distribution.test.framework.containers;

import com.google.common.io.Resources;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.distribution.test.framework.MySQLContainer;
import io.siddhi.distribution.test.framework.SiddhiRunnerContainer;
import io.siddhi.distribution.test.framework.util.DatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Siddhi Runner test class.
 */
public class SiddhiRunnerContainerTest {

    private static final Logger log = LoggerFactory.getLogger(SiddhiRunnerContainerTest.class);

    @Test
    public void testSiddhiRunnerStartup() {
        SiddhiRunnerContainer siddhiRunnerContainer = new SiddhiRunnerContainer()
                .withLogConsumer(new Slf4jLogConsumer(log));
        siddhiRunnerContainer.start();
        WaitingConsumer consumer = new WaitingConsumer();
        siddhiRunnerContainer.followOutput(consumer, OutputFrame.OutputType.STDOUT);
        try {
            consumer.waitUntil(frame ->
                            frame.getUtf8String().contains("Siddhi Runner Distribution started"),
                    5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Assert.fail("Siddhi Runner failed to start.");
        } finally {
            siddhiRunnerContainer.stop();
        }
    }

    @Test
    public void testSiddhiAppDeployment() {
        URL appUrl = Resources.getResource("AppDeploymentTestResource");
        int listenerPortInApp = 8006;
        SiddhiRunnerContainer siddhiRunnerContainer = new SiddhiRunnerContainer()
                .withSiddhiApps(appUrl.getPath())
                .withExposedPorts(new int[]{listenerPortInApp})
                .withLogConsumer(new Slf4jLogConsumer(log));
        siddhiRunnerContainer.start();
        WaitingConsumer consumer = new WaitingConsumer();
        siddhiRunnerContainer.followOutput(consumer, OutputFrame.OutputType.STDOUT);
        try {
            consumer.waitUntil(frame ->
                            frame.getUtf8String().contains("Siddhi App ReceiveAndCount deployed successfully"),
                    5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Assert.fail("Siddhi App AppDeploymentTestResource deployment failed.");
        } finally {
            siddhiRunnerContainer.stop();
        }
    }

    @Test
    public void testAddingAdditionalJars() throws SQLException, ConnectionUnavailableException, MalformedURLException {
        //Siddhi Runner needs MySQL client jar to communicate with the database.
        // Test fails if the added client jar is not accessible from the Siddhi Runner
        URL siddhiAppUrl = Resources.getResource("ExtendingLibTestResource/app");
        Path extraJarsPath = Paths.get("target", "ExtendingLibTestResource/jars");
        Network network = Network.newNetwork();

        MySQLContainer mySQLContainer = new MySQLContainer()
                .withDatabaseName("testDatabase")
                .withNetwork(network)
                .withNetworkAliases("mysqldb");
        mySQLContainer.start();

        SiddhiRunnerContainer siddhiContainer = new SiddhiRunnerContainer()
                .withSiddhiApps(siddhiAppUrl.getPath())
                .withJars(extraJarsPath.toString(), false)
                .withLogConsumer(new Slf4jLogConsumer(log))
                .withNetwork(network)
                .withEnv("DATABASE_URL", mySQLContainer.getNetworkedJdbcUrl())
                .withEnv("USERNAME", "test")
                .withEnv("PASSWORD", "test")
                .withEnv("JDBC_DRIVER_NAME", "com.mysql.jdbc.Driver");
        siddhiContainer.start();

        ResultSet resultSet = null;
        try {
            resultSet = DatabaseClient.executeQuery(mySQLContainer, "SELECT * FROM CUSTOMER_INFO_TABLE");
            String secondColumnValue = resultSet.getString(2);
            Assert.assertTrue(secondColumnValue.equals("dummyCustomer"));
        } finally {
            siddhiContainer.stop();
            mySQLContainer.stop();
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    @Test
    public void testOverridingSiddhiRunnerConfig() {
        //Tests the use-case described in
        // https://siddhi.io/en/v5.0/deployment/siddhi-as-a-local-microservice/#running-with-runner-config
        URL siddhiAppUrl = Resources.getResource("ConfigOverrideTestResource/app");
        URL configUrl = Resources.getResource("ConfigOverrideTestResource/config/TestDb.yaml");
        SiddhiRunnerContainer siddhiRunnerContainer = new SiddhiRunnerContainer()
                .withConfig(configUrl.getPath())
                .withSiddhiApps(siddhiAppUrl.getPath())
                .withLogConsumer(new Slf4jLogConsumer(log));
        siddhiRunnerContainer.start();
        WaitingConsumer consumer = new WaitingConsumer();
        siddhiRunnerContainer.followOutput(consumer, OutputFrame.OutputType.STDOUT);
        try {
            consumer.waitUntil(frame ->
                            frame.getUtf8String().contains("Siddhi App ConsumeAndStore deployed successfully"),
                    5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Assert.fail("Siddhi-Runner configuration update failed");
        } finally {
            siddhiRunnerContainer.stop();
        }
    }
}
