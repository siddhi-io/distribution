/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.siddhi.distribution.sample.grpc.generic.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.siddhi.extension.io.grpc.proto.MyServiceGrpc;
import io.siddhi.extension.io.grpc.proto.Request;
import io.siddhi.extension.io.grpc.proto.Response;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Test Grpc Generic server
 */
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
