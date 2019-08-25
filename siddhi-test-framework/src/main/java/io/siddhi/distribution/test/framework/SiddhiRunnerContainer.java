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

import io.siddhi.distribution.test.framework.util.BundleUtil;
import io.siddhi.distribution.test.framework.util.HTTPClient;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.siddhi.distribution.test.framework.util.BundleUtil.listFiles;
import static org.rnorth.ducttape.unreliables.Unreliables.retryUntilSuccess;

/**
 * Siddhi Runner docker container class.
 */
public class SiddhiRunnerContainer extends GenericContainer<SiddhiRunnerContainer> {
    private static final String IMAGE = "siddhi-runner";
    private static final String SIDDHI_RUNNER_VERSION = "test";
    private static final int DEFAULT_HTTP_PORT = 9090;
    private static final int DEFAULT_HTTPS_PORT = 9443;
    private static final int DEFAULT_THRIFT_TCP_PORT = 7611;
    private static final int DEFAULT_THRIFT_SSL_PORT = 7711;
    private static final int DEFAULT_BINARY_TCP_PORT = 9612;
    private static final int DEFAULT_BINARY_SSL_PORT = 9712;
    private static final List<Integer> defaultExposePorts = Arrays.asList(DEFAULT_HTTP_PORT, DEFAULT_HTTPS_PORT,
            DEFAULT_THRIFT_TCP_PORT, DEFAULT_THRIFT_SSL_PORT, DEFAULT_THRIFT_SSL_PORT, DEFAULT_BINARY_TCP_PORT,
            DEFAULT_BINARY_SSL_PORT);
    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";
    private static final String DEPLOYMENT_DIRECTORY = "/home/siddhi_user/siddhi-files";
    private static final String LIB_DIRECTORY = "/home/siddhi_user/extend_lib_volume/";
    private static final String CONF_DIRECTORY = "/home/siddhi_user/conf";
    private static final String HEALTH_ENDPOINT_CONTEXT = "/health";
    private static final String OVERRIDE_CONF_SYSTEM_PARAMETER = "-Dconfig";
    private static final String DEPLOY_APP_SYSTEM_PARAMETER = "-Dapps";
    private static final String BLANK_SPACE = " ";
    private List<Integer> portsToExpose = new ArrayList<>(defaultExposePorts);
    private String initScriptPath = "/home/siddhi_user/init.sh";
    private StringBuilder initCommand = new StringBuilder(initScriptPath);
    private int startupTimeoutSeconds = 120;
    private URI baseURI = null;

    public SiddhiRunnerContainer() {
        super(IMAGE + ":" + SIDDHI_RUNNER_VERSION);
    }

    public SiddhiRunnerContainer(String dockerImageName) {
        super(dockerImageName);
    }

    @Override
    protected void configure() {
        for (int port : this.portsToExpose) {
            withExposedPorts(port);
        }
        withCommand(initCommand.toString());
    }

    /**
     * Exposes the ports to accept traffic from the Siddhi Runner container.
     *
     * @param portsToExposeForApps Array of ports to be exposed
     * @return self
     */
    public SiddhiRunnerContainer withExposedPorts(int[] portsToExposeForApps) {
        if (portsToExposeForApps != null) {
            for (int port : portsToExposeForApps) {
                portsToExpose.add(port);
            }
        }
        return this;
    }

    /**
     * Overrides the default init script of Siddhi Runner container as entry point.
     *
     * @param initScriptPath Absolute local path of the init script
     * @return self
     */
    public SiddhiRunnerContainer withInitScript(String initScriptPath) {
        this.initScriptPath = initScriptPath;
        return this;
    }

    /**
     * Merges the provided configurations with the default Siddhi runner configuration.
     * It supports configuring state persistence, databases connections and secure vault.
     *
     * @param confPath Absolute path of the config yaml to be merged
     * @return self
     */
    public SiddhiRunnerContainer withConfig(String confPath) {
        File configFile = new File(confPath);
        if (!configFile.isDirectory()) {
            withFileSystemBind(confPath, CONF_DIRECTORY, BindMode.READ_ONLY);
            initCommand.append(BLANK_SPACE).append(OVERRIDE_CONF_SYSTEM_PARAMETER).append("=").append(CONF_DIRECTORY);
            return this;
        } else {
            logger().error("Provided configurations path points to a directory. " +
                    "Hence, configuration merging is skipped.");
            return this;
        }
    }

