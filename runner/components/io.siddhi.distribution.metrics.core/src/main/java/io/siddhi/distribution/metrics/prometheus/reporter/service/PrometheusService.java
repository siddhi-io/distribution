package io.siddhi.distribution.metrics.prometheus.reporter.service;

import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;

import java.util.Optional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Prometheus Service is used to post the metrics into Prometheus.
 */
@Path("/metrics")
public class PrometheusService {
    private MetricRegistry metricRegistry;
    private MicroservicesRunner microservicesRunner;
    private volatile boolean microserviceActive;
    private static final Logger log = LoggerFactory.getLogger(PrometheusService.class);
    private static final String PROMETHEUS_ACTIVATION_SYS_PROPERTY = "prometheus-metrics";
    private static TransportsConfiguration transportsConfiguration;

    public PrometheusService() {
    }

    @POST
    public Response postMetrics(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        CollectorRegistry defaultRegistry = CollectorRegistry.defaultRegistry;
        defaultRegistry.clear();
        defaultRegistry.register(new DropwizardExports(metricRegistry));
        return Response.ok().entity("Prometheus metrics is posted on the Prometheus Server successfully").build();

    }

    /**
     * This is the activation method of Siddhi Prometheus Service Component. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        if (transportsConfiguration != null) {
            microservicesRunner = new MicroservicesRunner(transportsConfiguration);
        }
        String toolIdentifier = System.getProperty(PROMETHEUS_ACTIVATION_SYS_PROPERTY);
        Optional.ofNullable(toolIdentifier)
                .ifPresent(identifier -> {
                    startPrometheusService();
                });
    }

    /**
     * This is the deactivation method of Siddhi Prometheus Service Component. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.debug("Siddhi prometheus deactivated.");
        stopPrometheusService();
    }

    public void startPrometheusService() {
        if (microservicesRunner != null && !microserviceActive) {
            microservicesRunner.deploy(new PrometheusService());
            microservicesRunner.start();
            microserviceActive = true;
        }
        log.info("Prometheus Service activated.");
    }

    public void stopPrometheusService() {
        if (microservicesRunner != null && microserviceActive) {
            microservicesRunner.stop();
            microserviceActive = false;
        }
    }

}
