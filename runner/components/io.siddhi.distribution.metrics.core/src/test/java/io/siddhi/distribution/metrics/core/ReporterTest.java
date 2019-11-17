package io.siddhi.distribution.metrics.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
//import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;
//import org.wso2.carbon.metrics.core.MetricManagementService;
//import org.wso2.carbon.metrics.core.MetricService;
//import org.wso2.carbon.metrics.core.Metrics;

/**
 * Test Cases for Reporters.
 */
public class ReporterTest extends BaseReporterTest {

    private static final Logger logger = LoggerFactory.getLogger(ReporterTest.class);

    @BeforeClass
    private void stopReporters() {
        metricManagementService.stopReporters();
    }

    @Test
    public void testInvalidReporter() {
        try {
            metricManagementService.startReporter("INVALID");
            Assert.fail("The reporter should not be started");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testPrometheusReporter(){
        metricManagementService.startReporter("Prometheus");
        Assert.assertTrue(metricManagementService.isReporterRunning("Prometheus"));
        metricManagementService.report();
        metricManagementService.stopReporter("Prometheus");
        Assert.assertFalse(metricManagementService.isReporterRunning("Prometheus"));
    }

    private <T extends ReporterBuilder> void addReporter(T reporterBuilder) {
        try {
            metricManagementService.addReporter(reporterBuilder);
            Assert.fail("Add Reporter should fail.");
        } catch (IllegalArgumentException | ReporterBuildException e) {
            logger.info("Exception message from Add Reporter: {}", e.getMessage());
        }
    }
}
