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

import io.siddhi.distribution.test.framework.NatsContainer;
import io.siddhi.distribution.test.framework.util.NatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Nats Container test class.
 */
public class NatsContainerTest {
    private static final Logger log = LoggerFactory.getLogger(NatsContainerTest.class);

    @Test
    public void testNatsStartup() throws Exception {
        NatsContainer natsContainer = new NatsContainer()
                .withLogConsumer(new Slf4jLogConsumer(log));
        natsContainer.start();
        WaitingConsumer consumer = new WaitingConsumer();
        natsContainer.followOutput(consumer, OutputFrame.OutputType.STDERR);
        try {
            consumer.waitUntil(frame ->
                            frame.getUtf8String().contains("Server is ready"),
                    5, TimeUnit.SECONDS);
            testNatsFunctionality(natsContainer);
        } catch (TimeoutException e) {
            Assert.fail("Nats container failed to start.");
        } finally {
            natsContainer.stop();
        }
    }

    private void testNatsFunctionality(NatsContainer natsContainer) throws Exception {
        NatsClient.ResultHolder fooBarresultHolder = new NatsClient.ResultHolder(1, 3);
        NatsClient natsClient = new NatsClient(natsContainer.getClusterID(), natsContainer.getBootstrapServerUrl(),
                fooBarresultHolder);
        natsClient.connect();
        natsClient.subscribeFromNow("foo.bar");
        natsClient.publish("foo.bar", "Siddhi rulezzz!");
        Assert.assertTrue(((ArrayList<String>) fooBarresultHolder.waitAndGetResults())
                .get(0).contains("Siddhi rulezzz!"));
    }
}
