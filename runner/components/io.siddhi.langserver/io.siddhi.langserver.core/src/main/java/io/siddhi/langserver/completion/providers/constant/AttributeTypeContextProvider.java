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

package io.siddhi.langserver.completion.providers.constant;

import io.siddhi.langserver.completion.LSCompletionContext;
import io.siddhi.langserver.completion.providers.CompletionProvider;
import io.siddhi.langserver.utils.SnippetBlockUtil;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides completions for AttributeTypeContext {@link io.siddhi.query.compiler.SiddhiQLParser.Attribute_typeContext}.
 */
public class AttributeTypeContextProvider extends CompletionProvider {

    public AttributeTypeContextProvider() {

        this.providerName = SiddhiQLParser.Attribute_typeContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {

        List<Map<String, Object>> suggestions = SnippetBlockUtil.ATTRIBUTE_TYPES;
        return generateCompletionList(suggestions);
    }

    /**
     * Provides completions for GroupByContext.
     * {@link SiddhiQLParser.Group_byContext}.
     */
    public static class GroupByContextProvider extends CompletionProvider {

        public GroupByContextProvider() {

            this.providerName = SiddhiQLParser.Group_byContext.class.getName();
        }

        @Override
        public List<CompletionItem> getCompletions() {

            List<CompletionItem> completions = LSCompletionContext.INSTANCE
                    .getProvider(SiddhiQLParser.Attribute_referenceContext.class.getName()).getCompletions();
            if (getParent() instanceof SiddhiQLParser.Group_by_query_selectionContext) {
                if (getParent().getParent() instanceof SiddhiQLParser.Definition_aggregationContext) {
                    List<Map<String, Object>> suggestions = new ArrayList<>();
                    suggestions.add(SnippetBlockUtil.SNIPPETS.get("KEYWORD_AGGREGATE_BY"));
                    completions.addAll(generateCompletionList(suggestions));
                }
            }
            return completions;
        }
    }
}
