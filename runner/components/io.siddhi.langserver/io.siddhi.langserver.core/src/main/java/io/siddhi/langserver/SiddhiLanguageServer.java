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

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Siddhi Language Server implementation for Siddhi which  provides language analytic capabilities for Siddhi
 * application development.
 */
public class SiddhiLanguageServer implements LanguageServer {

    private LanguageClient client;
    private SiddhiTextDocumentService textDocumentService;
    private SiddhiWorkspaceService workspaceService;
    private int shutDownStatus = 1;

    public SiddhiLanguageServer() {
        LSOperationContext.INSTANCE.setSiddhiLanguageServer(this);
        this.textDocumentService = new SiddhiTextDocumentService();
        this.workspaceService = new SiddhiWorkspaceService();
    }

    /**
     * Set the client instance to which the diagnostics are pushed.
     *
     * @param languageClient
     */
    public void connect(LanguageClient languageClient) {
        this.client = languageClient;
        //todo: Initiate loggers once the logging framework is implemented.
    }

    /**
     * This method binds the language server with server options.
     *
     * @param initializeParams an object which comprises of initialization options for the language server.
     * @return {@link InitializeResult} object which comprises of the capabilities of the language server.
     */
    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
        final CompletionOptions completionOptions = new CompletionOptions();
        completionOptions.setTriggerCharacters(Arrays.asList("#", "@"));
        final InitializeResult initializedResult = new InitializeResult(new ServerCapabilities());
        initializedResult.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);
        initializedResult.getCapabilities().setCompletionProvider(completionOptions);
        return CompletableFuture.supplyAsync(() -> initializedResult);
    }

    /**
     * Shuts the language server down.
     *
     * @return    private LSCompletionContext completionContext; {@link Object} a new Object instance.
     */
    @Override
    public CompletableFuture<Object> shutdown() {
        this.shutDownStatus = 0;
        return CompletableFuture.supplyAsync(Object::new);
    }

    @Override
    public void exit() {
        System.exit(shutDownStatus);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return this.textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }

    public LanguageClient getClient() {
        return this.client;
    }
}
