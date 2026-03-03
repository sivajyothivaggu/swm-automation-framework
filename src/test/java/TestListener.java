package com.swm.core.listeners;

import org.testng.*;
import com.swm.core.utils.ScreenshotUtils;
import com.swm.core.utils.LoggerUtils;

public class TestListener implements ITestListener {
    
    @Override
    public void onTestStart(ITestResult result) {
        LoggerUtils.info("Test Started: " + result.getName());
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        LoggerUtils.info("Test Passed: " + result.getName());
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        LoggerUtils.error("Test Failed: " + result.getName());
        ScreenshotUtils.captureScreenshot(result.getName());
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        LoggerUtils.warn("Test Skipped: " + result.getName());
    }
}
