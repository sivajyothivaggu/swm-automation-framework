package com.swm.core.base



public class BaseTest 
    
    @BeforeMethod
    public void setUp() throws InterruptedException 
        DriverManager.initDriver(ConfigManager.getBrowser());
        DriverManager.getDriver().manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(120));
        int retries = 3;
        for (int i = 0; i < retries; i++) {
            try {
                DriverManager.getDriver().get(ConfigManager.getUrl());
                Thread.sleep(3000);
                break;
            } catch (Exception e) {
                if (i == retries - 1) throw e;
                Thread.sleep(10000);
            }
        }
    }
    
    @AfterMethod
    public void tearDown() {
        // Comment out to keep browser open
        // DriverManager.quitDriver();
    }
}
