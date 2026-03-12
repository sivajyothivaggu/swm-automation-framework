package com.swm.database;

import com.swm.core.base.BaseDB;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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
     * @param query the SQL SELECT query to execute; must not be null
     * @return a disconnected ResultSet containing the query results
     * @throws SQLException if a database access error occurs or the query is invalid
     * @throws NullPointerException if query is null
     */
    public ResultSet executeQuery(String query) throws SQLException {
        Objects.requireNonNull(query, "query must not be null");
        logger.debug("Executing query: {}", query);

        try {
            // Ensure we have an open connection
            if (Objects.isNull(connection) || connection.isClosed()) {
                logger.debug("Database connection is null or closed; attempting to connect.");
                connect();
            }

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
                cachedRowSet.populate(resultSet);
                logger.debug("Query executed and results populated into CachedRowSet.");
                return cachedRowSet;
            }
        } catch (SQLException sqle) {
            logger.error("SQLException while executing query [{}]: {}", query, sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger.error("Unexpected exception while executing query [{}]: {}", query, e.getMessage(), e);
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
     * @param query the SQL update statement; must not be null
     * @return number of rows affected
     * @throws SQLException if a database access error occurs
     * @throws NullPointerException if query is null
     */
    public int executeUpdate(String query) throws SQLException {
        Objects.requireNonNull(query, "query must not be null");
        logger.debug("Executing update: {}", query);

        try {
            if (Objects.isNull(connection) || connection.isClosed()) {
                logger.debug("Database connection is null or closed; attempting to connect.");
                connect();
            }

            try (Statement statement = connection.createStatement()) {
                int result = statement.executeUpdate(query);
                logger.debug("Update executed. Rows affected: {}", result);
                return result;
            }
        } catch (SQLException sqle) {
            logger.error("SQLException while executing update [{}]: {}", query, sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger.error("Unexpected exception while executing update [{}]: {}", query, e.getMessage(), e);
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
     * Converts a ResultSet into a List of Maps where each map represents a row with
     * column-label-to-value mapping. This method does not close the provided ResultSet;
     * callers should close it if necessary. If null is provided, an empty list is returned.
     *
     * @param resultSet the ResultSet to convert; may be null
     * @return a List of Maps representing the rows; never null
     * @throws SQLException if reading from the ResultSet fails
     */
    public List<Map<String, Object>> getResultList(ResultSet resultSet) throws SQLException {
        if (Objects.isNull(resultSet)) {
            logger.debug("getResultList called with null ResultSet; returning empty list.");
            return new ArrayList<>();
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>(columnCount);
                for (int col = 1; col <= columnCount; col++) {
                    String columnLabel = metaData.getColumnLabel(col);
                    if (columnLabel == null || columnLabel.isEmpty()) {
                        columnLabel = metaData.getColumnName(col);
                    }
                    Object value = resultSet.getObject(col);
                    row.put(columnLabel, value);
                }
                rows.add(row);
            }
            logger.debug("Converted ResultSet to list of {} rows.", rows.size());
            return rows;
        } catch (SQLException sqle) {
            logger.error("SQLException while converting ResultSet to list: {}", sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger.error("Unexpected exception while converting ResultSet to list: {}", e.getMessage(), e);
            throw new SQLException("Unexpected error converting ResultSet to list", e);
        }
    }
}