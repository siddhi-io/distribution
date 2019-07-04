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
package io.siddhi.distribution.test.framework.containers;

import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.distribution.test.framework.MySQLContainer;
import io.siddhi.distribution.test.framework.util.DatabaseClient;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assume.assumeFalse;
import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;
import static org.testng.Assert.fail;

/**
 * MySQL Database Container test class.
 */
public class MySQLContainerTest {

    private static final Logger logger = LoggerFactory.getLogger(MySQLContainerTest.class);

    @Test
    public void testSimple() throws SQLException, ConnectionUnavailableException {
        MySQLContainer mysql = (MySQLContainer) new MySQLContainer()
                .withLogConsumer(new Slf4jLogConsumer(logger));
        mysql.start();
        try {
            ResultSet resultSet = DatabaseClient.executeQuery(mysql, "SELECT 1");
            int resultSetInt = resultSet.getInt(1);
            assertEquals("A basic SELECT query succeeds", 1, resultSetInt);
        } finally {
            mysql.stop();
        }
    }

    @Test
    public void testSpecificVersion() throws SQLException, ConnectionUnavailableException {
        MySQLContainer mysqlOldVersion = (MySQLContainer) new MySQLContainer("mysql:5.5")
                .withLogConsumer(new Slf4jLogConsumer(logger));
        mysqlOldVersion.start();
        try {
            ResultSet resultSet = DatabaseClient.executeQuery(mysqlOldVersion, "SELECT VERSION()");
            String resultSetString = resultSet.getString(1);
            assertTrue("The database version can be set using a container rule parameter",
                    resultSetString.startsWith("5.5"));
        } finally {
            mysqlOldVersion.stop();
        }
    }

    @Test
    public void testMySQLWithCustomIniFile() throws SQLException, ConnectionUnavailableException {
        assumeFalse(SystemUtils.IS_OS_WINDOWS);
        MySQLContainer mysqlCustomConfig = new MySQLContainer("mysql:5.6");
        mysqlCustomConfig.start();
        try {
            ResultSet resultSet = DatabaseClient.executeQuery(mysqlCustomConfig,
                    "SELECT @@GLOBAL.innodb_file_format");
            String result = resultSet.getString(1);

            assertEquals("The InnoDB file format has been set by the ini file content",
                    "Antelope", result);
        } finally {
            mysqlCustomConfig.stop();
        }
    }

    @Test
    public void testCommandOverride() throws SQLException, ConnectionUnavailableException {
        MySQLContainer mysqlCustomConfig = (MySQLContainer) new MySQLContainer()
                .withCommand("mysqld --auto_increment_increment=42");
        mysqlCustomConfig.start();
        try {
            ResultSet resultSet = DatabaseClient.executeQuery(mysqlCustomConfig,
                    "show variables like 'auto_increment_increment'");
            String result = resultSet.getString("Value");
            assertEquals("Auto increment increment should be overriden by command line", "42", result);
        } finally {
            mysqlCustomConfig.stop();
        }

    }

    @Test
    public void testEmptyPasswordWithNonRootUser() {
        MySQLContainer container = (MySQLContainer) new MySQLContainer("mysql:5.5")
                .withDatabaseName("TEST")
                .withUsername("test")
                .withPassword("")
                .withEnv("MYSQL_ROOT_HOST", "%");
        try {
            container.start();
            fail("ContainerLaunchException expected to be thrown");
        } catch (ContainerLaunchException ignored) {
        } finally {
            container.stop();
        }
    }
}
