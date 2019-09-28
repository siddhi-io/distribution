package io.siddhi.distribution.sample.grpc.server;

import java.io.IOException;

public class GrpcServerMain {
    /**
     * Main method to start the test server.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        int port = Integer.parseInt(args[0]);
        Server server = new Server(port);
        server.start();
    }
}
