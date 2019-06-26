package io.siddhi.distribution.test.framework.util;

import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.distribution.test.framework.JdbcDatabaseContainer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

public class DatabaseClient {

    /**
     * Method for executing query on the provided JDBC database container
     *
     * @param container JDBC Database Container instance
     * @param query the query to be executed.
     * @throws SQLException if the query execution fails.
     */
    public static ResultSet executeQuery(JdbcDatabaseContainer container, String query)
            throws ConnectionUnavailableException, SQLException {
        ResultSet resultSet = null;
        Connection connection = null;
        Statement statement = null;
        boolean autocommit = true;
        try {
            connection = getConnection(container, autocommit);
            statement = connection.createStatement();
            statement.execute(query);
            resultSet = statement.getResultSet();
            RowSetFactory factory = RowSetProvider.newFactory();
            CachedRowSet rowset = factory.createCachedRowSet();
            rowset.populate(resultSet);
            rowset.next();
            return rowset;
        } catch (SQLException e) {
            throw new ConnectionUnavailableException("Could not execute the query. " +
                    "Connection is closed for database: '" + container.getDatabaseName() + "'", e);
        } finally {
            if (!autocommit) {
                connection.commit();
            }
            cleanupConnection(resultSet, statement, connection);
        }
    }

    /**
     * Returns a connection instance.
     *
     * @param container JDBC Database Container instance
     * @param autoCommit whether or not transactions to the connections should be committed automatically.
     * @return a new {@link Connection} instance from the datasource.
     * @throws SQLException
     */
    private static Connection getConnection(JdbcDatabaseContainer container, boolean autoCommit)
            throws SQLException {
        Connection conn = container.getDataSource().getConnection();
        conn.setAutoCommit(autoCommit);
        return conn;
    }

    /**
     * Method which can be used to clear up and ephemeral SQL connectivity artifacts.
     *
     * @param rs   {@link ResultSet} instance (can be null)
     * @param stmt {@link Statement} instance (can be null)
     * @param conn {@link Connection} instance (can be null)
     */
    private static void cleanupConnection(ResultSet rs, Statement stmt, Connection conn) throws SQLException {
        if (rs != null) {
            rs.close();
        }
        if (stmt != null) {
            stmt.close();
        }
        if (conn != null) {
            conn.close();
        }
    }
}
