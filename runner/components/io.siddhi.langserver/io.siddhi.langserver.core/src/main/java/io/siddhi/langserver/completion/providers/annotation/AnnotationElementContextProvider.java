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
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.lsp4j.CompletionItem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Provide completions for AnnotationElementContext
 * {@link io.siddhi.query.compiler.SiddhiQLParser.Annotation_elementContext}.
 */
public class AnnotationElementContextProvider extends CompletionProvider {

    public AnnotationElementContextProvider() {
        this.providerName = SiddhiQLParser.Annotation_elementContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        //If the parent of the AnnotationElementContext is AppAnnotationContext provide snippets.
        ParserRuleContext parent = getParent();
        if (parent instanceof SiddhiQLParser.App_annotationContext) {
            List<Map<String, Object>> suggestions = Arrays.asList(
                    SnippetBlockUtil.SNIPPETS.get("APP_ANNOTATION_ELEMENT_NAME_DEFINITION"),
                    SnippetBlockUtil.SNIPPETS.get("APP_ANNOTATION_ELEMENT_DESCRIPTION_DEFINITION"),
                    SnippetBlockUtil.SNIPPETS.get("APP_ANNOTATION_ELEMENT_STATISTICS_DEFINITION"));
            return generateCompletionList(suggestions);
        } else {
            //If the ExecutionElement's parent is AnnotationContext there is no defined completions yet.
            return generateCompletionList(null);
        }
    }
}
