/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package io.siddhi.distribution.event.simulator.core.api;

import io.siddhi.distribution.common.common.EventStreamService;
import io.siddhi.distribution.event.simulator.core.factories.SingleApiServiceFactory;
import io.siddhi.distribution.event.simulator.core.service.EventSimulatorDataHolder;
import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Single API class.
 */
@Component(
        name = "simulator-core-single-event-services",
        service = Microservice.class,
        immediate = true
)
@Path("/simulation/single")
@io.swagger.annotations.Api(description = "the single API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-07-20T09:30:14.336Z")
public class SingleApi implements Microservice {
    private static final Logger log = LoggerFactory.getLogger(SingleApi.class);
    private final SingleApiService delegate = SingleApiServiceFactory.getSingleApi();

    @POST
    @Consumes({"text/plain"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Send single event for simulation", notes = "", response = void.class,
            tags = {"simulator"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Single Event simulation started successfully",
                    response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500,
                    message = "Exception occurred while starting event simulation",
                    response = void.class)})
    public Response runSingleSimulation(
            @ApiParam(value = "Simulation object which is need to be run", required = true) String body)
            throws NotFoundException {
        return delegate.runSingleSimulation(body);
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled.
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
     * processor.
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
