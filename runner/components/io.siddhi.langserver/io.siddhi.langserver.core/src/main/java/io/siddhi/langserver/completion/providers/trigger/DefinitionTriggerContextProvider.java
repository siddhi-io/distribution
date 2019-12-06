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

package io.siddhi.langserver.completion.providers.trigger;

import io.siddhi.langserver.completion.LSCompletionContext;
import io.siddhi.langserver.completion.providers.CompletionProvider;
import io.siddhi.langserver.utils.SnippetBlockUtil;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides completions for DefinitionTriggerContext.
 * {@link io.siddhi.query.compiler.SiddhiQLParser.Definition_triggerContext}.
 */
public class DefinitionTriggerContextProvider extends CompletionProvider {

    public DefinitionTriggerContextProvider() {
        this.providerName = SiddhiQLParser.Definition_triggerContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        ParserRuleContext definitionTriggerContext = (ParserRuleContext)
                LSCompletionContext.INSTANCE.getParseTreeMap()
                        .get(SiddhiQLParser.Definition_triggerContext.class.getName());
        int childCount = definitionTriggerContext.getChildCount();
        ParseTree lastChild = definitionTriggerContext.getChild(childCount - 1);
        if (lastChild instanceof TerminalNodeImpl) {
            if (lastChild.getText().equalsIgnoreCase("at")) {
                suggestions.add(SnippetBlockUtil.SNIPPETS.get("KEYWORD_EVERY"));
            } else {
                suggestions.add(SnippetBlockUtil.SNIPPETS.get("KEYWORD_AT"));
            }
        } else {
            suggestions.add(SnippetBlockUtil.SNIPPETS.get("KEYWORD_AT"));
        }

        return generateCompletionList(suggestions);
    }
}
