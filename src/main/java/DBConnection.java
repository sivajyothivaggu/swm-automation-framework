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
import java.util.Optional;
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
     * @throws SQLException         if a database access error occurs or the query is invalid
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
                 ResultSet result_set = statement.executeQuery(query)) {

                CachedRowSet cached_row_set = RowSetProvider.newFactory().createCachedRowSet();
                cached_row_set.populate(result_set);
                logger.debug("Query executed and results populated into CachedRowSet.");
                return cached_row_set;
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
     * Converts the provided ResultSet into a List of Maps where each Map represents a row
     * with column label to value mappings.
     *
     * Note: This method does not close the provided ResultSet. If the caller passes a
     * connected ResultSet they are responsible for closing the underlying resources. For
     * CachedRowSet (disconnected) it is safe to iterate and then discard.
     *
     * @param result_set the ResultSet to convert; must not be null
     * @return a List of Maps representing the rows; never null but may be empty
     * @throws SQLException if a database access error occurs while iterating the ResultSet
     */
    public List<Map<String, Object>> getResultList(ResultSet result_set) throws SQLException {
        Objects.requireNonNull(result_set, "result_set must not be null");
        logger.debug("Converting ResultSet to List<Map<String,Object>>");

        List<Map<String, Object>> result_list = new ArrayList<>();

        try {
            ResultSetMetaData meta_data = result_set.getMetaData();
            if (Objects.isNull(meta_data)) {
                logger.warn("ResultSetMetaData is null for the provided ResultSet.");
                return result_list;
            }

            int column_count = meta_data.getColumnCount();

            while (result_set.next()) {
                Map<String, Object> row_map = new HashMap<>(column_count);
                for (int col = 1; col <= column_count; col++) {
                    String column_name = meta_data.getColumnLabel(col);
                    if (column_name == null || column_name.isEmpty()) {
                        // Fallback to column name if label is empty
                        column_name = meta_data.getColumnName(col);
                    }
                    Object value = result_set.getObject(col);
                    row_map.put(column_name, value);
                }
                result_list.add(row_map);
            }
            logger.debug("ResultSet converted to list with {} rows.", result_list.size());
            return result_list;
        } catch (SQLException sqle) {
            logger.error("SQLException while converting ResultSet to list: {}", sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger.error("Unexpected exception while converting ResultSet to list: {}", e.getMessage(), e);
            throw new SQLException("Unexpected error converting ResultSet to list", e);
        }
    }

    /**
     * Returns the first row from the provided ResultSet as an Optional Map. If the ResultSet
     * contains no rows, Optional.empty() is returned.
     *
     * @param result_set the ResultSet to read; must not be null
     * @return Optional containing the first row as a Map, or Optional.empty() if no rows
     * @throws SQLException if a database access error occurs while iterating the ResultSet
     */
    public Optional<Map<String, Object>> getSingleResult(ResultSet result_set) throws SQLException {
        Objects.requireNonNull(result_set, "result_set must not be null");
        logger.debug("Extracting single result from ResultSet");

        try {
            ResultSetMetaData meta_data = result_set.getMetaData();
            if (Objects.isNull(meta_data)) {
                logger.warn("ResultSetMetaData is null for the provided ResultSet.");
                return Optional.empty();
            }

            int column_count = meta_data.getColumnCount();

            if (result_set.next()) {
                Map<String, Object> row_map = new HashMap<>(column_count);
                for (int col = 1; col <= column_count; col++) {
                    String column_name = meta_data.getColumnLabel(col);
                    if (column_name == null || column_name.isEmpty()) {
                        column_name = meta_data.getColumnName(col);
                    }
                    Object value = result_set.getObject(col);
                    row_map.put(column_name, value);
                }
                logger.debug("Single result extracted.");
                return Optional.of(row_map);
            } else {
                logger.debug("ResultSet contains no rows; returning Optional.empty().");
                return Optional.empty();
            }
        } catch (SQLException sqle) {
            logger.error("SQLException while extracting single result: {}", sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger.error("Unexpected exception while extracting single result: {}", e.getMessage(), e);
            throw new SQLException("Unexpected error extracting single result", e);
        }
    }
}