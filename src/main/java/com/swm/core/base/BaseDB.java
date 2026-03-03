package com.swm.core.base;

import java.sql.*;
import com.swm.core.config.ConfigManager;

public class BaseDB {
    protected Connection connection;
    
    protected void connect() throws SQLException {
        connection = DriverManager.getConnection(
            ConfigManager.getDbUrl(),
            ConfigManager.getDbUser(),
            ConfigManager.getDbPassword()
        );
    }
    
    protected void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
