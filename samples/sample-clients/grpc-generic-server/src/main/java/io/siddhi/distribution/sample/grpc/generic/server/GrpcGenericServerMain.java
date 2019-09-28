package io.siddhi.distribution.sample.grpc.generic.server;

import java.io.IOException;

public class GrpcGenericServerMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(args[0]);
        GenericServer server = new GenericServer(port);
        server.start();
    }
}
