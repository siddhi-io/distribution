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

package io.siddhi.langserver.completion.providers.common;

import io.siddhi.langserver.completion.LSCompletionContext;
import io.siddhi.langserver.completion.providers.CompletionProvider;
import io.siddhi.langserver.completion.providers.ScopeCompletionProvider;
import io.siddhi.langserver.utils.SnippetBlockUtil;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Provides completions for FunctionOperationContext.
 * {@link io.siddhi.query.compiler.SiddhiQLParser.Function_operationContext}.
 */
public class FunctionOperationContextProvider extends ScopeCompletionProvider {

    public FunctionOperationContextProvider() {
        this.scopes = new ArrayList<>();
        this.scopes.add(SiddhiQLParser.Definition_windowContext.class.getName());
        this.scopes.add(SiddhiQLParser.Definition_aggregationContext.class.getName());
        this.scopes.add(SiddhiQLParser.QueryContext.class.getName());
        this.scopes.add(SiddhiQLParser.Query_outputContext.class.getName());
        this.providerName = SiddhiQLParser.Function_operationContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        ParserRuleContext scopeContext = findScope();
        if (scopeContext != null) {
            if (scopeContext instanceof SiddhiQLParser.Definition_aggregationContext) {
                suggestions.addAll(SnippetBlockUtil.getAggregatorFunctions());
                return generateCompletionList(suggestions);
            } else if (scopeContext instanceof SiddhiQLParser.QueryContext) {
                suggestions.addAll((Collection<? extends Map<String, Object>>) SnippetBlockUtil.getFunctions());
                return generateCompletionList(suggestions);
            }
            ParserRuleContext functionOperationContext =
                    (ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap().get(providerName);
            if (functionOperationContext != null) {
                List<ParseTree> contexts = LSCompletionContext.INSTANCE.getParseTreeMapVisitor()
                        .findFromImmediateSuccessors(functionOperationContext, SiddhiQLParser.Function_idContext.class);
                if (contexts.size() == 1) {
                    CompletionProvider attributeListContextProvider = LSCompletionContext.INSTANCE
                            .getProvider(SiddhiQLParser.Attribute_listContext.class.getName());
                    return attributeListContextProvider.getCompletions();
                } else if (contexts.size() == 0) {
                    suggestions.addAll(SnippetBlockUtil.getFunctions());
                }
            }
        }
        return generateCompletionList(suggestions);
    }

}
