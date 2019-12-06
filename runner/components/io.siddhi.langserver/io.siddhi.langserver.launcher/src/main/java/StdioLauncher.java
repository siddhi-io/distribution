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

import io.siddhi.langserver.SiddhiLanguageServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Launcher for language server.
 */
public class StdioLauncher {

    /**
     * Main method is executed at the classpath to start Siddhi language server.
     *
     * @param args language server/client options
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void main(String args[]) throws InterruptedException, ExecutionException {
        // To avoid logs printed to I/O which breaks LS protocol.
        //Logger.getRootLogger().setLevel(Level.OFF);
        //System's standard input and output is used as the transport for server client communication.
        startServer(System.in, System.out);
    }

    /**
     * Initiates the language server and creates client server connection.
     *
     * @param in  input stream/transport
     * @param out output stream/transport
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private static void startServer(InputStream in, OutputStream out) throws InterruptedException, ExecutionException {
        SiddhiLanguageServer server = new SiddhiLanguageServer();
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);
        LanguageClient client = launcher.getRemoteProxy();
        server.connect(client);
        Future<?> startListening = launcher.startListening();
        startListening.get();
    }
}
