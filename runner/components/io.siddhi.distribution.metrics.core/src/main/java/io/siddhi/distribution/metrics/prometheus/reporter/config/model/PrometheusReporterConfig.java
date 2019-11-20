/*
 * Copyright (c)  2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Configuration for Prometheus Reporter. Implements {@link ReporterBuilder} to construct a {@link PrometheusReporter}.
 */
public class PrometheusReporterConfig extends ScheduledReporterConfig implements ReporterBuilder<PrometheusReporter> {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusReporterConfig.class);

    public PrometheusReporterConfig() {
        super("prometheus");
    }

    @Override
    public Optional<PrometheusReporter> build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReporterBuildException {
        if (!isEnabled()) {
            return Optional.empty();
        }
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Creating Prometheus Reporter for Metrics with %d seconds polling period",
                    getPollingPeriod()));

        }

        return Optional.of(new PrometheusReporter(getName(), metricRegistry, getFilter(metricFilter),
                getPollingPeriod()));
    }
}
