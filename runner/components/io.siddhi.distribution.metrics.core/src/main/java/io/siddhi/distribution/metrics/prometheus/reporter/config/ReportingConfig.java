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
package io.siddhi.distribution.metrics.prometheus.reporter.config;

import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for all reporters in Metrics Core.
 */

public class ReportingConfig {

    private Set<PrometheusReporterConfig> prometheus;

    public ReportingConfig() {
        this.prometheus = new HashSet<>();
        this.prometheus.add(new PrometheusReporterConfig());
    }

    public Set<PrometheusReporterConfig> getPrometheus() {
        return prometheus;
    }

    public void setPrometheus(Set<PrometheusReporterConfig> prometheus) {
        this.prometheus = prometheus;
    }

    public Set<? extends ReporterBuilder> getReporterBuilders() {
        Set<ReporterBuilder> reporterBuilders = new HashSet<>();

        if (prometheus != null) {
            reporterBuilders.addAll(prometheus);
        }
        return reporterBuilders;
    }
}
