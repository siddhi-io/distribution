/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.json.JSONObject;
import io.siddhi.distribution.event.simulator.core.exception.exception.InvalidConfigException;
import io.siddhi.distribution.event.simulator.core.internal.generator.generator.random.RandomAttributeGenerator;
import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * factory interface used for creating random attribute generators
 */
public interface RandomAttrGeneratorFactory {

    RandomAttributeGenerator createRandomAttrGenerator(JSONObject attributeConfig, Attribute.Type attrType) throws
            InvalidConfigException;

    void validateRandomAttrGenerator(JSONObject attributeConfig, Attribute.Type attrType) throws
            InvalidConfigException;

}
