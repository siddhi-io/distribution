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
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@code LSCompletionProvider} CompletionProvider SPI.
 */
public abstract class CompletionProvider {

    protected String providerName;
    public static final String INSERT_TEXT = "insertText";
    public static final String LABEL = "label";
    public static final String COMPLETION_ITEM_KIND = "completionItemKind";
    public static final String DETAIL = "detail";
    public static final String FILTER_TEXT = "filterText";
    public static final String INSERT_TEXT_FORMAT = "insertTextFormat";
    public abstract List<CompletionItem> getCompletions();

    public String getProviderName() {
        return this.providerName;
    }

    public ParserRuleContext getParent() {
        Map<String, ParseTree> map = LSCompletionContext.INSTANCE.getParseTreeMap();
        ParserRuleContext parent = (ParserRuleContext) map.get(this.providerName).getParent();
        return parent;
    }

    public static List<CompletionItem> generateCompletionList(List<Map<String, Object>> suggestions) {
        List<CompletionItem> completionItems = new ArrayList<>();
        if (suggestions != null) {
            for (Map<String, Object> suggestion : suggestions) {
                    try {
                        CompletionItem completionItem = new CompletionItem();
                        completionItem.setInsertText((String) suggestion.get(INSERT_TEXT));
                        completionItem.setLabel((String) suggestion.get(LABEL));
                        completionItem.setKind(CompletionItemKind.valueOf((String) suggestion
                                .get(COMPLETION_ITEM_KIND)));
                        completionItem.setDetail((String) suggestion.get(DETAIL));
                        completionItem.setFilterText((String) suggestion.get(FILTER_TEXT));
                        completionItem.setInsertTextFormat(InsertTextFormat.valueOf((String) suggestion
                                .get(INSERT_TEXT_FORMAT)));
                        completionItems.add(completionItem);
                    } catch (IllegalArgumentException | ClassCastException ignored) {
                        //todo: log the whatever that is dropped, when the logging framework is implemented.
                    }
            }
        }
        return completionItems;
    }
}
