package io.siddhi.distribution.test.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testng.annotations.Test;

public class SiddhiRunnerContainerTest {
    private static final Logger log = LoggerFactory.getLogger(SiddhiRunnerContainerTest.class);

    @Test
    public void testUsage() throws Exception {
        try (SiddhiRunnerContainer siddhiRunnerContainer = new SiddhiRunnerContainer()
                .withLogConsumer(new Slf4jLogConsumer(log))) {
            siddhiRunnerContainer.start();
        }
    }

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

}
