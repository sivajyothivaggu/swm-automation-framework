package com.swm.testdata;

import java.util.*;

public class TestDataManager {
    private static Map<String, Object> testData = new HashMap<>();
    
    public static void setData(String key, Object value) {
        testData.put(key, value);
    }
    
    public static Object getData(String key) {
        return testData.get(key);
    }
    
    public static void clearData() {
        testData.clear();
    }
}
