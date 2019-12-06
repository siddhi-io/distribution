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
package io.siddhi.langserver.completion;

import io.siddhi.langserver.beans.LSErrorNode;
import io.siddhi.langserver.completion.providers.CompletionProvider;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * A process local context for language server completion process.
 */
public class LSCompletionContext {

    private Map<String, Integer> position;
    private Map<String, ParseTree> parseTreeMap = null;
    private ParserRuleContext currentParserContext = new ParserRuleContext();
    private TerminalNodeImpl currentTerminalNode = null;
    private LSErrorNode currentErrorNode = null;
    private String sourceContent;
    private MetaDataProvider metaDataProvider;
    private LSCompletionProviderFactory completionProviderFactory;
    private ParseTreeMapVisitor parseTreeMapVisitor;
    public static final LSCompletionContext INSTANCE = new LSCompletionContext();

    private LSCompletionContext() {
        this.position = new HashMap<>(2);
        this.completionProviderFactory = LSCompletionProviderFactory.getInstance();
        this.parseTreeMapVisitor = ParseTreeMapVisitor.getInstance();
        this.metaDataProvider = MetaDataProvider.getInstance();
    }

    public void setPosition(int line, int col) {
        this.position.put("line", line);
        this.position.put("column", col);
    }

    public int[] getPosition() {
        return new int[]{this.position.get("line"), this.position.get("column")};
    }

    /**
     * sets the returned tree traversing pattern and set the current context which is at the bottom of the
     * search path of the returned tree path.
     * Identify the terminal context {@link TerminalNodeImpl} or {@link LSErrorNode} and sets them if available.
     *
     * @param contextTree traverse result of the tree traversal at the Siddhi parser level.
     */
    public void setCompletionContext(Map<String, ParseTree> contextTree) {
        if (contextTree != null) {
            this.parseTreeMap = contextTree;
            if (this.parseTreeMap.containsKey(LSErrorNode.class.getName())) {
                this.currentErrorNode = (LSErrorNode) this.parseTreeMap.get(LSErrorNode.class.getName());
                this.currentParserContext = this.currentErrorNode.getParent();
            } else if (this.parseTreeMap.containsKey(TerminalNodeImpl.class.getName())) {
                this.currentTerminalNode =
                        (TerminalNodeImpl) this.parseTreeMap.get(TerminalNodeImpl.class.getName());
                this.currentParserContext = (ParserRuleContext)
                        this.parseTreeMap.get(this.currentTerminalNode.getParent().getClass().getName());
            } else {
                this.currentParserContext =
                        (ParserRuleContext) this.parseTreeMap.entrySet().toArray()[parseTreeMap.size() - 1];
            }
        }
    }

    public Map<String, ParseTree> getParseTreeMap() {
        return this.parseTreeMap;
    }

    public ParserRuleContext getCurrentContext() {
        return this.currentParserContext;
    }

    public void setCurrentContext(ParserRuleContext currentContext) {
        this.currentParserContext = currentContext;
    }

    public void setSourceContent(String sourceContent) {
        this.sourceContent = sourceContent;
    }

    public String getSourceContent() {
        return this.sourceContent;
    }

    public MetaDataProvider getMetaDataProvider() {
        return this.metaDataProvider;
    }

    public CompletionProvider getCurrentContextProvider() {
        if (this.completionProviderFactory != null) {
            return this.completionProviderFactory.getProvider(currentParserContext.getClass().getName());
        }
        return null;
    }

    public CompletionProvider getProvider(String providerClassName) {
        return this.completionProviderFactory.getProvider(providerClassName);
    }

    public ParseTreeMapVisitor getParseTreeMapVisitor() {
        return parseTreeMapVisitor;
    }
}
