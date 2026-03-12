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
     * Marked volatile to ensure visibility across threads.
     */
    protected volatile Connection connectionInstance;

    /**
     * Establishes a database connection using credentials obtained from ConfigManager.
     * This method is synchronized to avoid race conditions when used by subclasses concurrently.
     *
     * If a connection already exists and is open, this method is a no-op.
     *
     * @throws SQLException if a database access error occurs or required configuration is missing
     */
    protected synchronized void connect() throws SQLException {
        try {
            if (!Objects.isNull(connectionInstance) && !connectionInstance.isClosed()) {
                LOGGER.log(Level.FINE, "Connection already established; skipping connect().");
                return;
            }
        } catch (SQLException ex) {
            // If checking isClosed() fails, ensure we treat as not connected and attempt to re-establish.
            LOGGER.log(Level.WARNING, "Error while checking connection state prior to connect(). Attempting to re-establish.", ex);
            // proceed to attempt establishing a new connection
            connectionInstance = null;
        }

        final String dbUrl = ConfigManager.getDbUrl();
        final String dbUser = ConfigManager.getDbUser();
        final String dbPassword = ConfigManager.getDbPassword();

        if (Objects.isNull(dbUrl) || Objects.isNull(dbUser) || Objects.isNull(dbPassword)) {
            final String message = "Database configuration incomplete: url/user/password must not be null.";
            LOGGER.log(Level.SEVERE, message);
            throw new SQLException(message);
        }

        // Avoid logging sensitive information (do not log password)
        LOGGER.log(Level.FINE, "Attempting to establish database connection to URL: {0} with user: {1}", new Object[] { dbUrl, dbUser });

        try {
            connectionInstance = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            LOGGER.log(Level.FINE, "Database connection established.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection.", ex);
            // Ensure no partially initialized connection reference is retained
            connectionInstance = null;
            throw ex;
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error while establishing database connection.", ex);
            connectionInstance = null;
            throw new SQLException("Unexpected error while establishing database connection.", ex);
        }
    }

    /**
     * Closes the current database connection if it is open.
     * This method is synchronized to avoid race conditions when closing while another thread may be using the connection.
     *
     * After successful or attempted close the internal reference is cleared.
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
            }
        } finally {
            // Ensure reference is cleared so subsequent operations know there is no active connection.
            connectionInstance = null;
            if (!closedSuccessfully) {
                LOGGER.log(Level.FINER, "Connection reference cleared after attempted close.");
            }
        }
    }

    /**
     * Returns an Optional containing the current Connection if present and open.
     * If the connection has been closed or an error occurs while checking, an empty Optional is returned.
     *
     * @return Optional of Connection
     */
    protected Optional<Connection> getConnection() {
        if (Objects.isNull(connectionInstance)) {
            return Optional.empty();
        }
        try {
            if (connectionInstance.isClosed()) {
                // Clear stale reference to avoid future incorrect assumptions.
                connectionInstance = null;
                return Optional.empty();
            }
            return Optional.of(connectionInstance);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error while verifying connection state in getConnection(). Returning empty Optional.", ex);
            connectionInstance = null;
            return Optional.empty();
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error while accessing the database connection.", ex);
            connectionInstance = null;
            return Optional.empty();
        }
    }
}