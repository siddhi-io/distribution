package io.siddhi.distribution.sample.grpc.generic.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.siddhi.extension.io.grpc.proto.MyServiceGrpc;
import io.siddhi.extension.io.grpc.proto.Request;
import io.siddhi.extension.io.grpc.proto.Response;
import org.apache.log4j.Logger;

import java.io.IOException;

public class GenericServer {
    private static final Logger logger = Logger.getLogger(GenericServer.class.getName());
    private Server server;
    private int port;

    public GenericServer(int port) {
        this.port = port;
    }

    public void start() throws IOException, InterruptedException {
        if (server != null) {
            throw new IllegalStateException("Already started");
        }
        server = ServerBuilder
                .forPort(port)
                .addService(new MyServiceGrpc.MyServiceImplBase() {

                    @Override
                    public void process(Request request, StreamObserver<Response> responseObserver) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Server hits with request :\n" + request);
                        }
                        Response response = Response.newBuilder()
                                .setIntValue(request.getIntValue())
                                .setStringValue("Hello from Server!")
                                .setDoubleValue(request.getDoubleValue())
                                .setLongValue(request.getLongValue())
                                .setBooleanValue(request.getBooleanValue())
                                .setFloatValue(request.getFloatValue())
                                .build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    }
                }).build();

        server.start();
        if (logger.isDebugEnabled()) {
            logger.debug("Generic Server started");
        }
        server.awaitTermination();
    }

}
