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
package io.siddhi.distribution.test.framework;

import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.Network;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * MySQL docker container
 */
public class MySQLContainer extends JdbcDatabaseContainer {
    private static final String IMAGE = "mysql";
    private static final String DEFAULT_TAG = "5.7.22";
    private static final Integer MYSQL_PORT = 3306;
    private String imageName = IMAGE + ":" + DEFAULT_TAG;
    private String databaseName = "siddhi-distribution-testdb";
    private String username = "test";
    private String password = "test";
    private static final String MYSQL_ROOT_USER = "root";
    private boolean isNetworkingEnabled = false;

    public MySQLContainer() {
        super(IMAGE + ":" + DEFAULT_TAG);
    }

    public MySQLContainer(String dockerImageName) {
        super(dockerImageName);
        imageName = dockerImageName;
    }

    @Override
    protected Set<Integer> getLivenessCheckPorts() {
        return new HashSet<>(getMappedPort(MYSQL_PORT));
    }

    @Override
    protected void configure() {
        addExposedPort(MYSQL_PORT);
        addEnv("MYSQL_DATABASE", databaseName);
        addEnv("MYSQL_USER", username);
        if (password != null && !password.isEmpty()) {
            addEnv("MYSQL_PASSWORD", password);
            addEnv("MYSQL_ROOT_PASSWORD", password);
        } else if (MYSQL_ROOT_USER.equalsIgnoreCase(username)) {
            addEnv("MYSQL_ALLOW_EMPTY_PASSWORD", "yes");
        } else {
            throw new ContainerLaunchException("Empty password can only be used with the root user");
        }
        setStartupAttempts(3);
    }

    @Override
    public String getDriverClassName() {
        return "com.mysql.jdbc.Driver";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:mysql://" + getContainerIpAddress() + ":" + getMappedPort(MYSQL_PORT) + "/" + databaseName
                + "?useSSL=false";
    }

    public String getNetworkedJdbcUrl() {
        if (isNetworkingEnabled) {
            return "jdbc:mysql://" + getNetworkAliases().get(0).toString() + ":" + MYSQL_PORT + "/" + databaseName
                    + "?useSSL=false";
        }
        return getJdbcUrl();
    }

    @Override
    protected String constructUrlForConnection(String queryString) {
        String url = super.constructUrlForConnection(queryString);
        if (!url.contains("useSSL=")) {
            String separator = url.contains("?") ? "&" : "?";
            url = url + separator + "useSSL=false";
        }
        //to handle connections from another host
        if (!url.contains("allowPublicKeyRetrieval=")) {
            url = url + "&allowPublicKeyRetrieval=true";
        }
        return url;
    }

    public MySQLContainer withNetwork(Network network) {
        super.withNetwork(network);
        isNetworkingEnabled = true;
        return this;
    }

    public MySQLContainer withNetworkAliases(String networkAlias) {
        super.withNetworkAliases(networkAlias);
        return this;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getTestQueryString() {
        return "SELECT 1";
    }

    @Override
    public MySQLContainer withDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    @Override
    public MySQLContainer withUsername(final String username) {
        this.username = username;
        return this;
    }

    @Override
    public MySQLContainer withPassword(final String password) {
        this.password = password;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MySQLContainer that = (MySQLContainer) o;
        return Objects.equals(imageName, that.imageName) && Objects.equals(databaseName, that.databaseName) &&
                Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), imageName, databaseName, username, password);
    }
}
