/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.siddhi.distribution.test.framework.util;

import io.netty.handler.codec.http.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import static java.lang.System.currentTimeMillis;

/**
 * HTTP Request Sender Util
 */
public class HTTPClient {
    private static final String LINE_FEED = "\r\n";
    private static final String CHARSET = "UTF-8";
    private HttpURLConnection connection = null;
    private OutputStream outputStream = null;
    private PrintWriter writer = null;
    private String boundary = null;

    public HTTPClient(URI baseURI, String path, Boolean auth, Boolean keepAlive, String methodType,
                      String contentType, String userName, String password) throws IOException {
            URL url = baseURI.resolve(path).toURL();
            boundary = "---------------------------" + currentTimeMillis();

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept-Charset", CHARSET);
            connection.setRequestMethod(methodType);
            setHeader("HTTP_METHOD", methodType);
            if (keepAlive) {
                connection.setRequestProperty("Connection", "Keep-Alive");
            }
            if (contentType != null) {
                if (contentType.equals("multipart/form-data")) {
                    setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
                } else {
                    setHeader("Content-Type", contentType);
                }
            }
            connection.setUseCaches(false);
            connection.setDoInput(true);
            if (auth) {
                connection.setRequestProperty("Authorization",
                        "Basic " + java.util.Base64.getEncoder().
                                encodeToString((userName + ":" + password).getBytes()));
            }
            if (methodType.equals(HttpMethod.POST.name()) || methodType.equals(HttpMethod.PUT.name())
                    || methodType.equals(HttpMethod.DELETE.name())) {
                connection.setDoOutput(true);
                outputStream = connection.getOutputStream();
                writer = new PrintWriter(new OutputStreamWriter(outputStream, CHARSET),
                        true);
            }
    }

    public HttpURLConnection getConnection() {
        return this.connection;
    }

    public void addBodyContent(String body) {
        if (body != null && !body.isEmpty()) {
            writer.write(body);
            writer.close();
        }
    }

    public HTTPResponseMessage getResponse() throws IOException {
        assert connection != null;
        String successContent = null;
        String errorContent = null;
        if (writer != null) {
            writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();
        }
        try {
            if (connection.getResponseCode() >= 400) {
                errorContent = readErrorContent();
            } else {
                successContent = readSuccessContent();
            }
            return new HTTPResponseMessage(connection.getResponseCode(),
                    connection.getContentType(), connection.getResponseMessage(), successContent, errorContent);
        } finally {
            connection.disconnect();
        }
    }

    private String readSuccessContent() throws IOException {
        StringBuilder sb = new StringBuilder("");
        String line;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                connection.getInputStream()))) {
            while ((line = in.readLine()) != null) {
                sb.append(line + "\n");
            }
        }
        return sb.toString();
    }

    private String readErrorContent() throws IOException {
        StringBuilder sb = new StringBuilder("");
        String line;
        InputStream errorStream = connection.getErrorStream();
        if (errorStream != null) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(errorStream))) {
                while ((line = in.readLine()) != null) {
                    sb.append(line + "\n");
                }
            }
        }
        return sb.toString();
    }

    private void setHeader(String key, String value) {
        if (key != null && value != null) {
            connection.setRequestProperty(key, value);
        }
    }

    public static class HTTPResponseMessage {
        private int responseCode;
        private String contentType;
        private String message;
        private Object successContent;
        private Object errorContent;

        public HTTPResponseMessage(int responseCode, String contentType, String message) {
            this.responseCode = responseCode;
            this.contentType = contentType;
            this.message = message;
        }

        public HTTPResponseMessage(int responseCode, String contentType, String message, Object successContent, Object
                errorContent) {
            this.responseCode = responseCode;
            this.contentType = contentType;
            this.message = message;
            this.successContent = successContent;
            this.errorContent = errorContent;
        }

        public HTTPResponseMessage() {
        }

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public Object getSuccessContent() {
            return successContent;
        }

        public void setSuccessContent(Object content) {
            this.successContent = content;
        }

        public Object getErrorContent() {
            return errorContent;
        }

        public void setErrorContent(Object errorContent) {
            this.errorContent = errorContent;
        }
    }

}
