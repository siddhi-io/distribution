/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.siddhi.langserver.completion;

import io.siddhi.langserver.utils.metadata.MetaData;
import io.siddhi.langserver.utils.metadata.MetaDataProviderUtil;
import io.siddhi.langserver.utils.metadata.ProcessorMetaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code MetaDataProvider}  Provider of Extension MetaData.
 */
public class MetaDataProvider {

    private List<ProcessorMetaData> functions = new ArrayList<>();
    private List<ProcessorMetaData> windowProcessors = new ArrayList<>();
    private List<ProcessorMetaData> streamProcessors = new ArrayList<>();
    private List<ProcessorMetaData> aggregatorFunctions = new ArrayList<>();
    private List<ProcessorMetaData> sinks = new ArrayList<>();
    private List<ProcessorMetaData> sources = new ArrayList<>();
    private List<ProcessorMetaData> sourceMaps = new ArrayList<>();
    private List<ProcessorMetaData> sinkMaps = new ArrayList<>();
    private List<ProcessorMetaData> stores = new ArrayList<>();
    private Map<String, MetaData> extensionMetaData = new HashMap<>();
    private static boolean initialized = false;
    private static MetaDataProvider instance;

    private MetaDataProvider() {
        populateExtensionMetaDta();
    }

    public static MetaDataProvider getInstance() {
        if (!initialized) {
            instance = new MetaDataProvider();
            initialized = true;
        }
        return instance;
    }

    /**
     * @return {@link List<ProcessorMetaData>} list of function metadata of all the extensions.
     */
    public List<ProcessorMetaData> getFunctionMetaData() {
        return this.functions;
    }

    /**
     * @return {@link List<ProcessorMetaData>} list of window processor metadata of all the extensions.
     */
    public List<ProcessorMetaData> getWindowProcessorFunctions() {
        return this.windowProcessors;
    }

    public List<ProcessorMetaData> getStreamProcessorFunctions() {
        return this.streamProcessors;
    }

    public List<ProcessorMetaData> getSources() {
        return this.sources;
    }

    public List<ProcessorMetaData> getSinks() {
        return this.sinks;
    }

    public List<ProcessorMetaData> getSourceMaps() {
        return this.sourceMaps;
    }

    public List<ProcessorMetaData> getSinkMaps() {

        return sinkMaps;
    }

    public List<ProcessorMetaData> getStores() {
        return stores;
    }

    public List<ProcessorMetaData> getAggregatorFunctions() {
        return aggregatorFunctions;
    }

    private void populateExtensionMetaDta() {
        this.extensionMetaData = MetaDataProviderUtil.getExtensionProcessorMetaData();
        for (Map.Entry<String, MetaData> entry : this.extensionMetaData.entrySet()) {
            if ("IncrementalAggregator".equalsIgnoreCase(entry.getKey())) {
                this.aggregatorFunctions.addAll(entry.getValue().getFunctions());
            } else {
                this.functions.addAll(entry.getValue().getFunctions());
                this.streamProcessors.addAll(entry.getValue().getStreamProcessors());
                this.windowProcessors.addAll(entry.getValue().getWindowProcessors());
                this.stores.addAll(entry.getValue().getStores());
                this.sinks.addAll(entry.getValue().getSinks());
                this.sinkMaps.addAll(entry.getValue().getSinkMaps());
                this.sourceMaps.addAll(entry.getValue().getSourceMaps());
            }
        }
    }

}
