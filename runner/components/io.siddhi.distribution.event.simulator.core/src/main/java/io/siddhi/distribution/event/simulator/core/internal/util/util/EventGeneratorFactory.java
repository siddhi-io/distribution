/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.siddhi.distribution.event.simulator.core.internal.util.util;

import io.siddhi.distribution.event.simulator.core.exception.exception.SimulationValidationException;
import io.siddhi.distribution.event.simulator.core.internal.generator.generator.EventGenerator;
import org.json.JSONObject;

/**
 * Factory class used to create event generators.
 */
public interface EventGeneratorFactory {

    EventGenerator createEventGenerator(JSONObject sourceConfig, long startTimestamp, long endTimestamp,
                                        String simulationName)
            throws SimulationValidationException;

    void validateGeneratorConfiguration(JSONObject sourceConfig, String simulationName)
            throws SimulationValidationException;
}
