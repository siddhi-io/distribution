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

package io.siddhi.langserver.completion.providers.annotation;

import io.siddhi.langserver.completion.providers.CompletionProvider;
import io.siddhi.langserver.utils.SnippetBlockUtil;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Provide completions for AnnotationContext {@link io.siddhi.query.compiler.SiddhiQLParser.AnnotationContext}.
 */
public class AnnotationContextProvider extends CompletionProvider {

    public AnnotationContextProvider() {
        this.providerName = SiddhiQLParser.AnnotationContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        suggestions.addAll(Arrays.asList(SnippetBlockUtil.SNIPPETS.get("ANNOTATION_ASYNC_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("ANNOTATION_INDEX_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("ANNOTATION_PRIMARY_KEY_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("ANNOTATION_QUERY_INFO_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("APP_ANNOTATION_ELEMENT_DESCRIPTION_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("APP_ANNOTATION_ELEMENT_NAME_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("APP_NAME_ANNOTATION_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("APP_STATISTICS_ANNOTATION_DEFINITION"),
                SnippetBlockUtil.SNIPPETS.get("APP_DESCRIPTION_ANNOTATION_DEFINITION")));
        suggestions.addAll(SnippetBlockUtil.getStoreAnnotations());
        suggestions.addAll(SnippetBlockUtil.getSourceAnnotations());
        suggestions.addAll(SnippetBlockUtil.getSinkAnnotations());
        return generateCompletionList(suggestions);
    }
}
