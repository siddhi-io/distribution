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
package io.siddhi.langserver.utils;

import io.siddhi.langserver.completion.LSCompletionContext;
import io.siddhi.langserver.utils.metadata.AttributeMetaData;
import io.siddhi.langserver.utils.metadata.ParameterMetaData;
import io.siddhi.langserver.utils.metadata.ProcessorMetaData;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * SnippetBlock class provides pre defined  snippet block objects to be generated to completion items.
 * Each completion snippet consists of an array of
 * {insertText,label,completionItemKind,details,filterText,insertTextFormat}.
 */
public class SnippetBlockUtil {

    public static final Map<String, Map<String, Object>> SNIPPETS;
    public static final List<Map<String, Object>> QUERY_SECTION_KEYWORDS;

    public static final List<Map<String, Object>> ATTRIBUTE_TYPES;
    public static final List<Map<String, Object>> ORDER_KEYWORDS;
    public static final List<Map<String, Object>> AGGREGATION_TIME_DURATION;

    public static final List<Map<String, Object>> TIME_VALUE;
    public static final List<Map<String, Object>> BOOLEAN_CONSTANTS;

    private static List<Map<String, Object>> functionSuggestions = new ArrayList<>();
    private static List<Map<String, Object>> windowSuggestions = new ArrayList<>();
    private static List<Map<String, Object>> aggregatorFunctionSuggestions = new ArrayList<>();
    private static List<Map<String, Object>> streamFunctionSuggestions = new ArrayList<>();
    private static List<Map<String, Object>> sinkSuggestions = new ArrayList<>();
    private static List<Map<String, Object>> storeSuggestions = new ArrayList<>();
    private static List<Map<String, Object>> sourceSuggestions = new ArrayList<>();

    public static List<Map<String, Object>> getFunctions() {
        if (functionSuggestions.isEmpty()) {
            List<ProcessorMetaData> functions = LSCompletionContext.INSTANCE.getMetaDataProvider().
                    getFunctionMetaData();
            for (ProcessorMetaData function : functions) {
                if (!function.getParameterOverloads().isEmpty()) {
                    for (String[] parameterOverload : function.getParameterOverloads()) {
                        functionSuggestions.add(generateFunctionSuggestion(function.getName(),
                                function.getNamespace(), parameterOverload,
                                function.getDescription(), function.getReturnAttributes()));
                    }
                } else {
                    functionSuggestions
                            .add(generateFunctionSuggestion(function.getName(), function.getNamespace(), null,
                                    function.getDescription(),
                                    function.getReturnAttributes()));
                }
            }
        }
        return functionSuggestions;
    }

    public static List<Map<String, Object>> getWindowProcessorFunctions() {
        if (windowSuggestions.isEmpty()) {
            List<ProcessorMetaData> windowProcessorFunctions = LSCompletionContext.INSTANCE.getMetaDataProvider().
                    getWindowProcessorFunctions();
            for (ProcessorMetaData function : windowProcessorFunctions) {
                if (!function.getParameterOverloads().isEmpty()) {
                    for (String[] parameterOverload : function.getParameterOverloads()) {
                        windowSuggestions.add(generateFunctionSuggestion(function.getName(), parameterOverload,
                                function.getDescription(), function.getReturnAttributes()));
                    }
                } else {
                    windowSuggestions
                            .add(generateFunctionSuggestion(function.getName(), null,
                                    function.getDescription(), function.getReturnAttributes()));
                }

            }
        }
        return windowSuggestions;
    }

    public static List<Map<String, Object>> getStreamProcessorFunctions() {
        if (streamFunctionSuggestions.isEmpty()) {
            List<ProcessorMetaData> streamProcessorFunctions = LSCompletionContext.INSTANCE.getMetaDataProvider().
                    getStreamProcessorFunctions();
            for (ProcessorMetaData function : streamProcessorFunctions) {
                if (!function.getParameterOverloads().isEmpty()) {
                    for (String[] parameterOverload : function.getParameterOverloads()) {
                        streamFunctionSuggestions
                                .add(generateFunctionSuggestion(function.getName(), function.getNamespace(),
                                        parameterOverload, function.getDescription(), function.getReturnAttributes()));
                    }
                } else {
                    streamFunctionSuggestions
                            .add(generateFunctionSuggestion(function.getName(), null,
                                    function.getDescription(), function.getReturnAttributes()));
                }

            }
        }
        return streamFunctionSuggestions;
    }

