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

package io.siddhi.langserver.completion.providers.executionelement.partition;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provide completions for PartitionWithStreamContext {@link io.siddhi.query.compiler.SiddhiQLParser.AnnotationContext}.
 */
public class PartitionWithStreamContextProvider extends CompletionProvider {

    public PartitionWithStreamContextProvider() {
        this.providerName = SiddhiQLParser.Partition_with_streamContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        ParseTreeMapVisitor parseTreeMapVisitor = LSCompletionContext.INSTANCE.getParseTreeMapVisitor();
        ParserRuleContext siddhiAppContext =
                (ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap()
                        .get(SiddhiQLParser.Siddhi_appContext.class.getName());
        List<ParseTree> streamDefinitionContexts = parseTreeMapVisitor.findSuccessorContexts(siddhiAppContext,
                SiddhiQLParser.Definition_streamContext.class);
        for (Object streamDefinitionContext : streamDefinitionContexts) {
            List<String> attributeNames = new ArrayList<>();
            List<ParseTree> streamIdContext =
                    parseTreeMapVisitor.findSuccessorContexts((ParserRuleContext) streamDefinitionContext,
                            SiddhiQLParser.Stream_idContext.class);
            List<ParseTree> sourceName = parseTreeMapVisitor
                    .findSuccessorContexts((ParserRuleContext) streamIdContext.get(0), TerminalNodeImpl.class);
            Object source1 = sourceName.get(0);
            String source = ((TerminalNodeImpl) source1).getText();
            List<ParseTree> attributeNameContexts = parseTreeMapVisitor
                    .findSuccessorContexts((ParserRuleContext) streamDefinitionContext,
                            SiddhiQLParser.Attribute_nameContext.class);
            for (Object attributeNameContext : attributeNameContexts) {
                String name =
                        ((parseTreeMapVisitor
                                .findSuccessorContexts((ParserRuleContext) attributeNameContext,
                                        TerminalNodeImpl.class).get(0))).getText();
                attributeNames.add(name);
            }
            map.put(source, attributeNames);

        }
        return generateCompletionList(SnippetBlockUtil.generatePartitionKeys(map));
    }
}
