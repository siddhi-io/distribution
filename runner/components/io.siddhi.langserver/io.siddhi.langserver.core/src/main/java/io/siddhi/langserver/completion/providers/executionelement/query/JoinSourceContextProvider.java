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

package io.siddhi.langserver.completion.providers.executionelement.query;

import io.siddhi.langserver.completion.LSCompletionContext;
import io.siddhi.langserver.completion.ParseTreeMapVisitor;
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
 * Provider for source join context.
 */
public class JoinSourceContextProvider extends CompletionProvider {

    public JoinSourceContextProvider() {
        this.providerName = SiddhiQLParser.Join_sourceContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        ParseTreeMapVisitor parseTreeMapVisitor = LSCompletionContext.INSTANCE.getParseTreeMapVisitor();
        ParserRuleContext joinStreamContext = getParent();
        List<ParseTree> joinSources = parseTreeMapVisitor.findSuccessorContexts(joinStreamContext,
                SiddhiQLParser.Stream_idContext.class);
        List<Object> sourceNameTerminals = new ArrayList<>();
        List<String> sourceNames = new ArrayList<>();
        for (ParseTree joinSource : joinSources) {
            sourceNameTerminals.addAll(parseTreeMapVisitor.findSuccessorContexts((ParserRuleContext) joinSource,
                    TerminalNodeImpl.class));

        }
        for (Object sourceNameTerminal : sourceNameTerminals) {
            sourceNames.add(((TerminalNodeImpl) sourceNameTerminal).getText());
        }
        List<ParseTree> streamIdContexts = new ArrayList<>();
        List<ParseTree> sourceProviderContexts =
                parseTreeMapVisitor
                        .findSuccessorContexts((ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap().get(
                                SiddhiQLParser.Siddhi_appContext.class.getName()),
                                SiddhiQLParser.Definition_streamContext.class);

        sourceProviderContexts.addAll(parseTreeMapVisitor
                .findSuccessorContexts((ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap().get(
                        SiddhiQLParser.Siddhi_appContext.class.getName()),
                        SiddhiQLParser.Definition_tableContext.class));

        sourceProviderContexts.addAll(parseTreeMapVisitor
                .findSuccessorContexts((ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap().get(
                        SiddhiQLParser.Siddhi_appContext.class.getName()),
                        SiddhiQLParser.Definition_windowContext.class));

        List<ParseTree> definitionAggregationContexts = parseTreeMapVisitor
                .findSuccessorContexts((ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap().get(
                        SiddhiQLParser.Siddhi_appContext.class.getName()),
                        SiddhiQLParser.Definition_aggregationContext.class);

        for (Object sourceProviderContext : sourceProviderContexts) {
            streamIdContexts.addAll(parseTreeMapVisitor
                    .findSuccessorContexts((ParserRuleContext) sourceProviderContext,
                            SiddhiQLParser.Stream_idContext.class));
        }
        for (Object definitionAggregationContext : definitionAggregationContexts) {
            streamIdContexts.addAll(parseTreeMapVisitor
                    .findSuccessorContexts((ParserRuleContext) definitionAggregationContext,
                            SiddhiQLParser.Aggregation_nameContext.class));
        }
        List<String> sources = new ArrayList<>();
        for (Object streamIdContext : streamIdContexts) {
            String source = ((ParserRuleContext) streamIdContext).getText();
            if (!sourceNames.contains(source)) {
                sources.add(source);
            }
        }
        List<Map<String, Object>> suggestions = SnippetBlockUtil.generateSourceReferences(sources);
        return generateCompletionList(suggestions);
    }
}
