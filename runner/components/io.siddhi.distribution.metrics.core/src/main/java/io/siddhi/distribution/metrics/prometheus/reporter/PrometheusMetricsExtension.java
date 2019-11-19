package io.siddhi.distribution.metrics.prometheus.reporter;

import io.siddhi.distribution.metrics.prometheus.reporter.config.model.MetricsConfig;
import io.siddhi.distribution.metrics.prometheus.reporter.config.model.PrometheusReporterConfig;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.spi.MetricsExtension;

import java.util.Arrays;
import java.util.Set;

/**
 * * Metrics Extension to support Prometheus Reporter.
 */
@Component(
        name = "io.siddhi.distribution.metrics.prometheus.reporter.PrometheusMetricsExtension",
        service = MetricsExtension.class
)
public class PrometheusMetricsExtension implements MetricsExtension {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetricsExtension.class);
    private static final String STREAMLINED_PROMETHEUS_NS = "metrics.prometheus";
    private String[] names;

    @Override
    public void activate(ConfigProvider configProvider, MetricService metricService,
                         MetricManagementService metricManagementService) {
        MetricsConfig metricsConfig;
        try {
            if (configProvider.getConfigurationObject(STREAMLINED_PROMETHEUS_NS) != null) {
                metricsConfig = configProvider.getConfigurationObject(STREAMLINED_PROMETHEUS_NS, MetricsConfig.class);
            } else {
                metricsConfig = configProvider.getConfigurationObject(MetricsConfig.class);
            }
        } catch (ConfigurationException e) {
            logger.error("Error loading Metrics Configuration", e);
            metricsConfig = new MetricsConfig();
        }
        Set<PrometheusReporterConfig> prometheusReporterConfigs = metricsConfig.getReporting().getPrometheus();
        if (prometheusReporterConfigs != null) {
            prometheusReporterConfigs.forEach(reporterBuilder -> {
                        try {
                            metricManagementService.addReporter(reporterBuilder);
                        } catch (ReporterBuildException e) {
                            logger.warn("PROMETHEUS Reporter build failed", e);
                        }
                    }
            );
            names = prometheusReporterConfigs.stream().map(prometheusReporterConfig ->
                    prometheusReporterConfig.getName()).toArray(size -> new String[size]);
        }
    }

    @Override
    public void deactivate(MetricService metricService, MetricManagementService metricManagementService) {
        if (names != null) {
            Arrays.stream(names).forEach(metricManagementService::removeReporter);
        }
    }

    @Reference(
            name = "carbon.metrics.service",
            service = MetricService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetricService"
    )
    protected void setMetricService(MetricService metricService) {
        // This extension should be activated only after getting MetricService.
        // Metrics Component will activate this extension.
        if (logger.isDebugEnabled()) {
            logger.debug("Metric Service is available as an OSGi service.");
        }
    }

    protected void unsetMetricService(MetricService metricService) {
        // Ignore
    }

}
