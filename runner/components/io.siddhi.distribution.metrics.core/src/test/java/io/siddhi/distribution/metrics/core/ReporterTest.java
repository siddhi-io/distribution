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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.config.annotation.Configuration;

/**
 * Test Cases for Reporters.
 */
@Configuration(namespace = "wso2.metrics.prometheus", description = "Carbon Metrics Configuration Parameters")
public class ReporterTest extends BaseReporterTest {

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
    public void testPrometheusReporter() {
        metricManagementService.startReporter("Prometheus");
        Assert.assertTrue(metricManagementService.isReporterRunning("Prometheus"));
        metricManagementService.report();
        metricManagementService.stopReporter("Prometheus");
        Assert.assertFalse(metricManagementService.isReporterRunning("Prometheus"));
    }

}
