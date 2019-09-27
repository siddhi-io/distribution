package io.siddhi.distribution.sample.grpc.server;

import java.io.IOException;

public class GrpcRunServer {
    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        Server server = new Server(8889);
        server.start();
    }
}