    public static Map<String, Object> generateFunctionSuggestion
            (String functionName, String functionNameSpace, String[] parameterOverload,
             String description, List<AttributeMetaData> returnAttributes) {
        StringBuilder insertText = new StringBuilder();
        if (!functionNameSpace.equalsIgnoreCase("")) {
            insertText.append(functionNameSpace + ":");
        }
        insertText.append(functionName);
        if (parameterOverload == null) {
            insertText.append("()");
        } else {
            insertText.append("(").append(String.join(",", parameterOverload)).append(")");
        }
        StringBuilder descriptionText = new StringBuilder();
        descriptionText.append("functionName: " + functionName + "\n\nNameSpace: " + functionNameSpace +
                "\n\ndescription: " + description);
        if (returnAttributes != null) {
            descriptionText.append("\n\nreturnAttributes:");
            returnAttributes.forEach(returnAttribute -> {
                descriptionText.append(returnAttribute.getName()).append("\n" + returnAttribute.getDescription());
            });
        }

        Map<String, Object> functionSuggestion = new HashMap<>();
        functionSuggestion.put("insertText", insertText.toString());
        functionSuggestion.put("label", insertText.toString());
        functionSuggestion.put("completionItemKind", "Function");
        functionSuggestion.put("detail", descriptionText.toString());
        functionSuggestion.put("filterText", insertText.toString());
        functionSuggestion.put("insertTextFormat", "Plaintext");
        return functionSuggestion;
    }

    public static List<Map<String, Object>> getAggregatorFunctions() {
        if (aggregatorFunctionSuggestions.isEmpty()) {
            List<ProcessorMetaData> aggregatorFunctions = LSCompletionContext.INSTANCE.getMetaDataProvider().
                    getAggregatorFunctions();
            for (ProcessorMetaData function : aggregatorFunctions) {
                if (!function.getParameterOverloads().isEmpty()) {
                    for (String[] parameterOverload : function.getParameterOverloads()) {
                        aggregatorFunctionSuggestions.add(generateFunctionSuggestion(function.getName(),
                                parameterOverload,
                                function.getDescription(), function.getReturnAttributes()));
                    }
                } else {
                    aggregatorFunctionSuggestions
                            .add(generateFunctionSuggestion(function.getName(), null,
                                    function.getDescription(), function.getReturnAttributes()));
                }

            }
        }
        return aggregatorFunctionSuggestions;
    }

    public static Map<String, Object> generateFunctionSuggestion
            (String functionName, String[] parameterOverload, String description,
             List<AttributeMetaData> returnAttributes) {
        StringBuilder insertText = new StringBuilder(functionName);
        if (parameterOverload == null) {
            insertText.append("()");
        } else {
            //todo: add snippet support
            insertText.append("(").append(String.join(",", parameterOverload)).append(")");
        }
        StringBuilder descriptionText = new StringBuilder();
        descriptionText.append("functionName: " + functionName + "\n\ndescription: " + description);
        if (returnAttributes != null) {
            descriptionText.append("\n\nreturnAttributes:");
            returnAttributes.forEach(returnAttribute -> {
                descriptionText.append(returnAttribute.getName()).append("\n" + returnAttribute.getDescription());
            });
        }
        Map<String, Object> functionSuggestion = new HashMap<>();
        functionSuggestion.put("insertText", insertText.toString());
        functionSuggestion.put("label", insertText.toString());
        functionSuggestion.put("completionItemKind", "Function");
        functionSuggestion.put("detail", descriptionText.toString());
        functionSuggestion.put("filterText", insertText.toString());
        functionSuggestion.put("insertTextFormat", "Snippet");
        return functionSuggestion;
    }

