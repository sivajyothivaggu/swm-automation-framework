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

    private static final Logger logger_obj = LoggerFactory.getLogger(DBConnection.class);

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
        logger_obj.debug("Executing query: {}", sql_query);

        try {
            // Ensure we have an open connection
            if (Objects.isNull(connection) || connection.isClosed()) {
                logger_obj.debug("Database connection is null or closed; attempting to connect.");
                connect();
            }

            try (Statement stmt_obj = connection.createStatement();
                 ResultSet result_set = stmt_obj.executeQuery(sql_query)) {

                CachedRowSet cached_row_set = RowSetProvider.newFactory().createCachedRowSet();
                cached_row_set.populate(result_set);
                logger_obj.debug("Query executed and results populated into CachedRowSet.");
                return cached_row_set;
            }
        } catch (SQLException sqle) {
            logger_obj.error("SQLException while executing query [{}]: {}", sql_query, sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger_obj.error("Unexpected exception while executing query [{}]: {}", sql_query, e.getMessage(), e);
            throw new SQLException("Unexpected error executing query", e);
        } finally {
            try {
                // Disconnect after populating CachedRowSet to free resources.
                disconnect();
                logger_obj.debug("Database connection disconnected after query execution.");
            } catch (Exception e) {
                // Log and continue; do not mask original exceptions
                logger_obj.warn("Failed to disconnect database connection after query execution: {}", e.getMessage(), e);
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
        logger_obj.debug("Executing update: {}", sql_query);

        try {
            if (Objects.isNull(connection) || connection.isClosed()) {
                logger_obj.debug("Database connection is null or closed; attempting to connect.");
                connect();
            }

            try (Statement stmt_obj = connection.createStatement()) {
                int rows_affected = stmt_obj.executeUpdate(sql_query);
                logger_obj.debug("Update executed. Rows affected: {}", rows_affected);
                return rows_affected;
            }
        } catch (SQLException sqle) {
            logger_obj.error("SQLException while executing update [{}]: {}", sql_query, sqle.getMessage(), sqle);
            throw sqle;
        } catch (Exception e) {
            logger_obj.error("Unexpected exception while executing update [{}]: {}", sql_query, e.getMessage(), e);
            throw new SQLException("Unexpected error executing update", e);
        } finally {
            try {
                disconnect();
                logger_obj.debug("Database connection disconnected after update execution.");
            } catch (Exception e) {
                logger_obj.warn("Failed to disconnect database connection after update execution: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Converts the provided ResultSet into a List of Maps where each Map represents a row
     * keyed by column name with the corresponding column value.
     *
     * The method returns an Optional containing the list. If the provided result_set is null
     * or an error occurs during processing, Optional.empty() is returned.
     *
     * @param result_set the ResultSet to convert; may be a disconnected CachedRowSet
     * @return Optional containing List of Maps (one per row), or Optional.empty() on null input or error
     */
    public Optional<List<Map<String, Object>>> getResultList(ResultSet result_set) {
        if (Objects.isNull(result_set)) {
            logger_obj.debug("getResultList called with null ResultSet; returning empty Optional.");
            return Optional.empty();
        }

        List<Map<String, Object>> result_list = new ArrayList<>();
        try {
            ResultSetMetaData meta_data = result_set.getMetaData();
            int column_count = meta_data.getColumnCount();

            while (result_set.next()) {
                Map<String, Object> row_map = new HashMap<>(column_count);
                for (int i = 1; i <= column_count; i++) {
                    String column_name = meta_data.getColumnLabel(i);
                    if (column_name == null || column_name.isEmpty()) {
                        column_name = meta_data.getColumnName(i);
                    }
                    Object value = result_set.getObject(i);
                    row_map.put(column_name, value);
                }
                result_list.add(row_map);
            }

            logger_obj.debug("Converted ResultSet to List<Map<String,Object>> with {} rows.", result_list.size());
            return Optional.of(result_list);
        } catch (SQLException sqle) {
            logger_obj.error("SQLException while converting ResultSet to list: {}", sqle.getMessage(), sqle);
            return Optional.empty();
        } catch (Exception e) {
            logger_obj.error("Unexpected exception while converting ResultSet to list: {}", e.getMessage(), e);
            return Optional.empty();
        } finally {
            // Do not close the ResultSet here because callers may expect to manage its lifecycle,
            // especially if it's a CachedRowSet that is disconnected and safe to close elsewhere.
        }
    }
}