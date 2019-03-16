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

package io.siddhi.distribution.event.simulator.core.api.api;

import io.siddhi.distribution.event.simulator.core.exception.exception.FileOperationsException;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

/**
 * Feed API service.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-07-20T09:30:14.336Z")
public abstract class FeedApiService {

    public abstract Response addFeedSimulation(String body, Request request) throws NotFoundException;

    public abstract Response deleteFeedSimulation(String simulationName, Request request) throws NotFoundException;

    public abstract Response getFeedSimulation(String simulationName, Request request) throws NotFoundException;

    public abstract Response getFeedSimulations(Request request) throws NotFoundException;

    public abstract Response operateFeedSimulation(String action, String simulationName, Request request)
            throws NotFoundException;

    public abstract Response updateFeedSimulation(String simulationName, String body, Request request)
            throws NotFoundException, FileOperationsException;

    public abstract Response getFeedSimulationStatus(String simulationName, Request request) throws NotFoundException;

}
