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

package io.siddhi.langserver.completion.providers.siddhiapp;

import io.siddhi.langserver.completion.providers.CompletionProvider;
import io.siddhi.langserver.utils.SnippetBlockUtil;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.eclipse.lsp4j.CompletionItem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Provide completions for SiddhiAppContext {@link io.siddhi.query.compiler.SiddhiQLParser.AnnotationContext}.
 */
public class SiddhiAppContextProvider extends CompletionProvider {

    public SiddhiAppContextProvider() {
        this.providerName = SiddhiQLParser.Siddhi_appContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        List<Map<String, Object>> suggestions = Arrays.asList(
                SnippetBlockUtil.SNIPPETS.get("APP_DESCRIPTION_ANNOTATION_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("APP_NAME_ANNOTATION_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("STREAM_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("PARTITION_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("TABLE_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("TRIGGER_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("WINDOW_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("AGGREGATION_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("ANNOTATION_ASYNC_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("ANNOTATION_INDEX_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("ANNOTATION_PRIMARY_KEY_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("ANNOTATION_QUERY_INFO_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("APP_STATISTICS_ANNOTATION_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("QUERY_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("QUERY_FILTER"),
                SnippetBlockUtil.SNIPPETS.get("QUERY_JOIN"),
                SnippetBlockUtil.SNIPPETS.get("QUERY_PATTERN"),
                SnippetBlockUtil.SNIPPETS.get("QUERY_TABLE_JOIN"),
                SnippetBlockUtil.SNIPPETS.get("QUERY_WINDOW"),
                SnippetBlockUtil.SNIPPETS.get("QUERY_WINDOW_FILTER"),
                SnippetBlockUtil.SNIPPETS.get("SINK_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("SOURCE_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("FUNCTION_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("KEYWORD_FROM"),
                SnippetBlockUtil.SNIPPETS.get("KEYWORD_DEFINE"),
                SnippetBlockUtil.SNIPPETS.get("KEYWORD_STREAM"),
                SnippetBlockUtil.SNIPPETS.get("KEYWORD_AGGREGATION"),
                SnippetBlockUtil.SNIPPETS.get("KEYWORD_FUNCTION"),
                SnippetBlockUtil.SNIPPETS.get("KEYWORD_PARTITION"),
                SnippetBlockUtil.SNIPPETS.get("KEYWORD_TABLE"),
                SnippetBlockUtil.SNIPPETS.get("KEYWORD_WINDOW"),
                SnippetBlockUtil.SNIPPETS.get("KEYWORD_TRIGGER"));
        return generateCompletionList(suggestions);
    }
}
