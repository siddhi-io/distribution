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
package io.siddhi.langserver.parser;

import io.siddhi.langserver.visitor.LanguageServerParserErrorStrategy;
import io.siddhi.langserver.visitor.SiddhiQLLSVisitorImpl;
import io.siddhi.query.compiler.SiddhiQLLexer;
import io.siddhi.query.compiler.SiddhiQLParser;
import io.siddhi.query.compiler.exception.SiddhiParserException;
import io.siddhi.query.compiler.internal.SiddhiErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Map;

/**
 * LanguageServerParses the source content and return a parseTreeMap.
 */
public class LanguageServerParser {

    /**
     * Used at the Siddhi Language server to parse source content and obtain a parseTreeMap.
     *
     * @param source
     * @param goalPosition
     * @return
     */
    public static Map<String, ParseTree> parse(String source, int[] goalPosition) {
        ANTLRInputStream input = new ANTLRInputStream(source);
        SiddhiQLLexer lexer = new SiddhiQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SiddhiErrorListener.INSTANCE);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLParser parser = new SiddhiQLParser(tokens);
        parser.setErrorHandler(new LanguageServerParserErrorStrategy());
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        try {
            ParseTree parseTree = parser.parse();
            SiddhiQLLSVisitorImpl visitor = new SiddhiQLLSVisitorImpl(goalPosition);
            parseTree.accept(visitor);
            return visitor.getParseTreeMap();
        } catch (SiddhiParserException ignored) {
            //todo: e has been ignored until it will be written to a log file.
            return ((LanguageServerParserErrorStrategy)
                    parser.getErrorHandler()).getParseTreeMap();

        }
    }
}
