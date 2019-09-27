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

package io.siddhi.distribution.sample.grpc.generic.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.siddhi.extension.io.grpc.proto.MyServiceGrpc;
import io.siddhi.extension.io.grpc.proto.Request;
import io.siddhi.extension.io.grpc.proto.Response;
import org.apache.log4j.Logger;

/**
 * This is a sample generic gRpc client to publish events to gRpc endpoint.
 */
public class GrpcGenericClient {
    private static final Logger log = Logger.getLogger(GrpcGenericClient.class);

    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) throws InterruptedException {
        String port = args[0];
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + port).usePlaintext().build();
        int noOfEventsToSend = !args[1].isEmpty() ? Integer.parseInt(args[1]) : -1;
        boolean sendEventsContinuously = noOfEventsToSend == -1;
        int sentEvents = 0;

        while (sendEventsContinuously || sentEvents != noOfEventsToSend) {
            boolean boolValue = sentEvents % 2 == 0;
            Request request = Request.newBuilder()
                    .setStringValue("Request " + (sentEvents + 1))
                    .setIntValue(10 + sentEvents)
                    .setBooleanValue(boolValue)
                    .setDoubleValue(168.45 + sentEvents)
                    .setFloatValue(45.34f + sentEvents)
                    .setLongValue(1000L + sentEvents)
                    .build();
            MyServiceGrpc.MyServiceBlockingStub blockingStub = MyServiceGrpc.newBlockingStub(channel);
            Response response = blockingStub.process(request);
            log.info("\n" + response);
            Thread.sleep(1000);
            sentEvents++;
        }

    }
}
