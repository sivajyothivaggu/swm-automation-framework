package com.swm.core.listeners;

import org.testng.*;

public class RetryListener implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int MAX_RETRY = 2;
    
    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            return true;
        }
        return false;
    }
}