    /**
     * Deploys the Siddhi apps within the provided deployment directory in the container.
     *
     * @param deploymentDirectory Absolute path to the Siddhi app deployment directory
     * @return self
     */
    public SiddhiRunnerContainer withSiddhiApps(String deploymentDirectory) {
        File siddhiApps = new File(deploymentDirectory);
        String deploymentPath = DEPLOYMENT_DIRECTORY;
        if (!siddhiApps.isDirectory()) {
            deploymentPath = DEPLOYMENT_DIRECTORY.concat(File.pathSeparator).concat(siddhiApps.getName());
        }
        withFileSystemBind(deploymentDirectory, deploymentPath, BindMode.READ_ONLY);
        initCommand.append(BLANK_SPACE).append(DEPLOY_APP_SYSTEM_PARAMETER).append("=").append(DEPLOYMENT_DIRECTORY);
        return this;
    }

    /**
     * Mounts the JARs within the provided directory in the Siddhi Runner's classpath.
     *
     * @param jarsDir Absolute path of the extra JARs directory
     * @return self
     */
    public SiddhiRunnerContainer withJars(String jarsDir) {
        String tempDirName = "thirdPartyJars";
        int mountMode = 444;
        try {
            Path thirdPartyJarsPath = Files.createTempDirectory(tempDirName);
            BundleUtil.convertFromJarToBundle(jarsDir, thirdPartyJarsPath.toString());
            File thirdPartyJarsDir = new File(thirdPartyJarsPath.toString());
            if (thirdPartyJarsDir.exists()) {
                try {
                    List<Path> directoryContent  = listFiles(thirdPartyJarsDir.toPath());
                    for (Path aDirectoryItem : directoryContent) {
                        if (aDirectoryItem.toString().endsWith(".jar")) {
                            MountableFile mountableFile = MountableFile.forHostPath(aDirectoryItem.toAbsolutePath(),
                                    mountMode);
                            withCopyFileToContainer(mountableFile, LIB_DIRECTORY);
                        }
                    }
                } catch (IOException e) {
                    logger().error("Exception caught while mounting JARs to Siddhi Runner container.", e);
                }
            }
        } catch (IOException e) {
            logger().error("Failed to create temporary directory with name:" + tempDirName, e);
        }
        return this;
    }

    public void withStartupTimeoutSeconds(int startupTimeoutSeconds) {
        this.startupTimeoutSeconds = startupTimeoutSeconds;
    }

    private int getStartupTimeoutSeconds() {
        return startupTimeoutSeconds;
    }

    /**
     * Waits for the Siddhi Runner's health API response. Acts as a readiness probe.
     * Throws a TimeoutException after waiting {startupTimeoutSeconds}
     */
    @Override
    protected void waitUntilContainerStarted() {
        logger().info("Waiting for Siddhi Runner Container to start...");
        retryUntilSuccess(getStartupTimeoutSeconds(), TimeUnit.SECONDS, () -> {
            if (!isRunning()) {
                throw new ContainerLaunchException("Siddhi Runner Container failed to start");
            }
            HTTPClient.HTTPResponseMessage httpResponseMessage = callHealthAPI();
            if (httpResponseMessage.getResponseCode() == 200) {
                logger().info("Siddhi Runner Health API reached successfully.");
                return null;
            } else {
                throw new ConnectException("Failed to connect with the Siddhi Runner health API");
            }
        });
    }

    private HTTPClient.HTTPResponseMessage callHealthAPI() {
        try {
            if (baseURI == null) {
                baseURI = URI.create(String.format("http://%s:%d", "0.0.0.0", this.getMappedPort(DEFAULT_HTTP_PORT)));
            }
            HTTPClient healthRequest = new HTTPClient(baseURI, HEALTH_ENDPOINT_CONTEXT, false,
                    false, "GET", null, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
            return healthRequest.getResponse();
        } catch (IOException ignored) {
        }
        return new HTTPClient.HTTPResponseMessage();
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
        SiddhiRunnerContainer that = (SiddhiRunnerContainer) o;
        return Objects.equals(initCommand, that.initCommand) &&
                Objects.equals(initScriptPath, that.initScriptPath) &&
                Objects.equals(portsToExpose, that.portsToExpose);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), initCommand, initScriptPath, portsToExpose);
    }
}
