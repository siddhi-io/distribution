/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.siddhi.parser;


import io.siddhi.core.SiddhiManager;
import io.siddhi.extension.io.http.source.HttpSource;
import io.siddhi.extension.io.nats.source.NATSSource;
import io.siddhi.extension.map.json.sourcemapper.JsonSourceMapper;
import io.siddhi.parser.core.SiddhiAppCreator;
import io.siddhi.parser.core.appcreator.DeployableSiddhiQueryGroup;
import io.siddhi.parser.core.appcreator.NatsSiddhiAppCreator;
import io.siddhi.parser.core.topology.SiddhiTopology;
import io.siddhi.parser.core.topology.SiddhiTopologyCreatorImpl;
import io.siddhi.parser.service.model.MessagingConfig;
import io.siddhi.parser.service.model.MessagingSystem;
import org.apache.log4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Create the distributed partial siddhi applications which communicates using nats broker.
 */
public class NatsAppCreatorTestCase {

    private static final Logger log = Logger.getLogger(NatsAppCreatorTestCase.class);
    private static final String CLUSTER_ID = "test-cluster";
    private static String NATS_SERVER_URL = "nats://localhost:4222";
    private int port;
    private MessagingSystem messagingSystem = new MessagingSystem();

    @BeforeClass
    private void initializeDockerContainer() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("http-source", HttpSource.class);
        siddhiManager.setExtension("map-json", JsonSourceMapper.class);
        siddhiManager.setExtension("nats-source", NATSSource.class);
        SiddhiParserDataHolder.setSiddhiManager(siddhiManager);
        GenericContainer simpleWebServer
                = new GenericContainer("nats-streaming:0.11.2");
        simpleWebServer.setPrivilegedMode(true);
        simpleWebServer.start();
        port = simpleWebServer.getMappedPort(4222);
        NATS_SERVER_URL += port;
        messagingSystem.setConfig(new MessagingConfig(CLUSTER_ID,new String[]{NATS_SERVER_URL}));
        messagingSystem.setType("nats");
        Thread.sleep(500);
    }

    /** Test the topology creation for a particular siddhi app includes nats transport.
     */
    @Test
    public void testSiddhiTopologyCreator() {
        String siddhiApp = "@App:name('Energy-Alert-App')\n"
                + "@App:description('Energy consumption and anomaly detection')\n"
                + "@source(type = 'http', topic = 'device-power', @map(type = 'json'))\n"
                + "@source(type='nats',cluster.id='test-cluster',destination = 'dummy-destination', " +
                "bootstrap.servers='nats://localhost:8080',@map(type='json')) \n"
                + "define stream DevicePowerStream (type string, deviceID string, power int,"
                + " roomID string);\n"
                + "@sink(type = 'log')\n"
                + "define stream AlertStream (deviceID string, roomID string, initialPower double, "
                + "finalPower double,autorityContactEmail string);\n"
                + "@info(name = 'monitered-filter')\n"
                + "from DevicePowerStream[type == 'monitored']\n"
                + "select deviceID, power, roomID\n"
                + "insert current events into MonitoredDevicesPowerStream;\n"
                + "@info(name = 'power-increase-pattern')\n"
                + "partition with (deviceID of MonitoredDevicesPowerStream)\n"
                + "begin\n"
                + "@info(name = 'avg-calculator')\n"
                + "from MonitoredDevicesPowerStream#window.time(2 min)\n"
                + "select deviceID, avg(power) as avgPower, roomID\n"
                + "insert current events into #AvgPowerStream;\n"
                + "@info(name = 'power-increase-detector')\n"
                + "from every e1 = #AvgPowerStream -> e2 = #AvgPowerStream[(e1.avgPower + 5) "
                + "<= avgPower] within 10 min\n"
                + "select e1.deviceID as deviceID, e1.avgPower as initialPower, "
                + "e2.avgPower as finalPower, e1.roomID\n"
                + "insert current events into RisingPowerStream;\n"
                + "end;\n"
                + "@info(name = 'power-range-filter')\n"
                + "from RisingPowerStream[finalPower > 100]\n"
                + "select deviceID, roomID, initialPower, finalPower, "
                + "'no-reply@powermanagement.com' as autorityContactEmail\n"
                + "insert current events into AlertStream;\n"
                + "@info(name = 'internal-filter')\n"
                + "from DevicePowerStream[type == 'internal']\n"
                + "select deviceID, power\n"
                + "insert current events into InternaltDevicesPowerStream;\n";
        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology, messagingSystem);

        Assert.assertEquals(queryGroupList.size(), 2);
    }

    /** Test partial app creation for a user given app with an in-memory sink.
     */
    @Test
    public void testInmemoryBridgedAppParsing1() {
        String siddhiApp = "@App:name('InmemoryTransportTestApp') " +
                "@Source(type = 'http', receiver.url = 'http://0.0.0.0:8080/stockStream', " +
                "basic.auth.enabled = 'false', @map(type = 'json'))\n"
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@sink(type='inMemory', topic='takingOverTopic', @map(type='passThrough'))\n"
                + "Define stream takingOverStream(symbol string, overtakingSymbol string, avgPrice double);\n"
                + "@info(name = 'query1')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.lengthBatch(2)\n"
                + "Select symbol, tier as overtakingSymbol,avg(price) as avgPrice  \n"
                + "Insert into takingOverStream;\n"
                + "end;\n"
                + "@info(name ='query3')\n"
                + "from filteredStockStream [price >250 and price <350]\n"
                + "Select \"XYZ\" as symbol, tier as overtakingSymbol ,avg(price) as avgPrice  \n"
                + "Insert into takingOverStream;\n";
        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology, messagingSystem);

        Assert.assertEquals(queryGroupList.size(), 2);
        Assert.assertTrue(queryGroupList.get(0).isReceiverQueryGroup());
        String processApp = queryGroupList.get(1).getSiddhiQueries().get(0).getApp();
        Assert.assertFalse(processApp.contains("@sink(type='inMemory', topic='takingOverTopic', " +
                "@map(type='passThrough'))"));
        Assert.assertTrue(processApp.contains("@sink(type='nats',cluster.id='test-cluster'," +
                "destination = 'takingOverTopic'"));
    }

    /** Test partial app creation for a user given app with in-memory sink & source.
     */
    @Test
    public void testInmemoryBridgedAppParsing2() {
        String siddhiApp = "@App:name('InmemoryTransportTestApp') " +
                "@source(type='inMemory', topic='stockStream', @map(type='passThrough'))\n"
                + "Define stream stockStream(symbol string, price float, quantity int, tier string);\n"
                + "@sink(type='inMemory', topic='takingOverTopic', @map(type='passThrough'))\n"
                + "Define stream takingOverStream(symbol string, overtakingSymbol string, avgPrice double);\n"
                + "@info(name = 'query1')\n"
                + "From stockStream[price > 100]\n"
                + "Select *\n"
                + "Insert into filteredStockStream;\n"
                + "@info(name = 'query2')"
                + "Partition with (symbol of filteredStockStream)\n"
                + "begin\n"
                + "From filteredStockStream#window.lengthBatch(2)\n"
                + "Select symbol, tier as overtakingSymbol,avg(price) as avgPrice  \n"
                + "Insert into takingOverStream;\n"
                + "end;\n"
                + "@info(name ='query3')\n"
                + "from filteredStockStream [price >250 and price <350]\n"
                + "Select \"XYZ\" as symbol, tier as overtakingSymbol ,avg(price) as avgPrice  \n"
                + "Insert into takingOverStream;\n";
        SiddhiTopologyCreatorImpl siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(siddhiApp);
        SiddhiAppCreator appCreator = new NatsSiddhiAppCreator();
        List<DeployableSiddhiQueryGroup> queryGroupList = appCreator.createApps(topology, messagingSystem);

        Assert.assertEquals(queryGroupList.size(), 1);
        String app = queryGroupList.get(0).getSiddhiQueries().get(0).getApp();
        Assert.assertFalse(app.contains("@source(type='inMemory', topic='stockStream', @map(type='passThrough'))"));
        Assert.assertFalse(app.contains("@sink(type='inMemory', topic='takingOverTopic', " +
                "@map(type='passThrough'))"));
        Assert.assertTrue(app.contains("@source(type='nats',cluster.id='test-cluster'," +
                "destination = 'stockStream'"));
        Assert.assertTrue(app.contains("@sink(type='nats',cluster.id='test-cluster'," +
                "destination = 'takingOverTopic'"));
    }
}
