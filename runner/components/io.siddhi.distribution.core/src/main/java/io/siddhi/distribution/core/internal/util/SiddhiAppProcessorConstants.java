/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.siddhi.distribution.core.internal.util;


/**
 * Class which contains public constants.
 */
public class SiddhiAppProcessorConstants {


    public static final String ANNOTATION_NAME_NAME = "name";
    public static final String SIDDHI_APP_FILES_DIRECTORY = "siddhi-files";
    public static final String SIDDHI_APP_FILE_EXTENSION = ".siddhi";
    public static final String SIDDHI_APP_REST_PREFIX = "siddhi-apps";
    public static final String SYSTEM_PROP_RUN_SIDDHI_APPS = "apps";
    public static final String SYSTEM_PROP_CURRENT_DIRECTORY = "currentDirectory";
    public static final String SIDDHI_APP_DEPLOYMENT_DIRECTORY = "deployment";
    public static final String SIDDHI_APP_STATUS_ACTIVE = "active";
    public static final String SIDDHI_APP_STATUS_INACTIVE = "inactive";
    public static final String WSO2_ARTIFACT_DEPLOYMENT_NS = "wso2.artifact.deployment";
    public static final String WSO2_ARTIFACT_DEPLOYMENT_REPOSITORY_LOCATION = "repositoryLocation";

    public static final String PERSISTENCE_STORE_CLEAR_ENABLED = "persistenceStoreClearEnabled";
    public static final String SIDDHI_APP = "siddhiApp";

    private SiddhiAppProcessorConstants() {
        // Prevents instantiation.
    }
}
