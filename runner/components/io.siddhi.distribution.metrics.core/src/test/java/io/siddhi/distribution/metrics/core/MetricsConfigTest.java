package io.siddhi.distribution.metrics.core;

import io.siddhi.distribution.metrics.prometheus.reporter.config.model.MetricsConfig;
import io.siddhi.distribution.metrics.prometheus.reporter.config.model.PrometheusReporterConfig;
//import io.siddhi.distribution.metrics.prometheus.reporter.config.model.ReportingConfig;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigurationException;

public class MetricsConfigTest {

    static MetricsConfig metricsConfig;

    @BeforeClass
    private void load() throws ConfigurationException {
        metricsConfig = TestUtils.getConfigProvider("metrics-prometheus.yaml")
                .getConfigurationObject(MetricsConfig.class);

    }

    @Test
    public void testPrometheusConfigLoad() {
        PrometheusReporterConfig config = metricsConfig.getReporting().getPrometheus().iterator().next();
        Assert.assertEquals(config.getName(), "Prometheus");
        Assert.assertEquals(config.isEnabled(), true);
        Assert.assertEquals(config.getPollingPeriod(), 600L);
    }

}
