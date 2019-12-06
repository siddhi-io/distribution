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

import io.siddhi.langserver.beans.LSErrorNode;
import io.siddhi.langserver.completion.LSCompletionContext;
import io.siddhi.langserver.completion.providers.CompletionProvider;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide completions for queryInputContext {@link io.siddhi.query.compiler.SiddhiQLParser.Query_inputContext}.
 */
public class QueryInputContextProvider extends CompletionProvider {

    public QueryInputContextProvider() {
        this.providerName = SiddhiQLParser.Query_inputContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        //first check whether there is a query input context on context tree and then try to provide completions
        // based on the factor whether  a context exist or not.
        if (LSCompletionContext.INSTANCE.getParseTreeMap().containsKey(LSErrorNode.class.getName())) {
            LSErrorNode errorNode =
                    (LSErrorNode) LSCompletionContext.INSTANCE.getParseTreeMap().get(LSErrorNode.class.getName());
            if (errorNode.getErroneousSymbol().contains("#[")) {
                List<CompletionItem> completions = new ArrayList<>();
                completions.addAll(LSCompletionContext.INSTANCE
                        .getProvider(SiddhiQLParser.FilterContext.class.getName())
                        .getCompletions());
                return completions;
            } else if (errorNode.getErroneousSymbol().contains("#")) {
                List<CompletionItem> completions = new ArrayList<>();
                completions.addAll(LSCompletionContext.INSTANCE
                        .getProvider(SiddhiQLParser.WindowContext.class.getName()).getCompletions());
                completions.addAll(LSCompletionContext.INSTANCE
                        .getProvider(SiddhiQLParser.Stream_functionContext.class.getName()).getCompletions());

                return completions;
            }
        }
        return LSCompletionContext.INSTANCE
                .getProvider(SiddhiQLParser.Standard_streamContext.class.getName())
                .getCompletions();
    }
}
