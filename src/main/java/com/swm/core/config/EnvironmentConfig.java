package com.swm.core.config;

public class EnvironmentConfig {
    private String url;
    private String apiUrl;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    
    public EnvironmentConfig(String env) {
        PropertyReader reader = new PropertyReader(env + ".properties");
        this.url = reader.getProperty("app.url");
        this.apiUrl = reader.getProperty("api.url");
        this.dbUrl = reader.getProperty("db.url");
        this.dbUser = reader.getProperty("db.user");
        this.dbPassword = reader.getProperty("db.password");
    }
    
    public String getUrl() { return url; }
    public String getApiUrl() { return apiUrl; }
    public String getDbUrl() { return dbUrl; }
    public String getDbUser() { return dbUser; }
    public String getDbPassword() { return dbPassword; }
}
