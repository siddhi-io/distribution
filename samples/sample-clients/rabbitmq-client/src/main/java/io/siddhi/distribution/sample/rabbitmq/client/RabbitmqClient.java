package io.siddhi.distribution.sample.rabbitmq.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class RabbitmqClient {
    private static final Logger log = Logger.getLogger(RabbitmqClient.class);
    private int count = 0;
    private long startMilSec;

    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) throws InterruptedException, NoSuchAlgorithmException,
            KeyManagementException, URISyntaxException, IOException, TimeoutException {
        int noOfEventsToSend = !args[0].equals("EMPTY") ? Integer.parseInt(args[0]) : -1;
        boolean sendEventsContinuously = noOfEventsToSend == -1;
        int sentEvents = 0;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqp://guest:guest@192.168.112.9:5672");
        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();
        RabbitmqClient rabbitmqClient = new RabbitmqClient();

        channel.exchangeDeclare("direct", "direct", true);
        channel.queueDeclare("queue", true, false, false, null);
        channel.queueBind("queue", "direct", "direct");
        String text = "<events><event>" +
                "<symbol>Test 123</symbol>" +
                "<price>" + 100.5f + "</price>" +
                "<volume>" + 100000L + "</volume>" +
                "</event></events>";
        byte[] messageBodyBytes = text.getBytes();

        while (sendEventsContinuously || sentEvents != noOfEventsToSend) {
            channel.basicPublish("direct", "direct", null, messageBodyBytes);
            sentEvents++;
            rabbitmqClient.calculateThroughput();

        }
        channel.close();
        conn.close();
    }

    private void calculateThroughput() {
        count++;
        if (count % 10000 == 1) {
            startMilSec = System.currentTimeMillis();
            log.info("Start Millisecond :" + startMilSec);
        }

        if (count % 10000 == 0) {
            long currentMilSec = System.currentTimeMillis();
            log.info((currentMilSec - startMilSec) / count);
            log.info("current Millisecond :" + currentMilSec);
            log.info("Processing took " + (currentMilSec - startMilSec) + " ms and throughput = " +
                    (1000 * 10000 / ((currentMilSec - startMilSec))));
            count = 0;
        }
    }
}