    public static List<Map<String, Object>> getStoreAnnotations() {
        if (storeSuggestions.isEmpty()) {
            List<ProcessorMetaData> stores = LSCompletionContext.INSTANCE.getMetaDataProvider().getStores();
            for (ProcessorMetaData store : stores) {
                List<ParameterMetaData> parameters = store.getParameters().stream().filter(mandatoryPredicate).collect(
                        Collectors.toList());
                storeSuggestions.add(generateAnnotation(store.getName(), store.getNamespace(), parameters,
                        store.getDescription()));
            }
        }
        return storeSuggestions;
    }

    public static List<Map<String, Object>> getSinkAnnotations() {
        if (sinkSuggestions.isEmpty()) {
            List<ProcessorMetaData> sinks = LSCompletionContext.INSTANCE.getMetaDataProvider().getSinks();
            for (ProcessorMetaData sink : sinks) {
                List<ParameterMetaData> parameters = sink.getParameters().stream().filter(mandatoryPredicate).collect(
                        Collectors.toList());
                sinkSuggestions.add(generateAnnotation(sink.getName(), sink.getNamespace(), parameters,
                        sink.getDescription()));
            }
        }
        return sinkSuggestions;
    }

    public static List<Map<String, Object>> getSourceAnnotations() {
        if (sourceSuggestions.isEmpty()) {
            List<ProcessorMetaData> sources = LSCompletionContext.INSTANCE.getMetaDataProvider().getSources();
            for (ProcessorMetaData source : sources) {
                List<ParameterMetaData> parameters = source.getParameters().stream().filter(mandatoryPredicate).collect(
                        Collectors.toList());
                sourceSuggestions.add(generateAnnotation(source.getName(), source.getNamespace(), parameters,
                        source.getDescription()));
            }
        }
        return sourceSuggestions;
    }

    private static Predicate<ParameterMetaData> mandatoryPredicate =
            (parameterMetaData -> parameterMetaData.getOptional());

    public static Map<String, Object> generateAnnotation
            (String name, String nameSpace, List<ParameterMetaData> parameters, String description) {
        StringBuilder insertText = new StringBuilder("@" + nameSpace + "( " + "type = " + "\"" + name + "\"");
        parameters.forEach(parameter -> {
            insertText.append(", " + parameter.getName() + " = " + "\"" + parameter.getDefaultValue() + "\"");
        });
        insertText.append(")");
        Map<String, Object> annotationSuggestion = new HashMap<>();
        annotationSuggestion.put("insertText", insertText.toString());
        annotationSuggestion.put("label", insertText.toString());
        annotationSuggestion.put("completionItemKind", "Snippet");
        annotationSuggestion.put("detail", description);
        annotationSuggestion.put("filterText", insertText.toString());
        annotationSuggestion.put("insertTextFormat", "Snippet");
        return annotationSuggestion;
    }

    public static List<Map<String, Object>> generateAttributeReferences(Map<String, List<String>> attributeNameMap) {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        for (Map.Entry entry : attributeNameMap.entrySet()) {
            String source = (String) entry.getKey();
            List<String> values = (ArrayList) entry.getValue();
            if (attributeNameMap.size() == 1) {
                values.forEach(value -> {
                    Map<String, Object> suggestion = new HashMap<>();
                    suggestion.put("insertText", value);
                    suggestion.put("label", value);
                    suggestion.put("completionItemKind", "Reference");
                    suggestion.put("detail", "attribute-reference");
                    suggestion.put("filterText", value);
                    suggestion.put("insertTextFormat", "PlainText");
                    suggestions.add(suggestion);
                });
            } else {
                values.forEach(value -> {
                    Map<String, Object> suggestion = new HashMap<>();
                    suggestion.put("insertText", source + "." + value);
                    suggestion.put("label", source + "." + value);
                    suggestion.put("completionItemKind", "Reference");
                    suggestion.put("detail", "attribute-reference");
                    suggestion.put("filterText", source + "." + value);
                    suggestion.put("insertTextFormat", "PlainText");
                    suggestions.add(suggestion);
                });
            }
        }
        return suggestions;
    }

