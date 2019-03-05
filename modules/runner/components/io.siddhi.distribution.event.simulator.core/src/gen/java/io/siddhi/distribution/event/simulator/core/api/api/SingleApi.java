package io.siddhi.distribution.event.simulator.core.api.api;

import io.siddhi.distribution.common.common.EventStreamService;
import io.siddhi.distribution.msf4j.interceptor.common.common.AuthenticationInterceptor;
import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.siddhi.distribution.event.simulator.core.factories.factories.SingleApiServiceFactory;


import io.siddhi.distribution.event.simulator.core.service.service.EventSimulatorDataHolder;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
        name = "simulator-core-single-event-services",
        service = Microservice.class,
        immediate = true
)
@Path("/simulation/single")
@RequestInterceptor(AuthenticationInterceptor.class)
@io.swagger.annotations.Api(description = "the single API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public class SingleApi implements Microservice {
    private final SingleApiService delegate = SingleApiServiceFactory.getSingleApi();
    private static final Logger log = LoggerFactory.getLogger(SingleApi.class);
    @POST
    @Consumes({"text/plain"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Send single event for simulation", notes = "", response = void.class,
                                         tags = {"simulator",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Single Event simulation started successfully",
                                                response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500,
                                                message = "Exception occurred while starting event simulation",
                                                response = void.class)})
    public Response runSingleSimulation(
            @ApiParam(value = "Simulation object which is need to be run", required = true) String body)
            throws io.siddhi.distribution.event.simulator.core.api.api.NotFoundException {
        return delegate.runSingleSimulation(body);
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Single Event Simulator service component is activated");
        }
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Single Event Simulator service component is deactivated");
    }

    /**
     * This bind method will be called when a class is registered against EventStreamService interface of stream
     * processor
     */
    @Reference(
            name = "event.stream.service",
            service = EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "stopEventStreamService"
    )
    protected void eventStreamService(EventStreamService eventStreamService) {
        EventSimulatorDataHolder.getInstance().setEventStreamService(eventStreamService);
        if (log.isDebugEnabled()) {
            log.info("@Reference(bind) EventStreamService");
        }

    }

    /**
     * This is the unbind method which gets called at the un-registration of eventStream OSGi service.
     */
    protected void stopEventStreamService(EventStreamService eventStreamService) {
        EventSimulatorDataHolder.getInstance().setEventStreamService(null);
        if (log.isDebugEnabled()) {
            log.info("@Reference(unbind) EventStreamService");
        }

    }
}
