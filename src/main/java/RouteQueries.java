package com.swm.database;

import java.sql.*;
import java.util.*;

public class RouteQueries {
    private DBConnection dbConnection = new DBConnection();
    
    public List<Map<String, Object>> getAllRoutes() throws SQLException {
        String query = "SELECT * FROM routes";
        ResultSet rs = dbConnection.executeQuery(query);
        return dbConnection.getResultList(rs);
    }
    
    public Map<String, Object> getRouteById(String id) throws SQLException {
        String query = "SELECT * FROM routes WHERE id = '" + id + "'";
        ResultSet rs = dbConnection.executeQuery(query);
        List<Map<String, Object>> result = dbConnection.getResultList(rs);
        return result.isEmpty() ? null : result.get(0);
    }
}