    public static List<Map<String, Object>> generateSourceReferences(List<String> sources) {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        for (String source : sources) {
            Map<String, Object> suggestion = new HashMap<>();
            suggestion.put("insertText", source);
            suggestion.put("label", source);
            suggestion.put("completionItemKind", "Reference");
            suggestion.put("detail", "source");
            suggestion.put("filterText", source);
            suggestion.put("insertTextFormat", "PlainText");
            suggestions.add(suggestion);
        }
        return suggestions;
    }

    public static List<Map<String, Object>> generatePartitionKeys(Map<String, List<String>> attributeMap) {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        for (Map.Entry entry : attributeMap.entrySet()) {
            String key = (String) entry.getKey();
            List<String> attributes = attributeMap.get(key);
            for (String attribute : attributes) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("insertText", attribute + " of " + key);
                suggestion.put("label", attribute + " of " + key);
                suggestion.put("completionItemKind", "Reference");
                suggestion.put("detail", "key-selection");
                suggestion.put("filterText", attribute);
                suggestion.put("insertTextFormat", "PlainText");
                suggestions.add(suggestion);
            }

        }
        return suggestions;
    }

    static {
        Map<String, Object> obj = null;
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = SnippetBlockUtil.class
                    .getClassLoader()
                    .getResourceAsStream("snippets.yaml");
            obj = yaml.load(inputStream);
        } catch (YAMLException ignored) {
            //todo: log the error when the logging framework is developed.
        } finally {
            SNIPPETS = Collections.unmodifiableMap(obj instanceof Map ?
                    (Map<String, Map<String, Object>>) obj.get("snippets")
                    : new HashMap<>(0));
        }
        QUERY_SECTION_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
                SNIPPETS.get("KEYWORD_GROUP_BY"),
                SNIPPETS.get("KEYWORD_OFFSET"),
                SNIPPETS.get("KEYWORD_HAVING"),
                SNIPPETS.get("KEYWORD_ORDER_BY"),
                SNIPPETS.get("KEYWORD_LIMIT"),
                SNIPPETS.get("KEYWORD_INSERT"),
                SNIPPETS.get("KEYWORD_INSERT_INTO"),
                SNIPPETS.get("KEYWORD_DELETE"),
                SNIPPETS.get("KEYWORD_UPDATE_OR_INSERT_INTO"),
                SNIPPETS.get("KEYWORD_RETURN"),
                SNIPPETS.get("KEYWORD_UPDATE")));
        ATTRIBUTE_TYPES = Collections.unmodifiableList(Arrays.asList(
                SNIPPETS.get("KEYWORD_STRING"),
                SNIPPETS.get("KEYWORD_INT"),
                SNIPPETS.get("KEYWORD_FLOAT"),
                SNIPPETS.get("KEYWORD_DOUBLE"),
                SNIPPETS.get("KEYWORD_LONG"),
                SNIPPETS.get("KEYWORD_BOOL"),
                SNIPPETS.get("KEYWORD_OBJECT")));
        AGGREGATION_TIME_DURATION = Collections.unmodifiableList(Arrays.asList(
                SNIPPETS.get("SECONDS"),
                SNIPPETS.get("MINUTES"),
                SNIPPETS.get("HOURS"),
                SNIPPETS.get("DAYS"),
                SNIPPETS.get("MONTHS"),
                SNIPPETS.get("YEARS")));
        ORDER_KEYWORDS = Collections.unmodifiableList(Arrays.asList(
                SNIPPETS.get("KEYWORD_ASC"),
                SNIPPETS.get("KEYWORD_DESC")));
        TIME_VALUE = Collections.unmodifiableList(Arrays.asList(
                SNIPPETS.get("SECONDS"),
                SNIPPETS.get("MINUTES"),
                SNIPPETS.get("HOURS"),
                SNIPPETS.get("DAYS"),
                SNIPPETS.get("MONTHS"),
                SNIPPETS.get("YEARS"),
                SNIPPETS.get("MILLISECONDS")));
        BOOLEAN_CONSTANTS = Collections.unmodifiableList(Arrays.asList(
                SNIPPETS.get("CONSTANT_TRUE"),
                SNIPPETS.get("CONSTANT_FALSE")));
    }
}
