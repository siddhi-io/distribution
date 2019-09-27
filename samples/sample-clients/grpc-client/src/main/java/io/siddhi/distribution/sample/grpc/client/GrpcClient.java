package io.siddhi.distribution.sample.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.log4j.Logger;
import org.wso2.grpc.Event;
import org.wso2.grpc.EventServiceGrpc;

public class GrpcClient {
    private static final Logger log = Logger.getLogger(GrpcClient.class);

    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) throws InterruptedException {
        String port = args[0];
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:" + port).usePlaintext().build();
        Event.Builder requestBuilder = Event.newBuilder();
        int noOfEventsToSend = !args[1].isEmpty() ? Integer.parseInt(args[1]) : -1;
        boolean sendEventsContinuously = noOfEventsToSend == -1;
        int sentEvents = 0;

        while (sendEventsContinuously || sentEvents != noOfEventsToSend) {
            String json = "{ \"message\": \"Request " + (sentEvents + 1) + "\"}";
            requestBuilder.setPayload(json);
            requestBuilder.putHeaders("stream.id", "FooStream");
            Event sequenceCallRequest = requestBuilder.build();
            EventServiceGrpc.EventServiceBlockingStub blockingStub = EventServiceGrpc.newBlockingStub(channel);
            Event response = blockingStub.process(sequenceCallRequest);
            log.info(response);
            Thread.sleep(1000);
            sentEvents++;
        }
    }
}
