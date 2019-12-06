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
package io.siddhi.langserver;

import io.siddhi.langserver.diagnostic.DiagnosticProvider;
import io.siddhi.langserver.utils.CommonUtil;
import io.siddhi.langserver.utils.CompletionUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * TextDocumentService implementation for Siddhi provide various services
 * {@link TextDocumentService} upon clients' requests.
 */
public class SiddhiTextDocumentService implements TextDocumentService {
    private DocumentManager documentManager;
    private SiddhiLanguageServer siddhiLanguageServer;
    private DiagnosticProvider diagnosticProvider;

    public SiddhiTextDocumentService() {
        this.documentManager = DocumentManager.INSTANCE;
        this.siddhiLanguageServer = LSOperationContext.INSTANCE.getSiddhiLanguageServer();
        this.diagnosticProvider = LSOperationContext.INSTANCE.getDiagnosticProvider();
    }

    /**
     * The method is called to retrieve completion items upon a request of a client for completions for a given
     * completion parameter combination({@link org.eclipse.lsp4j.CompletionContext}).
     *
     * @param completionParams
     * @return the list of Completion items {@link List<CompletionItem>} or an completionList object
     * {@link CompletionList} is returned.
     */
    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
            CompletionParams completionParams) {
        List<CompletionItem> completions = new ArrayList<>();
        return CompletableFuture.supplyAsync(() -> {
            List<CompletionItem> completionItems;
            String documentUri = completionParams.getTextDocument().getUri();
            Optional<Path> completionPath = CommonUtil.getPathFromURI(documentUri);
            if (!completionPath.isPresent()) {
                return Either.forLeft(completions);
            }
            try {
                completionItems = CompletionUtil.getCompletions(completionParams);
            } catch (URISyntaxException e) {
                String msg = "Operation 'text/completion' failed!: " + e.getMessage();
                completionItems = new ArrayList<>();
            }
            return Either.forLeft(completionItems);
        });
    }

    /**
     * This method is called When a document with .siddhi extension is opened which publishes the diagnostics of the
     * document to the language client {@link org.eclipse.lsp4j.services.LanguageClient}.
     *
     * @param didOpenTextDocumentParams
     */
    @Override
    public void didOpen(DidOpenTextDocumentParams didOpenTextDocumentParams) {
        String documentUri = didOpenTextDocumentParams.getTextDocument().getUri();
        String documentContent = didOpenTextDocumentParams.getTextDocument().getText();
        this.documentManager.openFile(Paths.get(URI.create(documentUri)), documentContent);
        if (siddhiLanguageServer.getClient() != null) {
            this.diagnosticProvider
                    .compileAndSendDiagnostics(siddhiLanguageServer.getClient(), documentUri, documentContent);
        }
    }

    /**
     * This method is called when a document with .siddhi extension is opened which publishes the diagnostics of the
     * document to client {@link org.eclipse.lsp4j.services.LanguageClient}.
     *
     * @param didChangeTextDocumentParams
     */
    @Override
    public void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
        String documentUri = didChangeTextDocumentParams.getTextDocument().getUri();
        try {
            List<TextDocumentContentChangeEvent> changes = didChangeTextDocumentParams.getContentChanges();
            for (TextDocumentContentChangeEvent changeEvent : changes) {
                documentManager.updateFile(Paths.get(URI.create(documentUri)), changeEvent.getText());
            }
            try {
                this.diagnosticProvider.compileAndSendDiagnostics(siddhiLanguageServer.getClient(), documentUri,
                        documentManager.getFileContent(Paths.get(URI.create(documentUri))));
            } catch (Throwable ignored) {
                String msg = "Computing 'diagnostics' failed!";
                //todo: an error message should be logged once the logging framework is integrated.
            }
        } catch (Throwable ignored) {
            String msg = "Operation 'text/didChange' failed!";
            //todo: an error message should be logged once the logging framework is integrated.
        }
    }

    /**
     * This method is called when a document is close and the document instance held by the
     * {@link DocumentManager} is destroyed.
     *
     * @param didCloseTextDocumentParams
     */
    @Override
    public void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) {
        String fileUri = didCloseTextDocumentParams.getTextDocument().getUri();
        this.documentManager.closeFile(Paths.get(URI.create(fileUri)));
    }

    /**
     * This method is called when a document is saved.
     *
     * @param didSaveTextDocumentParams
     */
    @Override
    public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {
        //Operation not supported.
    }
}
