/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.siddhi.distribution.test.framework.util;

import io.nats.streaming.Message;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import io.nats.streaming.Subscription;
import io.nats.streaming.SubscriptionOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Nats client util to connect to a nats streaming server and pulish/subscribe.
 */
public class NatsClient {
    private String cluserId;
    private String clientId;
    private String natsUrl;
    private ResultHolder resultHolder;
    private StreamingConnectionFactory streamingConnectionFactory;
    private StreamingConnection streamingConnection;
    private Subscription subscription;
    private static Log log = LogFactory.getLog(NatsClient.class);

    public NatsClient(String clusterId, String clientId, String natsUrl, ResultHolder resultHolder) {
        this.cluserId = clusterId;
        this.clientId = clientId;
        this.natsUrl = natsUrl;
        this.resultHolder = resultHolder;
    }

    public NatsClient(String clusterId, String clientId, String natsUrl) {
        this.cluserId = clusterId;
        this.clientId = clientId;
        this.natsUrl = natsUrl;
    }

    public NatsClient(String cluserId, String natsUrl) {
        this.cluserId = cluserId;
        this.clientId = createClientId();
        this.natsUrl = natsUrl;
    }

    public NatsClient(String cluserId, String natsUrl, ResultHolder resultHolder) {
        this.cluserId = cluserId;
        this.clientId = createClientId();
        this.natsUrl = natsUrl;
        this.resultHolder = resultHolder;
    }

    public void connect() throws IOException, InterruptedException {
        streamingConnectionFactory = new StreamingConnectionFactory(this.cluserId, this.clientId);
        streamingConnectionFactory.setNatsUrl(this.natsUrl);
        streamingConnection = streamingConnectionFactory.createConnection();
    }

    public void publish(String subjectName, String message) throws InterruptedException, TimeoutException, IOException {
            streamingConnection.publish(subjectName, message.getBytes(StandardCharsets.UTF_8));
    }

    public void subscribeFromNow(String subject) throws InterruptedException, TimeoutException, IOException {
        subscription = streamingConnection.subscribe(subject, (Message m) ->
            resultHolder.eventReceived(new String(m.getData(), StandardCharsets.UTF_8)),
                new SubscriptionOptions.Builder().startAtTime(Instant.now()).build());
    }

    public void subscribe(String subject) throws InterruptedException, IOException, TimeoutException {
        subscription =  streamingConnection.subscribe(subject, (Message m) ->
            resultHolder.eventReceived(new String(m.getData(), StandardCharsets.UTF_8)),
                new SubscriptionOptions.Builder().deliverAllAvailable().build());
    }

    public void subscribeFromLastPublished(String subject) throws InterruptedException, IOException, TimeoutException {
        subscription = streamingConnection.subscribe(subject, (Message m) ->
            resultHolder.eventReceived(new String(m.getData(), StandardCharsets.UTF_8)),
                new SubscriptionOptions.Builder().startWithLastReceived().build());
    }

    public void subscribeFromGivenSequence(String subject, int sequence) throws InterruptedException, IOException,
            TimeoutException {
        subscription = streamingConnection.subscribe(subject, (Message m) ->
            resultHolder.eventReceived(new String(m.getData(), StandardCharsets.UTF_8)),
                new SubscriptionOptions.Builder().startAtSequence(sequence).build());
    }

    public void subscrbeFromGivenTime(String subject,  Instant instant) throws InterruptedException, IOException,
            TimeoutException {
        subscription = streamingConnection.subscribe(subject, (Message m) ->
            resultHolder.eventReceived(new String(m.getData(), StandardCharsets.UTF_8)),
                new SubscriptionOptions.Builder().startAtTime(instant).build());

    }

    public void subscribeDurable(String subject, String durableName) throws InterruptedException, IOException,
            TimeoutException {
        subscription = streamingConnection.subscribe(subject, (Message m) ->
            resultHolder.eventReceived(new String(m.getData(), StandardCharsets.UTF_8)),
                new SubscriptionOptions.Builder().durableName(durableName).build());
    }

    public void unsubscribe() throws IOException {
        subscription.unsubscribe();
    }

    public void subscribeWithQueueGroupFromSequence(String subject, String queueGroup, int sequence)
            throws InterruptedException, TimeoutException, IOException {
        subscription = streamingConnection.subscribe(subject, queueGroup, (Message m) ->
            resultHolder.eventReceived(new String(m.getData(), StandardCharsets.UTF_8)) ,
                new SubscriptionOptions.Builder().startAtSequence(sequence).build());
    }

    private String createClientId() {
        return new Date().getTime() + "_" + new Random().nextInt(99999) + "_" + new Random().nextInt(99999);
    }

    /**
     * Class to retain results received by NATS client so that tests can poll the result and assert against.
     */
    public static class ResultHolder {
        private static Log log = LogFactory.getLog(ResultHolder.class);
        private int eventCount;
        private int expectedEventCount;
        private List<String> results;
        private CountDownLatch latch;
        private int timeoutInSeconds = 90;

        public ResultHolder(int expectedEventCount) {
            this.eventCount = 0;
            this.expectedEventCount = expectedEventCount;
            this.results = new ArrayList<>(expectedEventCount);
            this.latch = new CountDownLatch(expectedEventCount);
        }

        public ResultHolder(int expectedEventCount, int timeoutInSeconds) {
            this.eventCount = 0;
            this.expectedEventCount = expectedEventCount;
            this.timeoutInSeconds = timeoutInSeconds;
            this.results = new ArrayList<>(expectedEventCount);
            this.latch = new CountDownLatch(expectedEventCount);
        }

        private void eventReceived(String message) {
            eventCount++;
            results.add(message);
            latch.countDown();
        }

        /**
         * Returns the results list immediately
         * @return List of Strings
         */
        public List getResultList() {
            return new ArrayList(results);
        }

        /**
         *  Wait for the event count or the specified waiting time and return the results list
         * @return results list
         */
        public List waitAndGetResults() {
            try {
                if (latch.await(timeoutInSeconds, TimeUnit.SECONDS)) {
                    return new ArrayList(results);
                } else {
                    log.error("Expected number of results not received. " +
                            "Expected " + expectedEventCount + " events, but only received " + eventCount + " events.");
                    return new ArrayList(results);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new ArrayList(results);
        }
    }
}
