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
 * - Establish and close a JDBC Connection using configuration provided by {@link ConfigManager}.
 * - Expose a safe Optional-based accessor for the current connection.
 * - Provide robust logging and error handling.
 *
 * Threading:
 * - The {@code connect()} and {@code disconnect()} methods are synchronized to avoid race conditions
 *   when subclasses access the connection concurrently.
 *
 * Usage:
 * - Subclasses may call {@code connect()} to create the connection and {@link #getConnection()} to obtain
 *   the live {@link Connection} wrapped in an {@link Optional}.
 *
 * Note: This class intentionally keeps the Connection as a protected field so that subclasses with special
 * needs can access the underlying JDBC Connection when necessary.
 */
public class BaseDB {
    private static final Logger LOGGER = Logger.getLogger(BaseDB.class.getName());

    /**
     * The live JDBC connection instance. Null when no connection is established.
     * Marked protected so subclasses can access the raw Connection when necessary.
     * Access to this field is guarded by synchronized methods in this class.
     */
    protected Connection connectionInstance;

    /**
     * Establishes a database connection using credentials obtained from {@link ConfigManager}.
     * This method is synchronized to avoid race conditions when used by subclasses concurrently.
     *
     * If a connection is already open and valid, this method does nothing.
     *
     * @throws SQLException if a database access error occurs or required configuration is missing
     */
    protected synchronized void connect() throws SQLException {
        // If a connection already exists and is valid, reuse it.
        try {
            if (!Objects.isNull(connectionInstance) && !connectionInstance.isClosed()) {
                LOGGER.log(Level.FINE, "Reusing existing database connection.");
                return;
            }
        } catch (SQLException ex) {
            // If checking isClosed() failed, clear reference and continue to create a new connection.
            LOGGER.log(Level.WARNING, "Error checking existing connection state; will attempt to (re)connect.", ex);
            connectionInstance = null;
        }

        final String dbUrl = ConfigManager.getDbUrl();
        final String dbUser = ConfigManager.getDbUser();
        final String dbPassword = ConfigManager.getDbPassword();

        if (Objects.isNull(dbUrl) || Objects.isNull(dbUser) || Objects.isNull(dbPassword)
                || dbUrl.isBlank() || dbUser.isBlank()) {
            final String message = "Database configuration incomplete: url/user/password must not be null or blank.";
            LOGGER.log(Level.SEVERE, message);
            throw new SQLException(message);
        }

        // Do not log sensitive information such as the password.
        LOGGER.log(Level.CONFIG, "Attempting database connection to URL: {0} with user: {1}",
                new Object[] { dbUrl, dbUser });

        try {
            connectionInstance = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            LOGGER.log(Level.FINE, "Database connection established.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection.", ex);
            // Ensure reference is cleared on failure.
            connectionInstance = null;
            throw ex;
        } catch (RuntimeException ex) {
            // Wrap unexpected runtime exceptions to provide consistent behavior to callers
            LOGGER.log(Level.SEVERE, "Unexpected error while establishing database connection.", ex);
            connectionInstance = null;
            throw new SQLException("Unexpected error while establishing database connection.", ex);
        }
    }

    /**
     * Closes the current database connection if it is open.
     * This method is synchronized to avoid race conditions when closing while another thread may be using the connection.
     *
     * After this method completes, {@link #connectionInstance} will be null.
     *
     * @throws SQLException if a database access error occurs while closing
     */
    protected synchronized void disconnect() throws SQLException {
        if (Objects.isNull(connectionInstance)) {
            LOGGER.log(Level.FINE, "No database connection to close.");
            return;
        }

        try {
            if (!connectionInstance.isClosed()) {
                try {
                    connectionInstance.close();
                    LOGGER.log(Level.FINE, "Database connection closed successfully.");
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Error occurred while closing database connection.", ex);
                    // Propagate after clearing reference to keep internal state consistent
                    throw ex;
                }
            } else {
                LOGGER.log(Level.FINE, "Database connection was already closed.");
            }
        } catch (SQLException ex) {
            // Ensure internal reference is cleared even if closing threw an exception
            connectionInstance = null;
            throw ex;
        } catch (RuntimeException ex) {
            // Convert unexpected runtime exceptions to SQLException for callers
            LOGGER.log(Level.SEVERE, "Unexpected error while closing database connection.", ex);
            connectionInstance = null;
            throw new SQLException("Unexpected error while closing database connection.", ex);
        } finally {
            // Ensure the reference is nulled to avoid holding onto stale connections
            connectionInstance = null;
        }
    }

    /**
     * Returns an Optional-wrapped Connection if available and open.
     *
     * This method does not create a connection; callers should invoke {@link #connect()} first.
     * The returned Connection is the internal instance and not a defensive copy. Callers must not close it
     * unless they own the lifecycle (prefer {@link #disconnect()} for lifecycle management).
     *
     * @return Optional containing the live Connection or Optional.empty() if none available
     */
    protected synchronized Optional<Connection> getConnection() {
        if (Objects.isNull(connectionInstance)) {
            return Optional.empty();
        }
        try {
            if (connectionInstance.isClosed()) {
                // Clear the stale reference and return empty
                connectionInstance = null;
                return Optional.empty();
            }
            return Optional.of(connectionInstance);
        } catch (SQLException ex) {
            // If we cannot determine state, clear reference and return empty
            LOGGER.log(Level.WARNING, "Error while checking connection state in getConnection().", ex);
            connectionInstance = null;
            return Optional.empty();
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error in getConnection().", ex);
            connectionInstance = null;
            return Optional.empty();
        }
    }
}