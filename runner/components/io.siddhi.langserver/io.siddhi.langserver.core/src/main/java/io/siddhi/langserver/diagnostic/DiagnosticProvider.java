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
package io.siddhi.langserver.diagnostic;

import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.langserver.LSOperationContext;
import io.siddhi.query.api.exception.SiddhiAppContextException;
import io.siddhi.query.compiler.exception.SiddhiParserException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Pushes diagnostics provided by SiddhiManager to client.
 */
public class DiagnosticProvider {

    private static final DiagnosticProvider INSTANCE = new DiagnosticProvider();

    private DiagnosticProvider() { }

    public static DiagnosticProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Validate a particular Siddhi app and publish diagnostics received to the known client.
     *
     * @param client        Known client that has been connected with the language server.
     * @param fileUri       File uri of the file which contains the validated Siddhi app.
     * @param sourceContent siddhi App content.
     */
    public void compileAndSendDiagnostics(LanguageClient client, String fileUri, String sourceContent) {
        try {
            LSOperationContext.INSTANCE.getSiddhiManager().createSiddhiAppRuntime(sourceContent);
            List<Diagnostic> diagnostics = new ArrayList<>();
            client.publishDiagnostics(new PublishDiagnosticsParams(fileUri, diagnostics));
        } catch (SiddhiAppCreationException | SiddhiParserException exception) {
            client.publishDiagnostics(new PublishDiagnosticsParams(fileUri, generateDiagnostics(exception)));
        }
    }

    /**
     * generate a list of diagnostics {@link Diagnostic} to be pushed into client.
     *
     * @param exception Validation result.
     * @return {@link List<Diagnostic>} A list of Diagnostics.
     */
    private List<Diagnostic> generateDiagnostics(SiddhiAppContextException exception) {
        List<Diagnostic> clientDiagnostics = new ArrayList<>();
        // LSP diagnostics range is 0 index based
        int startLine =
                (exception.getQueryContextStartIndex() != null) ? exception.getQueryContextStartIndex()[0] - 1 : 0;
        int startChar =
                (exception.getQueryContextStartIndex() != null) ? exception.getQueryContextStartIndex()[1] - 1 : 0;
        int endLine =
                (exception.getQueryContextEndIndex() != null) ? exception.getQueryContextEndIndex()[0] - 1 : 0;
        int endChar =
                (exception.getQueryContextEndIndex() != null) ? exception.getQueryContextEndIndex()[1] - 1 : 50;
        if (exception.getQueryContextStartIndex() != null && exception.getQueryContextEndIndex() != null) {
            startLine = (startLine < 0) ? startLine + 1 : startLine;
            startChar = (startChar < 0) ? startChar + 1 : startChar;
            endLine = (endLine <= 0) ? startLine : endLine;
            endChar = (endChar <= 0) ? startChar + 1 : endChar;
        }
        Range range = new Range(new Position(startLine, startChar), new Position(endLine, endChar));
        Diagnostic diagnostic = new Diagnostic(range, exception.getMessageWithOutContext());
        diagnostic.setSeverity(DiagnosticSeverity.Error);
        clientDiagnostics.add(diagnostic);
        return clientDiagnostics;
    }
}
