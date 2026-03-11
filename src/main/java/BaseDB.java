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
 *
 * Responsibilities:
 * - Establish and close a JDBC Connection using configuration provided by ConfigManager.
 * - Expose a safe Optional-based accessor for the current connection.
 * - Provide robust logging and error handling.
 *
 * Threading:
 * - The connect() and disconnect() methods are synchronized to avoid race conditions when
 *   subclasses access the connection concurrently.
 *
 * Usage:
 * - Subclasses may call connect() to create the connection and getConnection() to obtain
 *   the live Connection wrapped in an Optional.
 *
 * Note: This class intentionally keeps the Connection as a protected field so that
 * subclasses with special needs can access the underlying JDBC Connection when necessary.
 */
public class BaseDB {
    private static final Logger LOGGER = Logger.getLogger(BaseDB.class.getName());

    /**
     * The live JDBC connection instance. Null when no connection is established.
     */
    protected Connection connectionInstance;

    /**
     * Establishes a database connection using credentials obtained from ConfigManager.
     * This method is synchronized to avoid race conditions when used by subclasses concurrently.
     *
     * @throws SQLException if a database access error occurs or required configuration is missing
     */
    protected synchronized void connect() throws SQLException {
        final String dbUrl = ConfigManager.getDbUrl();
        final String dbUser = ConfigManager.getDbUser();
        final String dbPassword = ConfigManager.getDbPassword();

        if (Objects.isNull(dbUrl) || Objects.isNull(dbUser) || Objects.isNull(dbPassword)) {
            final String message = "Database configuration incomplete: url/user/password must not be null.";
            LOGGER.log(Level.SEVERE, message);
            throw new SQLException(message);
        }

        try {
            connectionInstance = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            LOGGER.log(Level.FINE, "Database connection established.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection.", ex);
            throw ex;
        } catch (RuntimeException ex) {
            // Wrap unexpected runtime exceptions to provide consistent behavior to callers
            LOGGER.log(Level.SEVERE, "Unexpected error while establishing database connection.", ex);
            throw new SQLException("Unexpected error while establishing database connection.", ex);
        }
    }

    /**
     * Closes the current database connection if it is open.
     * This method is synchronized to avoid race conditions when closing while another thread may be using the connection.
     *
     * @throws SQLException if a database access error occurs while closing
     */
    protected synchronized void disconnect() throws SQLException {
        if (Objects.isNull(connectionInstance)) {
            LOGGER.log(Level.FINE, "No database connection to close.");
            return;
        }

        boolean closedSuccessfully = false;
        try {
            if (!connectionInstance.isClosed()) {
                connectionInstance.close();
                closedSuccessfully = true;
                LOGGER.log(Level.FINE, "Database connection closed.");
            } else {
                LOGGER.log(Level.FINE, "Database connection was already closed.");
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error while closing database connection.", ex);
            throw ex;
        } catch (Exception ex) {
            // Catch non-SQLExceptions that may occur during close and rethrow as SQLException
            LOGGER.log(Level.SEVERE, "Unexpected error while closing database connection.", ex);
            throw new SQLException("Unexpected error while closing database connection.", ex);
        } finally {
            // Ensure reference is cleared so subsequent operations know there is no active connection.
            connectionInstance = null;
            if (!closedSuccessfully) {
                LOGGER.log(Level.FINER, "Connection reference cleared after attempted close.");
            }
        }
    }

    /**
     * Returns an Optional containing the current Connection if present.
     *
     * @return Optional of Connection
     */
    protected Optional<Connection> getConnection() {
        return Optional.ofNullable(connectionInstance);
    }
}