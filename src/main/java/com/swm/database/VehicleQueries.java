package com.swm.database;

import java.sql.*;
import java.util.*;

public class VehicleQueries {
    private DBConnection dbConnection = new DBConnection();
    
    public List<Map<String, Object>> getAllVehicles() throws SQLException {
        String query = "SELECT * FROM vehicles";
        ResultSet rs = dbConnection.executeQuery(query);
        return dbConnection.getResultList(rs);
    }
    
    public Map<String, Object> getVehicleById(String id) throws SQLException {
        String query = "SELECT * FROM vehicles WHERE id = '" + id + "'";
        ResultSet rs = dbConnection.executeQuery(query);
        List<Map<String, Object>> result = dbConnection.getResultList(rs);
        return result.isEmpty() ? null : result.get(0);
    }
    
    public int deleteVehicle(String id) throws SQLException {
        String query = "DELETE FROM vehicles WHERE id = '" + id + "'";
        return dbConnection.executeUpdate(query);
    }
}
