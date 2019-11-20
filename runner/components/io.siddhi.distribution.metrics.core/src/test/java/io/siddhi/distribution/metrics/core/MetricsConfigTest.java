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

package io.siddhi.distribution.metrics.core;

import io.siddhi.distribution.metrics.prometheus.reporter.config.model.MetricsConfig;
import io.siddhi.distribution.metrics.prometheus.reporter.config.model.PrometheusReporterConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigurationException;

public class MetricsConfigTest {

    private static MetricsConfig metricsConfig;

    @BeforeClass
    private void load() throws ConfigurationException {
        metricsConfig = TestUtils.getConfigProvider("metrics-prometheus.yaml")
                .getConfigurationObject(MetricsConfig.class);

    }

    @Test
    public void testPrometheusConfigLoad() {
        PrometheusReporterConfig config = metricsConfig.getReporting().getPrometheus().iterator().next();
        Assert.assertEquals(config.getName(), "prometheus");
        Assert.assertTrue(config.isEnabled());
        Assert.assertEquals(config.getPollingPeriod(), 600L);
    }
}
