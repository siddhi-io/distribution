package io.siddhi.distribution.metrics.prometheus.reporter.config.model;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import io.siddhi.distribution.metrics.prometheus.reporter.reporter.impl.PrometheusReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.config.model.ScheduledReporterConfig;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;

import java.util.Optional;

public class PrometheusReporterConfig extends ScheduledReporterConfig implements ReporterBuilder<PrometheusReporter> {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusReporterConfig.class);

    public PrometheusReporterConfig() {
        super("Prometheus");
    }

    @Override
    public Optional<PrometheusReporter> build(MetricRegistry metricRegistry, MetricFilter metricFilter) throws ReporterBuildException {
        if (!isEnabled()) {
            return Optional.empty();
        }
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Creating Prometheus Reporter for Metrics with %d seconds polling period", getPollingPeriod()));

        }

        return Optional.of(new PrometheusReporter(getName(), metricRegistry, getFilter(metricFilter), getPollingPeriod()));
    }
}
