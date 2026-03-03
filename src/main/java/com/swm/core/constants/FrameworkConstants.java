package com.swm.core.constants;

public class FrameworkConstants {
    public static final int EXPLICIT_WAIT = 20;
    public static final int IMPLICIT_WAIT = 10;
    public static final int PAGE_LOAD_TIMEOUT = 30;
    
    public static final String REPORTS_PATH = System.getProperty("user.dir") + "/reports/";
    public static final String LOGS_PATH = System.getProperty("user.dir") + "/logs/";
    public static final String SCREENSHOTS_PATH = REPORTS_PATH + "screenshots/";
    public static final String TESTDATA_PATH = System.getProperty("user.dir") + "/src/test/resources/testdata/";
}
