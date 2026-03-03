package com.swm.database;

import com.swm.core.base.BaseDB;
import java.sql.*;
import java.util.*;

public class DBConnection extends BaseDB {
    
    public ResultSet executeQuery(String query) throws SQLException {
        connect();
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }
    
    public int executeUpdate(String query) throws SQLException {
        connect();
        Statement stmt = connection.createStatement();
        int result = stmt.executeUpdate(query);
        disconnect();
        return result;
    }
    
    public List<Map<String, Object>> getResultList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), rs.getObject(i));
            }
            resultList.add(row);
        }
        return resultList;
    }
}
