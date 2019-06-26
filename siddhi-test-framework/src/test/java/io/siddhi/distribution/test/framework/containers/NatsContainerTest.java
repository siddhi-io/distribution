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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testng.annotations.Test;

public class NatsContainerTest {
    private static final Logger log = LoggerFactory.getLogger(NatsContainerTest.class);

    @Test
    public void testUsage() {
        NatsContainer natsContainer = new NatsContainer()
                .withLogConsumer(new Slf4jLogConsumer(log));
        natsContainer.start();
    }
}
