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
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provide completions for QuerySectionContext {@link io.siddhi.query.compiler.SiddhiQLParser.Query_sectionContext}.
 */
public class QuerySectionContextProvider extends CompletionProvider {

    public QuerySectionContextProvider() {
        this.providerName = SiddhiQLParser.Query_sectionContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        List<CompletionItem> completions;
        List<Map<String, Object>> suggestions;
        ParseTreeMapVisitor parseTreeMapVisitor = LSCompletionContext.INSTANCE.getParseTreeMapVisitor();
        completions = LSCompletionContext.INSTANCE.getProvider(
                SiddhiQLParser.Output_attributeContext.class.getName())
                .getCompletions();
        suggestions = new ArrayList<>();
        ParserRuleContext querySectionContext = (ParserRuleContext) LSCompletionContext.INSTANCE.getParseTreeMap().get(
                SiddhiQLParser.Query_sectionContext.class.getName());
        if (querySectionContext != null) {
            List<ParseTree> outputAttributeContexts =
                    parseTreeMapVisitor.findFromImmediateSuccessors(querySectionContext,
                            SiddhiQLParser.Output_attributeContext.class);
            if (outputAttributeContexts.size() > 0) {
                suggestions.addAll(SnippetBlockUtil.QUERY_SECTION_KEYWORDS);
            }
        } else {
            List<ParseTree> querySectionContextList =
                    parseTreeMapVisitor.findFromImmediateSuccessors(
                            (ParserRuleContext) (LSCompletionContext.INSTANCE.getCurrentContext().parent),
                            SiddhiQLParser.Query_sectionContext.class);
            if (querySectionContextList.size() == 1) {
                List<ParseTree> outputAttributeContexts =
                        parseTreeMapVisitor
                                .findFromImmediateSuccessors((ParserRuleContext) querySectionContextList.get(0),
                                        SiddhiQLParser.Output_attributeContext.class);
                if (outputAttributeContexts.size() > 0) {
                    suggestions.addAll(SnippetBlockUtil.QUERY_SECTION_KEYWORDS);
                }
            }
        }
        completions.addAll(generateCompletionList(suggestions));
        return completions;
    }
}
