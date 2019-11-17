package io.siddhi.distribution.metrics.prometheus.reporter.reporter.impl;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.wso2.carbon.metrics.core.reporter.ScheduledReporter;
import org.wso2.carbon.metrics.core.reporter.impl.AbstractReporter;

import java.util.concurrent.TimeUnit;

//import io.prometheus.client.CollectorRegistry;
//import io.prometheus.client.dropwizard.DropwizardExports;
//import io.prometheus.client.exporter.HTTPServer;
//import io.prometheus.client.exporter.MetricsServlet;
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.servlet.ServletContextHandler;
//import org.eclipse.jetty.servlet.ServletHolder;
//import org.wso2.msf4j.MicroservicesRunner;

public class PrometheusReporter extends AbstractReporter implements ScheduledReporter {

    private final MetricRegistry metricRegistry;

    private final MetricFilter metricFilter;

    private final long pollingPeriod;

    private PromReporter prometheusReporter;

//    Server server = new Server(1234);

    public PrometheusReporter(String name, MetricRegistry metricRegistry, MetricFilter metricFilter, long pollingPeriod) {
        super(name);
        this.metricRegistry = metricRegistry;
        this.metricFilter = metricFilter;
        this.pollingPeriod = pollingPeriod;
    }

    @Override
    public void report() {
        if (prometheusReporter != null) {
            prometheusReporter.report();
        }

    }

    @Override
    public void startReporter() {
        prometheusReporter = PromReporter.forRegistry(metricRegistry).filter(metricFilter)
                .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
        prometheusReporter.start(pollingPeriod, TimeUnit.SECONDS);

//        CollectorRegistry defaultRegistry = CollectorRegistry.defaultRegistry;
//        defaultRegistry.clear();
//        defaultRegistry.register(new DropwizardExports(metricRegistry));
//        ServletContextHandler handler = new ServletContextHandler();
//        handler.setContextPath("/");
//        server.setHandler(handler);
//        handler.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
//
//        try {
//
//            server.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            server.join();
//                new MicroservicesRunner()
////                        .deploy(new HelloService())
//                        .start();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
    }

    @Override
    public void stopReporter() {
        if (prometheusReporter != null) {
//            try {
//                server.stop();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            prometheusReporter.stop();
            prometheusReporter = null;

        }

    }
}
