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

import io.siddhi.distribution.test.framework.util.NatsClient;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.rnorth.ducttape.unreliables.Unreliables.retryUntilSuccess;

/**
 * NATS Streaming docker container.
 */
public class NatsContainer extends GenericContainer<NatsContainer> {
    private static final String IMAGE_NAME = "nats-streaming";
    private static final String DEFAULT_NATS_VERSION = "0.11.2";
    private static final String DEFAULT_CLUSTER_ID = "test-cluster";
    private static final int DEFAULT_NATS_PORT = 4222;
    private static final int DEFAULT_SERVICE_MONITOR_PORT = 8222;
    private static final String CLUSTER_ID_SYSTEM_PARAMETER = "-cid";
    private String clusterID = DEFAULT_CLUSTER_ID;
    private int startupTimeoutSeconds = 120;

    public NatsContainer() {
        super(IMAGE_NAME + ":" + DEFAULT_NATS_VERSION);
        withExposedPorts(DEFAULT_NATS_PORT);
        withExposedPorts(DEFAULT_SERVICE_MONITOR_PORT);
    }

    public NatsContainer(String dockerImageName) {
        super(dockerImageName);
        withExposedPorts(DEFAULT_NATS_PORT);
        withExposedPorts(DEFAULT_SERVICE_MONITOR_PORT);
    }

    public String getNetworkedBootstrapServerUrl() {
        return "nats://" + getNetworkAliases().get(0) + ":" + DEFAULT_NATS_PORT;
    }

    public String getBootstrapServerUrl() {
        return "nats://" + getContainerIpAddress() + ":" + getMappedPort(DEFAULT_NATS_PORT);
    }

    int getStartupTimeoutSeconds() {
        return startupTimeoutSeconds;
    }

    public void withStartupTimeoutSeconds(int startupTimeoutSeconds) {
        this.startupTimeoutSeconds = startupTimeoutSeconds;
    }

    @Override
    protected void waitUntilContainerStarted() {
        logger().info("Waiting for NATS Streaming Container to start...");
        retryUntilSuccess(getStartupTimeoutSeconds(), TimeUnit.SECONDS, () -> {
            if (!isRunning()) {
                throw new ContainerLaunchException("NATS Streaming Container failed to start");
            }
            try (Socket s = new Socket("localhost", this.getMappedPort(DEFAULT_SERVICE_MONITOR_PORT))) {
                try {
                    NatsClient natsClient = new NatsClient(DEFAULT_CLUSTER_ID, getBootstrapServerUrl());
                    natsClient.connect();
                    return null;
                } catch (IOException | InterruptedException ignored) {
                }
            }
            return 0;
        });
    }

    /**
     * Sets the ClusterID of the NATS Streaming server.
     *
     * @param clusterId Cluster ID
     * @return self
     */
    public NatsContainer withClusterId(String clusterId) {
        this.clusterID = clusterId;
        withCommand(CLUSTER_ID_SYSTEM_PARAMETER, clusterId);
        return this;
    }

    public String getClusterID() {
        return clusterID;
    }

    /**
     * Sets additional NATS Streaming server System Parameters.
     *
     * @param key   System parameter key
     * @param value System parameter key
     * @return self
     */
    public NatsContainer withSystemParams(String key, String value) {
        withCommand(key, value);
        return this;
    }

    @Override
    public void stop() {
        Stream.<Runnable>of(super::stop).parallel().forEach(Runnable::run);
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
        NatsContainer that = (NatsContainer) o;
        return Objects.equals(clusterID, that.clusterID) &&
                Objects.equals(startupTimeoutSeconds, that.startupTimeoutSeconds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), clusterID, startupTimeoutSeconds);
    }
}
