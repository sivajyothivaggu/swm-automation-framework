package com.swm.core.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import com.swm.core.constants.FrameworkConstants;
import java.time.Duration;

public class WaitUtils {
    private WebDriver driver;
    private WebDriverWait wait;
    
    public WaitUtils(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(FrameworkConstants.EXPLICIT_WAIT));
    }
    
    public WebElement waitForElementVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    public WebElement waitForElementClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    public boolean waitForElementInvisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
}
