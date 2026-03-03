package com.swm.core.utils;

import org.openqa.selenium.*;
import com.swm.core.driver.DriverManager;
import com.swm.core.constants.FrameworkConstants;
import java.io.*;
import java.nio.file.*;

public class ScreenshotUtils {
    
    public static String captureScreenshot(String testName) {
        TakesScreenshot ts = (TakesScreenshot) DriverManager.getDriver();
        File source = ts.getScreenshotAs(OutputType.FILE);
        String destination = FrameworkConstants.SCREENSHOTS_PATH + testName + "_" + DateUtils.getCurrentTimestamp() + ".png";
        
        try {
            Files.createDirectories(Paths.get(FrameworkConstants.SCREENSHOTS_PATH));
            Files.copy(source.toPath(), Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return destination;
    }
}
