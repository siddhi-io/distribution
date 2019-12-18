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
package io.siddhi.distribution.metrics.prometheus.reporter.impl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.dropwizard.samplebuilder.CustomMappingSampleBuilder;
import io.prometheus.client.dropwizard.samplebuilder.MapperConfig;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;
import io.prometheus.client.exporter.HTTPServer;
import org.apache.log4j.Logger;
import org.wso2.carbon.metrics.core.reporter.impl.AbstractReporter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which outputs measurements to prometheus.
 */
public class PrometheusReporter extends AbstractReporter {

    private final MetricRegistry metricRegistry;
    private final MetricFilter metricFilter;
    private PrometheusReporter prometheusReporter;
    private CollectorRegistry collectorRegistry;
    private SampleBuilder sampleBuilder;
    private HTTPServer server;
    private String reporterName;
    private String serverURL;

    private static final Logger log = Logger.getLogger(PrometheusReporter.class);

    private PrometheusReporter(String reporterName, MetricRegistry metricRegistry,
                               MetricFilter metricFilter, String serverURL) {
        super(reporterName);
        this.reporterName = reporterName;
        this.metricRegistry = metricRegistry;
        this.metricFilter = metricFilter;
        this.serverURL = serverURL;
    }

    @Override
    public void startReporter() {

        prometheusReporter = PrometheusReporter.forRegistry(metricRegistry, serverURL)
                .filter(metricFilter)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        collectorRegistry = new CollectorRegistry();

        try {
            URL target = new URL(serverURL);

//          Configuration for metrics without functions.
            MapperConfig queryMetricConfig = new MapperConfig();
            queryMetricConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Queries.*.*");
            queryMetricConfig.setName("siddhi.query" + "." + "${2}");
            Map<String, String> queryMetricLabels = new HashMap<String, String>();
            queryMetricLabels.put("app", "${0}");
            queryMetricLabels.put("type", "query");
            queryMetricLabels.put("element", "${1}");
            queryMetricLabels.put("metrics", "${2}");
            queryMetricConfig.setLabels(queryMetricLabels);

            MapperConfig sourceMapperMetricConfig = new MapperConfig();
            sourceMapperMetricConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.SourceMappers.*.*");
            sourceMapperMetricConfig.setName("siddhi.sourcemapper" + "." + "${2}");
            Map<String, String> sourceMapperMetriclabels = new HashMap<String, String>();
            sourceMapperMetriclabels.put("app", "${0}");
            sourceMapperMetriclabels.put("type", "sourcemapper");
            sourceMapperMetriclabels.put("element", "${1}");
            sourceMapperMetriclabels.put("metrics", "${2}");
            sourceMapperMetricConfig.setLabels(sourceMapperMetriclabels);

            MapperConfig sinkMapperMetricConfig = new MapperConfig();
            sinkMapperMetricConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.SinkMappers.*.*");
            sinkMapperMetricConfig.setName("siddhi.sikmapper" + "." + "${2}");
            Map<String, String> sinkMapperMetriclabels = new HashMap<String, String>();
            sinkMapperMetriclabels.put("app", "${0}");
            sinkMapperMetriclabels.put("type", "sinkmapper");
            sinkMapperMetriclabels.put("element", "${1}");
            sinkMapperMetriclabels.put("metrics", "${2}");
            sinkMapperMetricConfig.setLabels(sinkMapperMetriclabels);

            MapperConfig sourceMetricConfig = new MapperConfig();
            sourceMetricConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Sources.*.*");
            sourceMetricConfig.setName("siddhi.source" + "." + "${2}");
            Map<String, String> sourceMetriclabels = new HashMap<String, String>();
            sourceMetriclabels.put("app", "${0}");
            sourceMetriclabels.put("type", "source");
            sourceMetriclabels.put("element", "${1}");
            sourceMetriclabels.put("metrics", "${2}");
            sourceMetricConfig.setLabels(sourceMetriclabels);

            MapperConfig streamMetricConfig = new MapperConfig();
            streamMetricConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Streams.*.*");
            streamMetricConfig.setName("siddhi.stream" + "." + "${2}");
            Map<String, String> streamMetriclabels = new HashMap<String, String>();
            streamMetriclabels.put("app", "${0}");
            streamMetriclabels.put("type", "stream");
            streamMetriclabels.put("element", "${1}");
            streamMetriclabels.put("metrics", "${2}");
            streamMetricConfig.setLabels(streamMetriclabels);

            MapperConfig sinkMetricConfig = new MapperConfig();
            sinkMetricConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Sinks.*.*");
            sinkMetricConfig.setName("siddhi.sink" + "." + "${2}");
            Map<String, String> sinkMetriclabels = new HashMap<String, String>();
            sinkMetriclabels.put("app", "${0}");
            sinkMetriclabels.put("type", "sink");
            sinkMetriclabels.put("element", "${1}");
            sinkMetriclabels.put("metrics", "${2}");
            sinkMetricConfig.setLabels(sinkMetriclabels);

            MapperConfig onDemandQueryMetricConfig = new MapperConfig();
            onDemandQueryMetricConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.OnDemandQueries.*.*");
            onDemandQueryMetricConfig.setName("siddhi.ondemandquery" + "." + "${2}");
            Map<String, String> onDemandQueryMetriclabels = new HashMap<String, String>();
            onDemandQueryMetriclabels.put("app", "${0}");
            onDemandQueryMetriclabels.put("type", "ondemandquery");
            onDemandQueryMetriclabels.put("element", "${1}");
            onDemandQueryMetriclabels.put("metrics", "${2}");
            onDemandQueryMetricConfig.setLabels(onDemandQueryMetriclabels);

            MapperConfig tableMetricConfig = new MapperConfig();
            tableMetricConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Tables.*.*");
            tableMetricConfig.setName("siddhi.table" + "." + "${2}");
            Map<String, String> tableMetriclabels = new HashMap<String, String>();
            tableMetriclabels.put("app", "${0}");
            tableMetriclabels.put("type", "table");
            tableMetriclabels.put("element", "${1}");
            tableMetriclabels.put("metrics", "${2}");
            tableMetricConfig.setLabels(tableMetriclabels);

            MapperConfig windowMetricConfig = new MapperConfig();
            windowMetricConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Windows.*.*");
            windowMetricConfig.setName("siddhi.window" + "." + "${2}");
            Map<String, String> windowMetriclabels = new HashMap<String, String>();
            windowMetriclabels.put("app", "${0}");
            windowMetriclabels.put("type", "window");
            windowMetriclabels.put("element", "${1}");
            windowMetriclabels.put("metrics", "${2}");
            windowMetricConfig.setLabels(windowMetriclabels);

            MapperConfig triggerMetricConfig = new MapperConfig();
            triggerMetricConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Trigger.*.*");
            triggerMetricConfig.setName("siddhi.trigger" + "." + "${2}");
            Map<String, String> triggerMetriclabels = new HashMap<String, String>();
            triggerMetriclabels.put("app", "${0}");
            triggerMetriclabels.put("type", "trigger");
            triggerMetriclabels.put("element", "${1}");
            triggerMetriclabels.put("metrics", "${2}");
            triggerMetricConfig.setLabels(triggerMetriclabels);

            MapperConfig aggregationMetricConfig = new MapperConfig();
            aggregationMetricConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Aggregations.*.*");
            aggregationMetricConfig.setName("siddhi.aggregation" + "." + "${2}");
            Map<String, String> aggregationMetriclabels = new HashMap<String, String>();
            aggregationMetriclabels.put("app", "${0}");
            aggregationMetriclabels.put("type", "aggregation");
            aggregationMetriclabels.put("element", "${1}");
            aggregationMetriclabels.put("metrics", "${2}");
            aggregationMetricConfig.setLabels(aggregationMetriclabels);

//          Configuration for metrics with Table Operations.
            MapperConfig tableOperatorConfig = new MapperConfig();
            tableOperatorConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Tables.*.*.*");
            tableOperatorConfig.setName("siddhi.table" + "." + "${3}");
            Map<String, String> tableOperatorLabels = new HashMap<String, String>();
            tableOperatorLabels.put("app", "${0}");
            tableOperatorLabels.put("type", "table");
            tableOperatorLabels.put("element", "${1}");
            tableOperatorLabels.put("operation", "${2}");
            tableOperatorLabels.put("metrics", "${3}");
            tableOperatorConfig.setLabels(tableOperatorLabels);

//          Configuration for metrics with Aggregation Operations.
            MapperConfig aggregationOperatorConfig = new MapperConfig();
            aggregationOperatorConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Aggregations.*.*.*");
            aggregationOperatorConfig.setName("siddhi.aggregation" + "." + "${3}");
            Map<String, String> aggregationOperatorLabels = new HashMap<String, String>();
            aggregationOperatorLabels.put("app", "${0}");
            aggregationOperatorLabels.put("type", "aggregation");
            aggregationOperatorLabels.put("element", "${1}");
            aggregationOperatorLabels.put("operation", "${2}");
            aggregationOperatorLabels.put("metrics", "${3}");
            aggregationOperatorConfig.setLabels(aggregationOperatorLabels);

//          Configuration for metrics with Window Operations.
            MapperConfig windowOperatorConfig = new MapperConfig();
            windowOperatorConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Windows.*.*.*");
            windowOperatorConfig.setName("siddhi.window" + "." + "${3}");
            Map<String, String> windowOperatorLabels = new HashMap<String, String>();
            windowOperatorLabels.put("app", "${0}");
            windowOperatorLabels.put("type", "window");
            windowOperatorLabels.put("element", "${1}");
            windowOperatorLabels.put("operation", "${2}");
            windowOperatorLabels.put("metrics", "${3}");
            windowOperatorConfig.setLabels(windowOperatorLabels);

//          Configuration for metrics with Sink Operations.
            MapperConfig sinkOperatorConfig = new MapperConfig();
            sinkOperatorConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Sinks.*.*.*");
            sinkOperatorConfig.setName("siddhi.sink" + "." + "${3}");
            Map<String, String> sinkOperatorLabels = new HashMap<String, String>();
            sinkOperatorLabels.put("app", "${0}");
            sinkOperatorLabels.put("type", "sink");
            sinkOperatorLabels.put("element", "${1}");
            sinkOperatorLabels.put("operation", "${2}");
            sinkOperatorLabels.put("metrics", "${3}");
            sinkOperatorConfig.setLabels(sinkOperatorLabels);

//          Configuration for metrics with Source Operations.
            MapperConfig sourceOperatorConfig = new MapperConfig();
            sourceOperatorConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Sources.*.*.*");
            sourceOperatorConfig.setName("siddhi.source" + "." + "${3}");
            Map<String, String> sourceOperatorLabels = new HashMap<String, String>();
            sourceOperatorLabels.put("app", "${0}");
            sourceOperatorLabels.put("type", "source");
            sourceOperatorLabels.put("element", "${1}");
            sourceOperatorLabels.put("operation", "${2}");
            sourceOperatorLabels.put("metrics", "${3}");
            sourceOperatorConfig.setLabels(sourceOperatorLabels);

//          Configuration for metrics with Operation types.
            MapperConfig sinkMapperFunctionConfig = new MapperConfig();
            sinkMapperFunctionConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.SinkMappers.*.*.*.*");
            sinkMapperFunctionConfig.setName("siddhi.sinkmapper" + "." + "${4}");
            Map<String, String> sinkMapperFunctionLabels = new HashMap<String, String>();
            sinkMapperFunctionLabels.put("app", "${0}");
            sinkMapperFunctionLabels.put("type", "sinkmappper");
            sinkMapperFunctionLabels.put("element", "${1}");
            sinkMapperFunctionLabels.put("operation", "${2}");
            sinkMapperFunctionLabels.put("operationType", "${3}");
            sinkMapperFunctionLabels.put("metrics", "${4}");
            sinkMapperFunctionConfig.setLabels(sinkMapperFunctionLabels);

            MapperConfig sourceMapperFunctionConfig = new MapperConfig();
            sourceMapperFunctionConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.SourceMappers.*.*.*.*");
            sourceMapperFunctionConfig.setName("siddhi.sourcemapper" + "." + "${4}");
            Map<String, String> sourceMapperFunctionLabels = new HashMap<String, String>();
            sourceMapperFunctionLabels.put("app", "${0}");
            sourceMapperFunctionLabels.put("type", "sourcemappper");
            sourceMapperFunctionLabels.put("element", "${1}");
            sourceMapperFunctionLabels.put("operation", "${2}");
            sourceMapperFunctionLabels.put("operationType", "${3}");
            sourceMapperFunctionLabels.put("metrics", "${4}");
            sourceMapperFunctionConfig.setLabels(sourceMapperFunctionLabels);

//          Configuration for metrics with Functions.
            MapperConfig sinkfunctionConfig = new MapperConfig();
            sinkfunctionConfig.setMatch("io.siddhi.SiddhiApps.*.Siddhi.Sinks.*.*.*.*");
            sinkfunctionConfig.setName("siddhi.sink" + "." + "${4}");
            Map<String, String> sinkFunctionLabels = new HashMap<String, String>();
            sinkFunctionLabels.put("app", "${0}");
            sinkFunctionLabels.put("type", "sink");
            sinkFunctionLabels.put("element", "${1}");
            sinkFunctionLabels.put("operation", "${2}");
            sinkFunctionLabels.put("operationType", "${3}");
            sinkFunctionLabels.put("metrics", "${4}");
            sinkfunctionConfig.setLabels(sinkFunctionLabels);

            sampleBuilder = new CustomMappingSampleBuilder(Arrays.asList(queryMetricConfig, sourceMapperMetricConfig,
                    sinkMapperMetricConfig, sourceMetricConfig, streamMetricConfig, sinkMetricConfig,
                    onDemandQueryMetricConfig, tableMetricConfig, windowMetricConfig, triggerMetricConfig,
                    aggregationMetricConfig, windowOperatorConfig, tableOperatorConfig, aggregationOperatorConfig,
                    sinkOperatorConfig, sourceOperatorConfig, sinkMapperFunctionConfig, sourceMapperFunctionConfig,
                    sinkfunctionConfig));
            Collector collector = new DropwizardExports(metricRegistry, sampleBuilder);
            collectorRegistry.register(collector);
            InetSocketAddress address = new InetSocketAddress(target.getHost(), target.getPort());
            server = new HTTPServer(address, collectorRegistry);
            log.info("Prometheus Server has successfully connected at " + serverURL);
        } catch (MalformedURLException e) {
            log.error("Invalid server url '" + serverURL + "' configured for '" + reporterName + "'.", e);
        } catch (IOException e) {
            log.error("Failed to start Prometheus reporter '" + reporterName + "' at '" + serverURL + "'.", e);
        }
    }

    @Override
    public void stopReporter() {
        if (prometheusReporter != null) {
            disconnect();
            destroy();
            prometheusReporter.stop();
            prometheusReporter = null;
        }
    }

    public static PrometheusReporter.Builder forRegistry(MetricRegistry registry, String serverURL) {
        return new PrometheusReporter.Builder(registry, serverURL);
    }

    private void disconnect() {
        if (server != null) {
            server.stop();
            log.info("Prometheus Server successfully stopped at " + serverURL);
        }
    }

    private void destroy() {
        if (collectorRegistry != null) {
            collectorRegistry.clear();
        }
    }

    /**
     * Builds a {@link PrometheusReporter} with the given properties.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private MetricFilter filter;
        private String serverURL;

        private Builder(MetricRegistry registry, String serverURL) {
            this.registry = registry;
            this.serverURL = serverURL;
            this.filter = MetricFilter.ALL;
        }

        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder convertDurationsTo(TimeUnit durationUnit) {
            return this;
        }

        public Builder convertRatesTo(TimeUnit rateUnit) {
            return this;
        }

        public PrometheusReporter build() {
            return new PrometheusReporter("prometheus", registry, filter, serverURL);
        }

    }

}
