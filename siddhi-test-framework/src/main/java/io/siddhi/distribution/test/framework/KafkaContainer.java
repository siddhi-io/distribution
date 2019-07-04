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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.SocatContainer;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * This container wraps Kafka and Zookeeper.
 */
public class KafkaContainer extends GenericContainer<KafkaContainer> {
    private static final String DEFAULT_KAFKA_VERSION = "4.0.0";
    private static final int KAFKA_PORT = 9093;
    private static final int ZOOKEEPER_PORT = 2181;
    private String externalZookeeperConnect = null;
    private SocatContainer proxy;

    public KafkaContainer() {
        this(DEFAULT_KAFKA_VERSION);
    }

    public KafkaContainer(String confluentPlatformVersion) {
        super(TestcontainersConfiguration.getInstance().getKafkaImage() + ":"
                + confluentPlatformVersion);
        withNetwork(Network.newNetwork());
        withNetworkAliases("kafka-" + Base58.randomString(6));
        withExposedPorts(KAFKA_PORT);

        // Use two listeners with different names, it will force Kafka to communicate with itself via internal
        // listener when KAFKA_INTER_BROKER_LISTENER_NAME is set, otherwise Kafka will try to use the
        // advertised listener
        withEnv("KAFKA_LISTENERS", "PLAINTEXT://0.0.0.0:" + KAFKA_PORT + ",BROKER://0.0.0.0:9092");
        withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "BROKER:PLAINTEXT,PLAINTEXT:PLAINTEXT");
        withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "BROKER");
        withEnv("KAFKA_BROKER_ID", "1");
        withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1");
        withEnv("KAFKA_OFFSETS_TOPIC_NUM_PARTITIONS", "1");
        withEnv("KAFKA_LOG_FLUSH_INTERVAL_MESSAGES", Long.MAX_VALUE + "");
        withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0");
    }

    public KafkaContainer withEmbeddedZookeeper() {
        externalZookeeperConnect = null;
        return this;
    }

    public KafkaContainer withExternalZookeeper(String connectString) {
        externalZookeeperConnect = connectString;
        return this;
    }

    @Override
    protected void doStart() {
        String networkAlias = getNetworkAliases().get(0);
        proxy = new SocatContainer()
                .withNetwork(getNetwork())
                .withTarget(KAFKA_PORT, networkAlias)
                .withTarget(ZOOKEEPER_PORT, networkAlias);
        proxy.start();
        withEnv("KAFKA_ADVERTISED_LISTENERS", "BROKER://" + networkAlias + ":9092" + "," + getBootstrapServers());
        if (externalZookeeperConnect != null) {
            withEnv("KAFKA_ZOOKEEPER_CONNECT", externalZookeeperConnect);
        } else {
            addExposedPort(ZOOKEEPER_PORT);
            withEnv("KAFKA_ZOOKEEPER_CONNECT", "localhost:2181");
            withCommand(
                    "sh",
                    "-c",
                    // Use command to create the file to avoid file mounting
                    "printf 'clientPort=2181\ndataDir=/var/lib/zookeeper/data\ndataLogDir=/var/lib/zookeeper/log' " +
                            "> /zookeeper.properties" +
                            " && zookeeper-server-start /zookeeper.properties" +
                            " & /etc/confluent/docker/run"
            );
        }
        super.doStart();
    }

    public String getBootstrapServers() {
        return String.format("PLAINTEXT://%s:%s", proxy.getContainerIpAddress(), proxy.getFirstMappedPort());
    }

    @Override
    public void stop() {
        Stream.<Runnable>of(super::stop, proxy::stop).parallel().forEach(Runnable::run);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        KafkaContainer that = (KafkaContainer) o;
        return Objects.equals(externalZookeeperConnect, that.externalZookeeperConnect) &&
                Objects.equals(proxy, that.proxy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), externalZookeeperConnect, proxy);
    }
}
