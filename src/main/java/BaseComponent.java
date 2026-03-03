package com.swm.core.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import com.swm.core.driver.DriverManager;
import com.swm.core.utils.WaitUtils;

public class BaseComponent {
    protected WebDriver driver;
    protected WaitUtils wait;
    
    public BaseComponent() {
        this.driver = DriverManager.getDriver();
        this.wait = new WaitUtils(driver);
        PageFactory.initElements(driver, this);
    }
}
