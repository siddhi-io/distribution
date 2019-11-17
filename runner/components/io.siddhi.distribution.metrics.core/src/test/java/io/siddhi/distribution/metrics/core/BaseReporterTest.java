package io.siddhi.distribution.metrics.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.Metrics;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

/**
 * Base Class for all Reporter Based Test Cases.
 */
public abstract class BaseReporterTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseReporterTest.class);

    protected static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    protected static Metrics metrics;

    protected static MetricService metricService;

    protected static MetricManagementService metricManagementService;

    @BeforeSuite
    protected static void init() throws ConfigurationException {
        if (logger.isInfoEnabled()) {
            logger.info("Creating Metrics");
        }
//        System.setProperty("metrics.target", "target");
        metrics = new Metrics(TestUtils.getConfigProvider("metrics-prometheus.yaml"));
        metrics.activate();
        metricService = metrics.getMetricService();
        metricManagementService = metrics.getMetricManagementService();
    }

    @AfterSuite
    protected static void destroy() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Deactivating Metrics");
        }
        metrics.deactivate();
    }
}
