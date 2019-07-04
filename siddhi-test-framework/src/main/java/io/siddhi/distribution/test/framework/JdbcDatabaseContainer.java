package io.siddhi.distribution.test.framework;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.rnorth.ducttape.ratelimits.RateLimiter;
import org.rnorth.ducttape.ratelimits.RateLimiterBuilder;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

/**
 * Base class for containers that expose a JDBC connection.
 *
 */
public abstract class JdbcDatabaseContainer extends GenericContainer {

    private static final Object DRIVER_LOAD_MUTEX = new Object();
    private static final RateLimiter DB_CONNECT_RATE_LIMIT = RateLimiterBuilder.newBuilder()
        .withRate(10, TimeUnit.SECONDS)
        .withConstantThroughput()
        .build();
    private Driver driver;
    private DataSource dataSource;
    private int startupTimeoutSeconds = 120;
    private int connectTimeoutSeconds = 120;

    public JdbcDatabaseContainer(final String dockerImageName) {
        super(dockerImageName);
    }

    public JdbcDatabaseContainer(final Future<String> image) {
        super(image);
    }

    /**
     * @return the name of the actual JDBC driver to use
     */
    public abstract String getDriverClassName();

    /**
     * @return a JDBC URL that may be used to connect to the dockerized DB
     */
    public abstract String getJdbcUrl();

    /**
     * @return the database name
     */
    public String getDatabaseName() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the standard database username that should be used for connections
     */
    public abstract String getUsername();

    /**
     * @return the standard password that should be used for connections
     */
    public abstract String getPassword();

    /**
     * @return a test query string suitable for testing that this particular database type is alive
     */
    protected abstract String getTestQueryString();

    public JdbcDatabaseContainer withUsername(String username) {
        throw new UnsupportedOperationException();
    }

    public JdbcDatabaseContainer withPassword(String password) {
        throw new UnsupportedOperationException();
    }

    public JdbcDatabaseContainer withDatabaseName(String dbName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set startup time to allow, including image pull time, in seconds.
     *
     * @param startupTimeoutSeconds startup time to allow, including image pull time, in seconds
     * @return self
     */
    public JdbcDatabaseContainer withStartupTimeoutSeconds(int startupTimeoutSeconds) {
        this.startupTimeoutSeconds = startupTimeoutSeconds;
        return this;
    }

    /**
     * Set time to allow for the database to start and establish an initial connection, in seconds.
     *
     * @param connectTimeoutSeconds time to allow for the database to start
     *                              and establish an initial connection in seconds
     * @return self
     */
    public JdbcDatabaseContainer withConnectTimeoutSeconds(int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
        return this;
    }

    @Override
    protected void waitUntilContainerStarted() {
        // Repeatedly try and open a connection to the DB and execute a test query
        logger().info("Waiting for database connection to become available at {} using query '{}'",
                getJdbcUrl(), getTestQueryString());
        Unreliables.retryUntilSuccess(getStartupTimeoutSeconds(), TimeUnit.SECONDS, () -> {
            if (!isRunning()) {
                throw new ContainerLaunchException("Container failed to start");
            }
            try (Connection connection = createConnection("")) {
                try (Statement statement = connection.createStatement()) {
                    boolean success = statement.execute(JdbcDatabaseContainer.this.getTestQueryString());
                    if (success) {
                        logger().info("Obtained a connection to container ({})",
                                JdbcDatabaseContainer.this.getJdbcUrl());
                        return null;
                    } else {
                        throw new SQLException("Failed to execute test query:" + getTestQueryString());
                    }
                }
            }
        });
    }

    /**
     * Obtain an instance of the correct JDBC driver for this particular database container type.
     *
     * @return a JDBC Driver
     */
    public Driver getJdbcDriverInstance() {
        synchronized (DRIVER_LOAD_MUTEX) {
            if (driver == null) {
                try {
                    driver = (Driver) Class.forName(this.getDriverClassName()).newInstance();
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    throw new RuntimeException("Could not get Driver", e);
                }
            }
        }
        return driver;
    }

    /**
     * Creates a connection to the underlying containerized database instance.
     *
     * @param queryString query string parameters that should be appended to the JDBC connection URL.
     *                    The '?' character must be included
     * @return a Connection
     * @throws SQLException if there is a repeated failure to create the connection
     */
    public Connection createConnection(String queryString) throws SQLException {
        final Properties info = new Properties();
        info.put("user", this.getUsername());
        info.put("password", this.getPassword());
        final String url = constructUrlForConnection(queryString);
        final Driver jdbcDriverInstance = getJdbcDriverInstance();
        try {
            return Unreliables.retryUntilSuccess(getConnectTimeoutSeconds(), TimeUnit.SECONDS, () ->
                DB_CONNECT_RATE_LIMIT.getWhenReady(() ->
                    jdbcDriverInstance.connect(url, info)));
        } catch (Exception e) {
            throw new SQLException("Could not create new connection", e);
        }
    }

    public DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = createDataSource();
        }
        return dataSource;
    }

    private DataSource createDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getJdbcUrl());
        hikariConfig.setUsername(getUsername());
        hikariConfig.setPassword(getPassword());
        return new HikariDataSource(hikariConfig);
    }

    /**
     * Template method for constructing the JDBC URL to be used for creating {@link Connection}s.
     * This should be overridden if the JDBC URL and query string concatenation or URL string
     * construction needs to be different to normal.
     *
     * @param queryString query string parameters that should be appended to the JDBC connection URL.
     *                    The '?' character must be included
     * @return a full JDBC URL including queryString
     */
    protected String constructUrlForConnection(String queryString) {
        return getJdbcUrl() + queryString;
    }

    /**
     * @return startup time to allow, including image pull time, in seconds
     */
    protected int getStartupTimeoutSeconds() {
        return startupTimeoutSeconds;
    }

    /**
     * @return time to allow for the database to start and establish an initial connection, in seconds
     */
    private int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }
}
