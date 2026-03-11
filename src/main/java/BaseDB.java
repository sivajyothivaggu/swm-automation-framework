package com.swm.core.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.swm.core.config.ConfigManager;

/**
 * BaseDB provides basic JDBC connection management for subclasses.
 * <p>
 * Responsibilities:
 * - Establish and close a JDBC Connection using configuration provided by ConfigManager.
 * - Expose a safe Optional-based accessor for the current connection.
 * - Provide robust logging and error handling.
 * </p>
 *
 * Note: Field and method names intentionally use snake_case to satisfy external naming checks
 * while keeping behavior consistent with previous implementation.
 */
public class BaseDB {
    private static final Logger LOGGER = Logger.getLogger(BaseDB.class.getName());

    /**
     * The live JDBC connection instance. Null when no connection is established.
     */
    protected Connection connection_instance;

    /**
     * Establishes a database connection using credentials obtained from ConfigManager.
     * This method is synchronized to avoid race conditions when used by subclasses concurrently.
     *
     * @throws SQLException if a database access error occurs or required configuration is missing
     */
    protected synchronized void connect() throws SQLException {
        String db_url = ConfigManager.getDbUrl();
        String db_user = ConfigManager.getDbUser();
        String db_password = ConfigManager.getDbPassword();

        if (Objects.isNull(db_url) || Objects.isNull(db_user) || Objects.isNull(db_password)) {
            String message = "Database configuration incomplete: url/user/password must not be null.";
            LOGGER.log(Level.SEVERE, message);
            throw new SQLException(message);
        }

        try {
            connection_instance = DriverManager.getConnection(db_url, db_user, db_password);
            LOGGER.log(Level.FINE, "Database connection established.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection.", ex);
            throw ex;
        }
    }

    /**
     * Closes the current database connection if it is open.
     * This method is synchronized to avoid race conditions when closing while another thread may be using the connection.
     *
     * @throws SQLException if a database access error occurs while closing
     */
    protected synchronized void disconnect() throws SQLException {
        if (Objects.isNull(connection_instance)) {
            LOGGER.log(Level.FINE, "No database connection to close.");
            return;
        }

        try {
            if (!connection_instance.isClosed()) {
                connection_instance.close();
                LOGGER.log(Level.FINE, "Database connection closed.");
            } else {
                LOGGER.log(Level.FINE, "Database connection was already closed.");
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error while closing database connection.", ex);
            throw ex;
        } finally {
            connection_instance = null;
        }
    }

    /**
     * Returns an Optional containing the current Connection if present.
     *
     * @return Optional of Connection
     */
    protected Optional<Connection> get_connection() {
        return Optional.ofNullable(connection_instance);
    }
}