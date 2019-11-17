package io.siddhi.distribution.metrics.prometheus.reporter.config.model;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * Configuration for Metrics
 */
@Configuration(namespace = "wso2.metrics.prometheus", description = "Carbon Metrics Configuration Parameters")
public class MetricsConfig {

    @Element(description = "Enable Metrics")
//    private boolean enabled = true;

//    @Element(description = "Metrics JMX Configuration")

    private ReportingConfig reporting = new ReportingConfig();

//    public boolean isEnabled() {
//        return enabled;
//    }

//    public void setEnabled(boolean enabled) {
//        this.enabled = enabled;
//    }

    public ReportingConfig getReporting() {
        return reporting;
    }

    public void setReporting(ReportingConfig reporting) {
        this.reporting = reporting;
    }
}
