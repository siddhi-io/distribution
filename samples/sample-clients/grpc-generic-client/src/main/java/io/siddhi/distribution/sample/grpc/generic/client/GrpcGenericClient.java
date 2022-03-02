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
import io.siddhi.distribution.sample.grpc.Sweet;
import io.siddhi.distribution.sample.grpc.SweetServiceGrpc;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a sample generic gRPC client to publish events to gRPC endpoint.
 */
public class GrpcGenericClient {
    private static final Logger log = Logger.getLogger(GrpcGenericClient.class);
    private static List<SweetProduct> listOfSweets = new ArrayList<>();

    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) throws InterruptedException {
        String port = args[0];
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + port).usePlaintext().build();
        int noOfEventsToSend = !args[1].equals("EMPTY") ? Integer.parseInt(args[1]) : -1;
        boolean sendEventsContinuously = noOfEventsToSend == -1;
        int sentEvents = 0;
        addSweets();

        while (sendEventsContinuously || sentEvents != noOfEventsToSend) {
            SweetProduct sweetProduct = listOfSweets.get((int) Math.floor(Math.random() * listOfSweets.size()));
            Sweet request = Sweet.newBuilder()
                    .setName(sweetProduct.name)
                    .setPrice(sweetProduct.price)
                    .build();
            SweetServiceGrpc.SweetServiceBlockingStub blockingStub = SweetServiceGrpc.newBlockingStub(channel);
            Sweet response = blockingStub.getDiscount(request);
            log.info("\n" + response);
            Thread.sleep(1000);
            sentEvents++;
        }

    }

    private static void addSweets() {
        listOfSweets.add(new SweetProduct("Cake", 150));
        listOfSweets.add(new SweetProduct("Toffee", 10));
        listOfSweets.add(new SweetProduct("Ice-Cream", 56));
        listOfSweets.add(new SweetProduct("Cookie", 8));
        listOfSweets.add(new SweetProduct("Doughnut", 20));
        listOfSweets.add(new SweetProduct("Cupcake", 15));
        listOfSweets.add(new SweetProduct("Brownie", 60));
        listOfSweets.add(new SweetProduct("Apple-Pie", 35));
        listOfSweets.add(new SweetProduct("Macaroon", 25));
        listOfSweets.add(new SweetProduct("Marshmallow", 5));
        listOfSweets.add(new SweetProduct("Muffins", 18));
        listOfSweets.add(new SweetProduct("Nougat", 40));
        listOfSweets.add(new SweetProduct("Pudding", 80));
        listOfSweets.add(new SweetProduct("Jelly", 55));
        listOfSweets.add(new SweetProduct("Milkshake", 95));

    }

    private static class SweetProduct {
        private String name;
        private double price;

        public SweetProduct(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }
}
