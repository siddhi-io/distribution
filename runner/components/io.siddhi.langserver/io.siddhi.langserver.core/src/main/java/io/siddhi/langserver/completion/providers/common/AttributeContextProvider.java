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
import io.siddhi.query.compiler.SiddhiQLParser;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

/**
 * Provide completions for AttributeContext {@link io.siddhi.query.compiler.SiddhiQLParser.AttributeContext}.
 */
public class AttributeContextProvider extends CompletionProvider {

    public AttributeContextProvider() {
        this.providerName = SiddhiQLParser.AttributeContext.class.getName();
    }

    @Override
    public List<CompletionItem> getCompletions() {
        /**
         * Attribute context has only one child in SiddhiQL grammar. Therefore, getCompletions is directed to
         * {@link MathOperationContextProvider}.
         */
        return LSCompletionContext.INSTANCE.getProvider(SiddhiQLParser.Math_operationContext.class.getName())
                .getCompletions();
    }
}
