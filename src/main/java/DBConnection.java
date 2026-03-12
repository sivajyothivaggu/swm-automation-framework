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
     * @throws SQLException           if a database access error occurs or the query is invalid
     * @throws NullPointerException   if query is null
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
     * @throws SQLException         if a database access error occurs
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
     * Converts the provided ResultSet into a List of Maps, one map per row where
     * the keys are column labels and the values are the column values.
     *
     * The method will close the provided ResultSet when finished.
     *
     * @param resultSet the ResultSet to convert; must not be null
     * @return non-null List of rows (may be empty if ResultSet has no rows)
     * @throws SQLException           if an error occurs while reading the ResultSet
     * @throws NullPointerException   if resultSet is null
     */
    public List<Map<String, Object>> getResultList(ResultSet resultSet) throws SQLException {
        Objects.requireNonNull(resultSet, "resultSet must not be null");
        logger.debug("Converting ResultSet to List<Map<String,Object>>");

        List<Map<String, Object>> rows = new ArrayList<>();

        // Use try-with-resources to ensure the ResultSet is closed.
        try (ResultSet rs = resultSet) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnLabel = metaData.getColumnLabel(i);
                    if (columnLabel == null || columnLabel.isEmpty()) {
                        columnLabel = metaData.getColumnName(i);
                    }
                    Object value = rs.getObject(i);
                    row.put(columnLabel, value);
                }
                rows.add(row);
            }
            logger.debug("Converted ResultSet to list with {} rows.", rows.size());
        } catch (SQLException sqle) {
            logger.error("SQLException while converting ResultSet to list: {}", sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger.error("Unexpected exception while converting ResultSet to list: {}", e.getMessage(), e);
            throw new SQLException("Unexpected error converting ResultSet to list", e);
        }

        return rows;
    }

    /**
     * Utility to fetch a single row from a ResultSet as a Map. If the ResultSet has
     * no rows, an empty Map is returned. The ResultSet will be closed by this method.
     *
     * @param resultSet the ResultSet to read; must not be null
     * @return non-null Map representing the first row; empty if no rows
     * @throws SQLException         if an error occurs reading the ResultSet
     * @throws NullPointerException if resultSet is null
     */
    public Map<String, Object> getSingleResult(ResultSet resultSet) throws SQLException {
        Objects.requireNonNull(resultSet, "resultSet must not be null");
        logger.debug("Converting ResultSet to single Map<String,Object>");

        try (ResultSet rs = resultSet) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            if (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnLabel = metaData.getColumnLabel(i);
                    if (columnLabel == null || columnLabel.isEmpty()) {
                        columnLabel = metaData.getColumnName(i);
                    }
                    Object value = rs.getObject(i);
                    row.put(columnLabel, value);
                }
                logger.debug("Converted ResultSet to single row map.");
                return row;
            } else {
                logger.debug("ResultSet contained no rows; returning empty map.");
                return new HashMap<>();
            }
        } catch (SQLException sqle) {
            logger.error("SQLException while converting ResultSet to single map: {}", sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger.error("Unexpected exception while converting ResultSet to single map: {}", e.getMessage(), e);
            throw new SQLException("Unexpected error converting ResultSet to single map", e);
        }
    }
}