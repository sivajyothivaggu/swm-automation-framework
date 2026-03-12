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
 * <p>Responsibilities:
 * - Establish and close a JDBC Connection using configuration provided by {@link ConfigManager}.
 * - Expose an Optional-based accessor for the current connection.
 * - Provide robust logging and error handling.</p>
 *
 * <p>Threading:
 * The {@link #connect()} and {@link #disconnect()} methods are synchronized to avoid race
 * conditions when subclasses access the connection concurrently.</p>
 *
 * <p>Usage:
 * Subclasses may call {@link #connect()} to create the connection and {@link #getConnection()}
 * to obtain the live {@link Connection} wrapped in an {@link Optional}.</p>
 *
 * <p>Note: The connection is protected so subclasses with special needs can access the raw
 * JDBC {@link Connection} when necessary.</p>
 */
public class BaseDB {
    private static final Logger LOGGER = Logger.getLogger(BaseDB.class.getName());

    /**
     * The live JDBC connection instance. Null when no connection is established.
     */
    protected Connection connectionInstance;

    /**
     * Establishes a database connection using credentials obtained from {@link ConfigManager}.
     * This method is synchronized to avoid race conditions when used by subclasses concurrently.
     *
     * @throws SQLException if a database access error occurs or required configuration is missing
     */
    protected synchronized void connect() throws SQLException {
        final String dbUrl = ConfigManager.getDbUrl();
        final String dbUser = ConfigManager.getDbUser();
        final String dbPassword = ConfigManager.getDbPassword();

        if (Objects.isNull(dbUrl) || Objects.isNull(dbUser) || Objects.isNull(dbPassword)) {
            final String missing = buildMissingConfigMessage(dbUrl, dbUser, dbPassword);
            LOGGER.log(Level.SEVERE, missing);
            throw new SQLException(missing);
        }

        if (connectionInstance != null) {
            try {
                if (!connectionInstance.isClosed()) {
                    LOGGER.log(Level.FINE, "connect() called but connection is already open.");
                    return;
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Error checking existing connection state.", ex);
                // continue to attempt a new connection
            }
        }

        try {
            connectionInstance = DriverManager.getConnection(dbUrl.trim(), dbUser.trim(),
                    dbPassword);
            LOGGER.log(Level.FINE, "Database connection established.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection.", ex);
            throw ex;
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE,
                    "Unexpected runtime error while establishing database connection.", ex);
            throw new SQLException("Unexpected error while establishing database connection.", ex);
        }
    }

    /**
     * Closes the current database connection if it is open.
     * This method is synchronized to avoid race conditions when closing while another thread may
     * be using the connection.
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
                try {
                    connectionInstance.close();
                    closedSuccessfully = true;
                    LOGGER.log(Level.FINE, "Database connection closed.");
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error while closing database connection.", ex);
                    throw ex;
                }
            } else {
                LOGGER.log(Level.FINE, "Database connection was already closed.");
                closedSuccessfully = true;
            }
        } catch (SQLException ex) {
            // already logged above; propagate
            throw ex;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error while closing database connection.", ex);
            throw new SQLException("Unexpected error while closing database connection.", ex);
        } finally {
            // Ensure reference is cleared so subsequent operations know there is no active
            // connection.
            connectionInstance = null;
            if (!closedSuccessfully) {
                LOGGER.log(Level.FINER, "Connection reference cleared after attempted close.");
            }
        }
    }

    /**
     * Returns an Optional containing the current {@link Connection} if present.
     *
     * @return Optional of Connection
     */
    protected Optional<Connection> getConnection() {
        return Optional.ofNullable(connectionInstance);
    }

    /**
     * Helper to build a descriptive message for missing DB configuration values.
     *
     * @param url the database URL
     * @param user the database username
     * @param password the database password
     * @return a descriptive error message
     */
    private String buildMissingConfigMessage(final String url, final String user,
            final String password) {
        final StringBuilder sb = new StringBuilder(
                "Database configuration incomplete: the following required values are missing:");
        if (Objects.isNull(url)) {
            sb.append(" url");
        }
        if (Objects.isNull(user)) {
            sb.append(" user");
        }
        if (Objects.isNull(password)) {
            sb.append(" password");
        }
        sb.append('.');
        return sb.toString();
    }
}