package io.siddhi.distribution.test.framework;

import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.SocatContainer;
import org.testcontainers.utility.Base58;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class SiddhiRunnerContainer extends GenericContainer<SiddhiRunnerContainer> {
    public static final String SIDDHI_RUNNER_VERSION = "0.1.0";
    public static final String IMAGE = "siddhiio/siddhi-runner-alpine";
    public static final int DEFAULT_HTTP_PORT = 9090;
    public static final int DEFAULT_HTTPS_PORT = 9443;
    public static final String RUNTIME_SERVER_HOME = "/home/siddhi_user/siddhi-runner-" + SIDDHI_RUNNER_VERSION;
    private SocatContainer proxy;
    private Map<Integer, Integer> exposedPortMapping = new HashMap<>();
    private int startupTimeoutSeconds = 120;

    public SiddhiRunnerContainer() {
        super(IMAGE + ":" + SIDDHI_RUNNER_VERSION);
    }


    public SiddhiRunnerContainer (String dockerImageName) {
        super(dockerImageName);
        withNetwork(Network.newNetwork());
        withNetworkAliases("siddhi-runner" + Base58.randomString(6));
        withExposedPorts(DEFAULT_HTTP_PORT);
    }

    public SiddhiRunnerContainer (String dockerImageName, int[] portsToExport, String deploymentDirectory) {
        super(dockerImageName);
        withNetwork(Network.newNetwork());
        withNetworkAliases("siddhi-runner" + Base58.randomString(6));
        for (int port : portsToExport) {
            withExposedPorts(port);
        }
        withFileSystemBind(deploymentDirectory,
                RUNTIME_SERVER_HOME + "/wso2/runner/deployment/siddhi-files",
                BindMode.READ_ONLY);
    }

    public SiddhiRunnerContainer (int[] portsToExport, String deploymentDirectory) {
        super(IMAGE + ":" + SIDDHI_RUNNER_VERSION);
        withNetwork(Network.newNetwork());
        withNetworkAliases("siddhi-runner" + Base58.randomString(6));
        for (int port : portsToExport) {
            withExposedPorts(port);
            exposedPortMapping.put(port, null);
        }
        withFileSystemBind(deploymentDirectory,
                RUNTIME_SERVER_HOME + "/wso2/runner/deployment/siddhi-files",
                BindMode.READ_ONLY);
    }


    @Override
    protected void doStart() {
        String networkAlias = getNetworkAliases().get(0);
        super.doStart();
        Iterator it = exposedPortMapping.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            pair.setValue(getMappedPort((Integer) pair.getKey()));
            it.remove();
        }
    }

    public Map<Integer, Integer> getExposedPortMapping() {
        return exposedPortMapping;
    }

    @Override
    protected void waitUntilContainerStarted() {
        logger().info("Waiting for Siddhi Runner to start.");
        Unreliables.retryUntilSuccess(getStartupTimeoutSeconds(), TimeUnit.SECONDS, () -> {
            return null;
        });
    }

    @Override
    public void stop() {
//        Stream.<Runnable>of(super::stop, proxy::stop).parallel().forEach(Runnable::run);
        Stream.<Runnable>of(super::stop).parallel().forEach(Runnable::run);
    }

    protected int getStartupTimeoutSeconds() {
        return startupTimeoutSeconds;
    }

    //todo override equals and hashcode
}
