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
            if (Objects.isNull(connection) || connection.isClosed()) {
                logger.debug("Database connection is null or closed; attempting to connect.");
                connect();
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                CachedRowSet cached_row_set = RowSetProvider.newFactory().createCachedRowSet();
                cached_row_set.populate(rs);
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

            try (Statement stmt = connection.createStatement()) {
                int result = stmt.executeUpdate(query);
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
     * Converts the given ResultSet into a List of Maps. Each Map represents a single row
     * with column labels as keys and column values as values.
     *
     * Note: This method will close the ResultSet if it is an instance of CachedRowSet
     * after conversion. It will not attempt to close other ResultSet implementations to
     * avoid closing resources owned by callers.
     *
     * @param result_set the ResultSet to convert; must not be null
     * @return List of rows represented as maps; never null (empty list if no rows)
     * @throws SQLException if a database access error occurs during reading
     * @throws NullPointerException if result_set is null
     */
    public List<Map<String, Object>> getResultList(ResultSet result_set) throws SQLException {
        Objects.requireNonNull(result_set, "result_set must not be null");
        logger.debug("Converting ResultSet to List<Map<String, Object>>.");

        List<Map<String, Object>> result_list = new ArrayList<>();

        try {
            ResultSetMetaData meta_data = result_set.getMetaData();
            int column_count = meta_data.getColumnCount();

            while (result_set.next()) {
                Map<String, Object> row_map = new HashMap<>(column_count);
                for (int col_index = 1; col_index <= column_count; col_index++) {
                    String column_label = meta_data.getColumnLabel(col_index);
                    Object value = result_set.getObject(col_index);
                    row_map.put(column_label, value);
                }
                result_list.add(row_map);
            }

            logger.debug("Converted ResultSet to list with {} rows.", result_list.size());
            return result_list;
        } catch (SQLException sqle) {
            logger.error("SQLException while converting ResultSet to list: {}", sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger.error("Unexpected exception while converting ResultSet to list: {}", e.getMessage(), e);
            throw new SQLException("Unexpected error converting ResultSet to list", e);
        } finally {
            // Close the ResultSet only if it's a CachedRowSet (disconnected and safe to close)
            try {
                if (result_set instanceof CachedRowSet) {
                    ((CachedRowSet) result_set).close();
                    logger.debug("Closed CachedRowSet after conversion.");
                }
            } catch (Exception e) {
                logger.warn("Failed to close ResultSet after conversion: {}", e.getMessage(), e);
            }
        }
    }
}