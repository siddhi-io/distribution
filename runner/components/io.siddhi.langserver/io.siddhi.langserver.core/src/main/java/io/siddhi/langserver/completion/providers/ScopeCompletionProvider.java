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
package io.siddhi.langserver.completion.providers;

import io.siddhi.langserver.completion.LSCompletionContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

/**
 * Provider completions for scopes.
 */
public abstract class ScopeCompletionProvider extends CompletionProvider {

    protected List<String> scopes;

    public abstract List<CompletionItem> getCompletions();

    public ParserRuleContext findPredecessorContext(ParserRuleContext currentContext) {
        if (this.scopes.contains(currentContext.getClass().getName())) {
            return currentContext;
        } else if (currentContext.getParent() != null) {
            return findPredecessorContext(currentContext.getParent());
        } else {
            return null;
        }
    }

    public ParserRuleContext findScope() {
        ParserRuleContext currentContext = LSCompletionContext.INSTANCE.getCurrentContext();
        if (currentContext != null) {
            ParserRuleContext scopeContext = findPredecessorContext(currentContext);
            return scopeContext;
        } else {
            return null;
        }
    }
}
