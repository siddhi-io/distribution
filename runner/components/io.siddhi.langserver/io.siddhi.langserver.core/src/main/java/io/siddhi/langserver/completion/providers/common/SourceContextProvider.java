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
import io.siddhi.langserver.completion.ParseTreeMapVisitor;
import io.siddhi.langserver.completion.providers.CompletionProvider;
import io.siddhi.langserver.utils.SnippetBlockUtil;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provide completions for AttributeReferenceContext {@link io.siddhi.query.compiler.SiddhiQLParser.AnnotationContext}.
 */
public class SourceContextProvider extends CompletionProvider {

    public SourceContextProvider() {
        this.providerName = SiddhiQLParser.SourceContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        ParseTreeMapVisitor parseTreeMapVisitor = LSCompletionContext.INSTANCE.getParseTreeMapVisitor();
        ParseTree parentContext = LSCompletionContext.INSTANCE.getParseTreeMap()
                .get(SiddhiQLParser.SourceContext.class.getName())
                .getParent();
        if (parentContext instanceof SiddhiQLParser.Definition_streamContext) {
            List<Map<String, Object>> suggestions = new ArrayList<>();
            suggestions.add(SnippetBlockUtil.SNIPPETS.get("STREAM_NAME_SNIPPET"));
            return generateCompletionList(suggestions);
        } else if (parentContext instanceof SiddhiQLParser.Definition_windowContext) {
            List<Map<String, Object>> suggestions = new ArrayList<>();
            suggestions.add(SnippetBlockUtil.SNIPPETS.get("WINDOW_NAME_SNIPPET"));
            return generateCompletionList(suggestions);

        } else if (parentContext instanceof SiddhiQLParser.Definition_tableContext) {
            List<Map<String, Object>> suggestions = new ArrayList<>();
            suggestions.add(SnippetBlockUtil.SNIPPETS.get("TABLE_NAME_SNIPPET"));
            return generateCompletionList(suggestions);
        } else if (parentContext instanceof SiddhiQLParser.Join_sourceContext) {
            return generateCompletionList(null);
        } else if (parentContext instanceof SiddhiQLParser.TargetContext) {
            List<ParseTree> sourceContexts = null;
            List<String> sourceNames = new ArrayList<>();
            List<ParseTree> sourceProviderContexts = new ArrayList<>();

            sourceProviderContexts.addAll(parseTreeMapVisitor
                    .findSuccessorContexts((ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap().get(
                            SiddhiQLParser.Siddhi_appContext.class.getName()),
                            SiddhiQLParser.Definition_streamContext.class));

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
                sourceContexts = parseTreeMapVisitor
                        .findSuccessorContexts((ParserRuleContext) sourceProviderContext,
                                SiddhiQLParser.SourceContext.class);
            }
            for (Object definitionAggregationContext : definitionAggregationContexts) {
                sourceContexts = parseTreeMapVisitor
                        .findSuccessorContexts((ParserRuleContext) definitionAggregationContext,
                                SiddhiQLParser.Aggregation_nameContext.class);
            }
            if (sourceContexts != null) {
                sourceContexts.forEach(sourceContext -> {
                    sourceNames.add(sourceContext.getText());
                });
            }
            return generateCompletionList(SnippetBlockUtil.generateSourceReferences(sourceNames));
        } else {
            List<ParseTree> streamIdContexts = new ArrayList<>();
            List<String> sources = new ArrayList<>();
            List<ParseTree> definitionStreamContexts =
                    parseTreeMapVisitor.findSuccessorContexts(
                            (ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap().get(
                                    SiddhiQLParser.Siddhi_appContext.class.getName()),
                            SiddhiQLParser.Definition_streamContext.class);

            for (Object definitionStreamContext : definitionStreamContexts) {
                streamIdContexts.addAll(parseTreeMapVisitor
                        .findSuccessorContexts((ParserRuleContext) definitionStreamContext,
                                SiddhiQLParser.Stream_idContext.class));
            }
            for (Object streamIdContext : streamIdContexts) {
                sources.add(((ParserRuleContext) streamIdContext).getText());
            }
            List<Map<String, Object>> suggestions = SnippetBlockUtil.generateSourceReferences(sources);
            return generateCompletionList(suggestions);
        }

    }

    public List<String> getDefinedSources() {
        List<ParseTree> sourceProviderContexts;
        List<ParseTree> sources = new ArrayList<>();
        List<String> sourceNames = new ArrayList<>();
        ParseTreeMapVisitor parseTreeMapVisitor = LSCompletionContext.INSTANCE.getParseTreeMapVisitor();
        ParserRuleContext siddhiAppContext =
                (ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap()
                        .get(SiddhiQLParser.Siddhi_appContext.class.getName());
        sourceProviderContexts = parseTreeMapVisitor.findSuccessorContexts(siddhiAppContext,
                SiddhiQLParser.Definition_aggregationContext.class);
        sourceProviderContexts.addAll(parseTreeMapVisitor.findSuccessorContexts(siddhiAppContext,
                SiddhiQLParser.Definition_streamContext.class));
        sourceProviderContexts.addAll(parseTreeMapVisitor.findSuccessorContexts(siddhiAppContext,
                SiddhiQLParser.Definition_windowContext.class));
        sourceProviderContexts.addAll(parseTreeMapVisitor.findSuccessorContexts(siddhiAppContext,
                SiddhiQLParser.Definition_aggregationContext.class));
        sourceProviderContexts.forEach(sourceProviderContext -> {
            if (sourceProviderContext instanceof SiddhiQLParser.Definition_aggregationContext) {
                sources.addAll(
                        parseTreeMapVisitor.findSuccessorContexts((ParserRuleContext) sourceProviderContext,
                                SiddhiQLParser.Aggregation_nameContext.class));
            } else {
                sources.addAll(
                        parseTreeMapVisitor.findSuccessorContexts((ParserRuleContext) sourceProviderContext,
                                SiddhiQLParser.SourceContext.class));
            }

        });
        sources.forEach(source -> {
            sourceNames.add(source.getText());
        });
        return sourceNames;
    }

    public List<CompletionItem> getDefaultCompletions() {
        ParseTreeMapVisitor parseTreeMapVisitor = LSCompletionContext.INSTANCE.getParseTreeMapVisitor();
        List<Object> streamIdContexts = new ArrayList<>();
        List<String> sources = new ArrayList<>();
        List<ParseTree> definitionStreamContexts = parseTreeMapVisitor.findSuccessorContexts(
                (ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap()
                        .get(SiddhiQLParser.Siddhi_appContext.class.getName()),
                SiddhiQLParser.Definition_streamContext.class);

        for (ParseTree definitionStreamContext : definitionStreamContexts) {
            streamIdContexts.addAll(parseTreeMapVisitor
                    .findSuccessorContexts((ParserRuleContext) definitionStreamContext,
                            SiddhiQLParser.Stream_idContext.class));
        }

        for (Object streamIdContext : streamIdContexts) {
            sources.add(((ParserRuleContext) streamIdContext).getText());
        }
        List<Map<String, Object>> suggestions = SnippetBlockUtil.generateSourceReferences(sources);
        return generateCompletionList(suggestions);
    }
}
