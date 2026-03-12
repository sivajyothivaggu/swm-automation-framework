package com.swm.database;

import com.swm.core.base.BaseDB;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DBConnection provides convenience helpers on top of BaseDB to execute queries
 * and transform ResultSet instances into collection structures.
 *
 * <p>Design notes:
 * - executeQuery returns a disconnected ResultSet (CachedRowSet) so callers do not
 *   need an open connection to iterate results.
 * - executeUpdate uses try-with-resources to ensure JDBC Statements are closed.
 * - getResultList converts a ResultSet into a List of Maps (one map per row).
 *
 * All methods perform robust error handling and logging.
 */
public class DBConnection extends BaseDB {

    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    /**
     * Executes the provided SQL SELECT query and returns a disconnected ResultSet
     * containing the results. The returned ResultSet is a CachedRowSet so it does
     * not depend on an open JDBC Connection.
     *
     * @param sql_query the SQL SELECT query to execute; must not be null
     * @return a disconnected ResultSet containing the query results
     * @throws SQLException if a database access error occurs or the query is invalid
     * @throws NullPointerException if sql_query is null
     */
    public ResultSet executeQuery(String sql_query) throws SQLException {
        Objects.requireNonNull(sql_query, "sql_query must not be null");
        logger.debug("Executing query: {}", sql_query);

        try {
            // Ensure we have an open connection
            if (Objects.isNull(connection) || connection.isClosed()) {
                logger.debug("Database connection is null or closed; attempting to connect.");
                connect();
            }

            try (Statement statement = connection.createStatement();
                 ResultSet result_set = statement.executeQuery(sql_query)) {

                CachedRowSet cached_row_set = RowSetProvider.newFactory().createCachedRowSet();
                cached_row_set.populate(result_set);
                logger.debug("Query executed and results populated into CachedRowSet.");
                return cached_row_set;
            }
        } catch (SQLException sqle) {
            logger.error("SQLException while executing query [{}]: {}", sql_query, sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger.error("Unexpected exception while executing query [{}]: {}", sql_query, e.getMessage(), e);
            throw new SQLException("Unexpected error executing query", e);
        } finally {
            try {
                // Disconnect after populating CachedRowSet to free resources.
                disconnect();
                logger.debug("Database connection disconnected after query execution.");
            } catch (Exception e) {
                // Log and continue; do not mask original exceptions
                logger.warn("Failed to disconnect database connection after query execution: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Executes the provided SQL update (INSERT/UPDATE/DELETE) and returns the update count.
     *
     * @param sql_query the SQL update statement; must not be null
     * @return number of rows affected
     * @throws SQLException if a database access error occurs
     * @throws NullPointerException if sql_query is null
     */
    public int executeUpdate(String sql_query) throws SQLException {
        Objects.requireNonNull(sql_query, "sql_query must not be null");
        logger.debug("Executing update: {}", sql_query);

        try {
            if (Objects.isNull(connection) || connection.isClosed()) {
                logger.debug("Database connection is null or closed; attempting to connect.");
                connect();
            }

            try (Statement statement = connection.createStatement()) {
                int rows_affected = statement.executeUpdate(sql_query);
                logger.debug("Update executed. Rows affected: {}", rows_affected);
                return rows_affected;
            }
        } catch (SQLException sqle) {
            logger.error("SQLException while executing update [{}]: {}", sql_query, sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger.error("Unexpected exception while executing update [{}]: {}", sql_query, e.getMessage(), e);
            throw new SQLException("Unexpected error executing update", e);
        } finally {
            try {
                disconnect();
                logger.debug("Database connection disconnected after update execution.");
            } catch (Exception e) {
                logger.warn("Failed to disconnect database connection after update execution: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Converts a ResultSet into a List of Maps where each Map represents a row keyed by column name.
     * The ResultSet provided will not be closed by this method; callers should manage lifecycle if needed.
     *
     * @param result_set the ResultSet to convert; must not be null
     * @return a List of Map<String, Object> representing rows; never null (may be empty)
     * @throws SQLException if reading the ResultSet fails
     * @throws NullPointerException if result_set is null
     */
    public List<Map<String, Object>> getResultList(ResultSet result_set) throws SQLException {
        Objects.requireNonNull(result_set, "result_set must not be null");
        List<Map<String, Object>> rows = new ArrayList<>();
        logger.debug("Converting ResultSet to List<Map<String, Object>>.");

        try {
            ResultSetMetaData meta_data = result_set.getMetaData();
            int column_count = meta_data.getColumnCount();

            while (result_set.next()) {
                Map<String, Object> row_map = new HashMap<>(column_count);
                for (int col_index = 1; col_index <= column_count; col_index++) {
                    String column_name = meta_data.getColumnLabel(col_index);
                    if (column_name == null || column_name.isEmpty()) {
                        column_name = meta_data.getColumnName(col_index);
                    }
                    Object column_value = result_set.getObject(col_index);
                    row_map.put(column_name, column_value);
                }
                rows.add(row_map);
            }

            logger.debug("ResultSet conversion complete. Rows converted: {}", rows.size());
            return rows;
        } catch (SQLException sqle) {
            logger.error("SQLException while converting ResultSet to list: {}", sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger.error("Unexpected exception while converting ResultSet to list: {}", e.getMessage(), e);
            throw new SQLException("Unexpected error converting ResultSet to list", e);
        }
    }

    /**
     * Helper to obtain the underlying connection (if available). This method returns the
     * connection from BaseDB. Use responsibly; callers should not close the connection here.
     *
     * @return the active Connection or null if none
     */
    public Connection getConnection() {
        try {
            if (Objects.isNull(connection) || connection.isClosed()) {
                logger.debug("No active connection available.");
                return null;
            }
            return connection;
        } catch (SQLException e) {
            logger.warn("Failed to determine connection state: {}", e.getMessage(), e);
            return null;
        }
    }
}