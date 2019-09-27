package io.siddhi.distribution.sample.grpc.generic.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.siddhi.extension.io.grpc.proto.MyServiceGrpc;
import io.siddhi.extension.io.grpc.proto.Request;
import io.siddhi.extension.io.grpc.proto.Response;
import org.apache.log4j.Logger;

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
