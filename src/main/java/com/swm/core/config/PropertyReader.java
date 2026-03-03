package com.swm.core.config;

import java.io.*;
import java.util.Properties;

public class PropertyReader {
    private Properties properties;
    
    public PropertyReader(String filePath) {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
