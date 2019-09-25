/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.siddhi.distribution.sample.kafka.client;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.extension.io.kafka.sink.KafkaSink;
import io.siddhi.extension.map.avro.sinkmapper.AvroSinkMapper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Test client for Kafka source.
 */
public class KafkaClient {

    private static Logger log = Logger.getLogger(KafkaClient.class);

    /**
     * Main method to start the test client.
     *
     * @param args no args need to be provided
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Initialize kafka producer client.");
        final String[] types = new String[]{"json", "xml", "text", "binary", "avro"};
        String bootstrapServers = args[0];
        String topic = args[1];
        String partitionNo = !args[2].isEmpty() ? args[2] : null;
        String sequenceId = !args[3].isEmpty() ? args[3] : null;
        String key = !args[4].isEmpty() ? args[4] : null;
        String schemaDefinition = args[5];
        String optionalConfiguration = !args[6].isEmpty() ? args[6] : null;
        Boolean isBinaryMessage = !args[7].isEmpty() && Boolean.parseBoolean(args[7]);
        String type = Arrays.asList(types).contains(args[8]) ? args[8] : "json";
        if (isBinaryMessage) {
            if (!"avro".equals(type)) {
                type = "binary";
            }
        }
        int delay = !args[9].isEmpty() ? Integer.parseInt(args[9]) : 1000;
        String customMapping = args[10];
        String filePath = args[11];
        String eventDefinition = args[12];
        int noOfEventsToSend = !args[13].isEmpty() ? Integer.parseInt(args[13]) : -1;
        boolean continuouslyReadFile = !args[14].isEmpty() && Boolean.parseBoolean(args[14]);

        boolean sendEventsContinuously = true;
        if (noOfEventsToSend != -1) {
            sendEventsContinuously = false;
        }
        List<String[]> fileEntriesList = null;
        if (!filePath.isEmpty()) {
            fileEntriesList = readFile(filePath);
        }
        if (eventDefinition.isEmpty()) {
            if (Boolean.parseBoolean(customMapping)) {
                if (type.equals("json")) {
                    eventDefinition = "{\"item\": {\"id\":\"{0}\",\"amount\": {1}}}";
                } else if (type.equals("xml")) {
                    eventDefinition = "<events><item><id>{0}</id><amount>{1}</amount></item></events>";
                } else if (type.equals("avro")) {
                    if (schemaDefinition.isEmpty()) {
                        schemaDefinition = "\"\"\"\n" +
                                "            {\n" +
                                "\t            \"type\": \"record\",\n" +
                                "\t            \"name\": \"sweetProduction\",\n" +
                                "\t            \"namespace\": \"material\",\n" +
                                "\t            \"fields\": [{\n" +
                                "\t\t            \"name\": \"name\",\n" +
                                "\t\t            \"type\": \"string\"\n" +
                                "\t            }, {\n" +
                                "\t\t            \"name\": \"amount\",\n" +
                                "\t\t            \"type\": \"double\"\n" +
                                "\t\t        }]\n" +
                                "            }\"\"\"";
                         eventDefinition = "{\"event\": {\"name\":\"{0}\",\"amount\": {1}}}";
                    }
                } else {
                    eventDefinition = "id:\"{0}\"\namount:{1}";
                }
            } else {
                if (type.equals("json")) {
                    eventDefinition = "{\"event\": {\"name\":\"{0}\",\"amount\": {1}}}";
                } else if (type.equals("xml")) {
                    eventDefinition = "<events><event><name>{0}</name><amount>{1}</amount></event></events>";
                } else if (type.equals("avro")) {
                    if (schemaDefinition.isEmpty()) {
                        schemaDefinition = "\"\"\"\n" +
                                "            {\n" +
                                "\t            \"type\": \"record\",\n" +
                                "\t            \"name\": \"sweetProduction\",\n" +
                                "\t            \"namespace\": \"material\",\n" +
                                "\t            \"fields\": [{\n" +
                                "\t\t            \"name\": \"name\",\n" +
                                "\t\t            \"type\": \"string\"\n" +
                                "\t            }, {\n" +
                                "\t\t            \"name\": \"amount\",\n" +
                                "\t\t            \"type\": \"double\"\n" +
                                "\t\t        }]\n" +
                                "            }\"\"\"";
                        eventDefinition = "{\"event\": {\"name\":\"{0}\",\"amount\": {1}}}";
                    }

                } else {
                    eventDefinition = "name:\"{0}\"\namount:{1}";
                }
            }
        }

        SiddhiManager siddhiManager = new SiddhiManager();
        InputHandler sweetProductionStream;

        StringBuilder builder = new StringBuilder("@App:name(\"KafkaSink\")\n" +
                                                          "@sink(type='kafka',\n");
        builder.append("topic='").append(topic).append("',\n");
        if (partitionNo != null) {
            builder.append("partition.no='").append(partitionNo).append("',\n");
        }
        builder.append("bootstrap.servers='").append(bootstrapServers).append("',\n");
        if (sequenceId != null) {
            builder.append("sequence.id='").append(sequenceId).append("',\n");
        }
        if (key != null) {
            builder.append("key='").append(key).append("',\n");
        }
        if (isBinaryMessage) {
            builder.append("is.binary.message='true',\n");
        }
        if (optionalConfiguration != null) {
            builder.append("optional.configuration='").append(optionalConfiguration).append("',\n");
        }
        if ("binary".equalsIgnoreCase(type)) {
            builder.append("@map(type='").append(type).append("'))\n");
            builder.append("define stream SweetProductionStream(name string, amount double);\n");
        } else if ("avro".equalsIgnoreCase(type)) {
            siddhiManager.setExtension("sink:kafka", KafkaSink.class);
            siddhiManager.setExtension("sinkMapper:avro", AvroSinkMapper.class);
            builder.append("is.binary.message='true',\n");
            builder.append("@map(type='").append(type).append("',schema.def=").append(schemaDefinition).append("))\n");
            builder.append("define stream SweetProductionStream(name string, amount double);\n");
        } else {
            builder.append("@map(type='").append(type).append("', @payload(\"{{message}}\")))\n");
            builder.append("define stream SweetProductionStream(message string);\n");
        }

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(builder.toString());
        siddhiAppRuntime.start();
        sweetProductionStream = siddhiAppRuntime.getInputHandler("SweetProductionStream");
        Thread.sleep(2000);
        String[] sweetName = {"Cupcake", "Donut", "Éclair", "Froyo", "Gingerbread", "Honeycomb", "Ice",
                              "Cream Sandwich", "Jelly Bean", "KitKat", "Lollipop", "Marshmallow"};
        EventSendingUtil.publishEvents(fileEntriesList, sendEventsContinuously, noOfEventsToSend, eventDefinition,
                                       sweetName, sweetProductionStream, delay, isBinaryMessage, continuouslyReadFile);
        Thread.sleep(2000);
        siddhiAppRuntime.shutdown();
        Thread.sleep(2000);
    }

    private static List<String[]> readFile(String fileName) throws IOException {
        File file = new File(fileName);
        Scanner inputStream = new Scanner(file);
        List<String[]> fileEntriesList = new ArrayList<String[]>();
        while (inputStream.hasNext()) {
            String data = inputStream.next();
            fileEntriesList.add(data.split(","));
        }
        inputStream.close();
        return fileEntriesList;
    }
}
