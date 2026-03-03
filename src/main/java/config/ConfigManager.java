package com.swm.core.config;

public class ConfigManager {
    private static EnvironmentConfig config;
    private static String browser;
    
    static {
        String env = System.getProperty("env", "qa");
        browser = System.getProperty("browser", "chrome");
        config = new EnvironmentConfig(env);
    }
    
    public static String getUrl() {
        return config.getUrl();
    }
    
    public static String getApiUrl() {
        return config.getApiUrl();
    }
    
    public static String getDbUrl() {
        return config.getDbUrl();
    }
    
    public static String getDbUser() {
        return config.getDbUser();
    }
    
    public static String getDbPassword() {
        return config.getDbPassword();
    }
    
    public static String getBrowser() {
        return browser;
    }
}
