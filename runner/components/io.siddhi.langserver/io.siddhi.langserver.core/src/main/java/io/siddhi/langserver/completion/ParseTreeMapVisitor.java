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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Visits parse tree to find a given context or a set of contexts.
 */
public class ParseTreeMapVisitor {

    private List<ParseTree> successors;
    private Class context;
    private static final ParseTreeMapVisitor INSTANCE = new ParseTreeMapVisitor();

    private ParseTreeMapVisitor() {
        successors = new ArrayList<>();
    }

    public static ParseTreeMapVisitor getInstance() {
        return INSTANCE;
    }

    public List<ParseTree> findSuccessorContexts(ParserRuleContext rootContext, Class context) {
        successors = new ArrayList<>();
        this.context = context;
        if (rootContext.getChildCount() != 0) {
            visitChildren(rootContext);
        }
        return this.successors;
    }

    public ParseTree findOneFromSuccessors(ParserRuleContext parentContext, Class descendantContext) {
        Queue<ParseTree> queue = new ArrayDeque<>();
        Set<ParseTree> alreadyVisited = new HashSet<>();
        if (parentContext.getChildCount() > 0) {
            queue.addAll(parentContext.children);
            ParseTree currentChild;
            currentChild = queue.remove();
            while (!queue.isEmpty()) {
                if (currentChild.getClass().equals(descendantContext)) {
                    return currentChild;
                } else {
                    alreadyVisited.add(currentChild);
                    if (currentChild instanceof ParserRuleContext && currentChild.getChildCount() > 0) {
                        queue.addAll(((ParserRuleContext) currentChild).children);
                    }
                    queue.removeAll(alreadyVisited);
                }

            }
        }
        return null;
    }

    public List<ParseTree> findFromImmediateSuccessors(ParserRuleContext parentContext, Class descendantContext) {
        List<ParseTree> successors = new ArrayList<>();
        if (parentContext.getChildCount() != 0) {
            List<ParserRuleContext> children = parentContext.getRuleContexts(ParserRuleContext.class);
            children.forEach(child -> {
                if (child.getClass().equals(descendantContext)) {
                    successors.add(child);
                }
            });

        }
        return successors;
    }

    public ParseTree findOneFromImmediateSuccessors(ParserRuleContext parentContext, Class childContext) {
        if (parentContext.getChildCount() != 0) {
            List<ParserRuleContext> children = parentContext.getRuleContexts(ParserRuleContext.class);
            for (ParserRuleContext childCtx : children) {
                if (childCtx.getClass().equals(childContext)) {
                    return childCtx;
                }
            }
        }
        return null;
    }

    public void visitChildren(ParserRuleContext ctx) {
        if (ctx.getChildCount() != 0) {
            List<ParseTree> children = ctx.children;
            for (ParseTree childCtx : children) {
                if (childCtx.getClass().equals(this.context)) {
                    this.successors.add(childCtx);
                }
                if (!childCtx.getClass().equals(TerminalNodeImpl.class)) {
                    visitChildren((ParserRuleContext) childCtx);
                }

            }
        }
    }
}
