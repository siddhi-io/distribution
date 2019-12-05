package io.siddhi.distribution.sample.rabbitmq.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.log4j.Logger;


import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class Client {
    private static final Logger log = Logger.getLogger(Client.class);

    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) {
        log.info("Arg 0: " + args[0]);
        log.info("Arg 1: " + args[1]);
        int noOfThreads = !args[1].equals("EMPTY") ? Integer.parseInt(args[1]) : 1;
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < noOfThreads; i++) {
            executor.execute(new SendMessages());
        }
    }


    private static class SendMessages implements Runnable
    {
        private int count = 0;
        private long startMilSec;
        @Override
        public void run() {
            try {

                int sentEvents = 0;
                ConnectionFactory factory = new ConnectionFactory();
                factory.setUri("amqp://guest:guest@192.168.112.9:5672");
                Connection conn = factory.newConnection();
                Channel channel = conn.createChannel();

                channel.exchangeDeclare("direct", "direct", true);
                channel.queueDeclare("queue", true, false, false, null);
                channel.queueBind("queue", "direct", "direct");
                while (count != 10000) {

                    byte[] messageBodyBytes;
                    String symbol = "Symbol " + sentEvents;
                    float price = sentEvents * 1.1f;
                    long volume = sentEvents * 10;
                    String text = "<events><event>" +
                            "<symbol>" + symbol + "</symbol>" +
                            "<price>" + price + "</price>" +
                            "<volume>" + volume + "</volume>" +
                            "</event></events>";
                    messageBodyBytes = text.getBytes();
                    channel.basicPublish("direct", "direct", null, messageBodyBytes);
                    sentEvents++;
                    calculateThroughput();

                }
                channel.close();
                conn.close();
            } catch (NoSuchAlgorithmException | URISyntaxException | KeyManagementException
                    | IOException | TimeoutException e) {
                System.out.println("Error occurred");
            }
        }

        private void calculateThroughput() {
            count++;
            if (count % 10000 == 1) {
                startMilSec = System.currentTimeMillis();
            }

            if (count % 10000 == 0) {
                long currentMilSec = System.currentTimeMillis();
                log.info("===============================" +Thread.currentThread().getId() + "==========================");
                log.info("***** Processing took " + (currentMilSec - startMilSec + " ms and throughput = " +
                        "" + (1000 * count / ((currentMilSec - startMilSec)))));
            }
        }
    }
}
