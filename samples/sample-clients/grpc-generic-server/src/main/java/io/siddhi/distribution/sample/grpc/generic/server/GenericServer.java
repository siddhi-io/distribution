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
import io.siddhi.distribution.sample.grpc.Sweet;
import io.siddhi.distribution.sample.grpc.SweetServiceGrpc;
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
                .addService(new SweetServiceGrpc.SweetServiceImplBase() {
                    @Override
                    public void getDiscount(Sweet request, StreamObserver<Sweet> responseObserver) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Server hits with request :\n" + request);
                        }
                        double discountPrice = request.getPrice() * 0.9; //give 10% discount
                        Sweet response = Sweet.newBuilder()
                                .setName(request.getName())
                                .setPrice(discountPrice)
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
