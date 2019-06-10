package io.siddhi.distribution.test.framework;/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.collect.ImmutableMap;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.distribution.test.framework.util.HTTPRequestSender;
import io.siddhi.distribution.test.framework.util.HTTPResponseMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.StrictAssertions;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.output.WaitingConsumer;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.http.sink.HttpSink;
import org.wso2.extension.siddhi.map.json.sinkmapper.JsonSinkMapper;

import java.net.URI;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

/**
 *
 */
public class SiddhiTestFramworkTests {

    private static final Logger log = LoggerFactory.getLogger(SiddhiTestFramworkTests.class);
    private static String SIDDHI_RUNNER_VERSION = "0.1.0";

    //not binding to localhost because tests fail on many environments where binded ports are
    // already taken and will disallow running it in parallel on CI systems.
//    @ClassRule
//    private GenericContainer simpleWebServer
//            = new GenericContainer("siddhiio/siddhi-runner-alpine:0.1.0")
//            .withExposedPorts(8006)
//            .withFileSystemBind("src/test/resources/siddhi-files/http",
//                    "/home/siddhi_user/siddhi-runner-0.1.0/wso2/runner/deployment/siddhi-files",
//                    BindMode.READ_ONLY)
//            .withLogConsumer(new Slf4jLogConsumer(log))
//            ;

    private GenericContainer createSiddhiRunnerConatiner(int[] portsToExport, String deploymentDirPath) {
        GenericContainer simpleWebServer
                = new GenericContainer("siddhiio/siddhi-runner-alpine:0.1.0");
        for (int port : portsToExport) {
            simpleWebServer.withExposedPorts(port);
        }
        simpleWebServer.withFileSystemBind(deploymentDirPath,
                "/home/siddhi_user/siddhi-runner-0.1.0/wso2/runner/deployment/siddhi-files",
                BindMode.READ_ONLY)
                .withLogConsumer(new Slf4jLogConsumer(log))
        ;
        return simpleWebServer;
    }

    private GenericContainer createKafkaContainer(int zkPort, int kafkaPort) {
        GenericContainer zkContainer
                = new GenericContainer("wurstmeister/zookeeper")
                .withExposedPorts(zkPort).withLogConsumer(new Slf4jLogConsumer(log));
        zkContainer.withPrivilegedMode(true);
        zkContainer.start();
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int zkPortBinded = zkContainer.getMappedPort(zkPort);
        String zkIP = zkContainer.getContainerIpAddress();
        GenericContainer kafkaContainer
                = new GenericContainer("wurstmeister/kafka")
                .withExposedPorts(kafkaPort).withLogConsumer(new Slf4jLogConsumer(log));
        kafkaContainer.addEnv("KAFKA_ADVERTISED_HOST_NAME", zkIP);
        kafkaContainer.addEnv("KAFKA_ZOOKEEPER_CONNECT", "localhost:" + zkPortBinded);
        kafkaContainer.withPrivilegedMode(true);
        kafkaContainer.start();
        return kafkaContainer;
    }


    @Test
    public void receiveAndCountTest() throws TimeoutException, InterruptedException {

        GenericContainer siddhiContainer =
                createSiddhiRunnerConatiner(new int[]{8006}, "src/test/resources/siddhi-files/http");
        siddhiContainer.start();
        WaitingConsumer consumer = new WaitingConsumer();
        siddhiContainer.followOutput(consumer, OutputFrame.OutputType.STDOUT);
        URI _baseURI = URI.create(String.format("http://%s:%d", "localhost",
                siddhiContainer.getMappedPort(8006)));
        HTTPResponseMessage httpResponseMessage = sendHRequest("{\n" +
                        "    \"event\": {\n" +
                        "        \"name\": \"data2\",\n" +
                        "        \"amount\": 20.44345\n" +
                        "    }\n" +
                        "}", _baseURI, "/productionStream", "application/json",
                "POST", false, "admin", "admin");
        try {
            consumer.waitUntil(frame ->
                    frame.getUtf8String().contains("data=[1]"), 5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Assert.fail();
        }
        siddhiContainer.stop();
    }

    @Test
    public void receiveAndCountTestFromSiddhi() throws TimeoutException, InterruptedException {

        GenericContainer siddhiContainer =
                createSiddhiRunnerConatiner(new int[]{8006}, "src/test/resources/siddhi-files/http");
        siddhiContainer.start();
        int bindedPort = siddhiContainer.getMappedPort(8006);
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setExtension("sink-http", HttpSink.class);
        siddhiManager.setExtension("sink-mapper-json", JsonSinkMapper.class);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                "@App:name(\"ReceiveAndCount\")\n" +
                        "\n" +
                        "@App:description('Receive events via HTTP transport and view the output on the console')\n" +
                        "\n" +
                        "@sink(type='http',publisher.url='http://localhost:" +
                        bindedPort +
                        "/productionStream', method='POST', " +
                        "@map(type='json'))\n" +
                        "define stream SweetProductionStream (name string, amount double);");
        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("SweetProductionStream");
        inputHandler.send(new Object[]{"daat2", 20.44345});
        WaitingConsumer consumer = new WaitingConsumer();
        siddhiContainer.followOutput(consumer, OutputFrame.OutputType.STDOUT);
        try {
            consumer.waitUntil(frame ->
                    frame.getUtf8String().contains("data=[1]"), 5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error(e.getLocalizedMessage());
            Assert.fail();
        }
        siddhiContainer.stop();

    }

    @Test
    public void kafkaTest() throws TimeoutException, InterruptedException {

        GenericContainer siddhiContainer =
                createSiddhiRunnerConatiner(new int[]{8006}, "src/test/resources/siddhi-files/kafka");
        GenericContainer kafka = createKafkaContainer(2181, 9092);
        int kafkaBindedPort = kafka.getMappedPort(9092);
        siddhiContainer.start();
        int bindedPort = siddhiContainer.getMappedPort(8006);
        URI _baseURI = URI.create(String.format("http://%s:%d", "localhost",
                bindedPort));
        HTTPResponseMessage httpResponseMessage = sendHRequest("{\n" +
                        "    \"event\": {\n" +
                        "        \"name\": \"data2\",\n" +
                        "        \"amount\": 20.44345\n" +
                        "    }\n" +
                        "}", _baseURI, "/productionStream", "application/json",
                "POST", false, "admin", "admin");
        WaitingConsumer consumer = new WaitingConsumer();
        siddhiContainer.followOutput(consumer, OutputFrame.OutputType.STDOUT);
        try {
            consumer.waitUntil(frame ->
                    frame.getUtf8String().contains("data=[1]"), 5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error(e.getLocalizedMessage());
            Assert.fail();
        }

    }


    private HTTPResponseMessage sendHRequest(String body, URI baseURI, String path, String contentType,
                                             String methodType, Boolean auth, String userName, String password) {

        HTTPRequestSender HTTPRequestSender = new HTTPRequestSender(baseURI, path, auth, false, methodType,
                contentType, userName, password);
        HTTPRequestSender.addBodyContent(body);
        return HTTPRequestSender.getResponse();
    }
}
