package com.swm.ui.pages.transport;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BasePage;
import java.util.List;

public class TransportPage extends BasePage {
    
    @FindBy(tagName = "a")
    private List<WebElement> links;
    
    public void clickTransportModule() {
        // Try multiple locators for Transport module
        List<By> transportLocators = List.of(
            By.xpath("//*[contains(text(), 'Transport')]"),
            By.xpath("//a[contains(text(), 'Transport')]"),
            By.xpath("//div[contains(text(), 'Transport')]"),
            By.linkText("Transport"),
            By.partialLinkText("Transport")
        );
        
        for (By locator : transportLocators) {
            try {
                wait.waitForElementClickable(locator).click();
                return;
            } catch (Exception e) {
                // Try next locator
            }
        }
        throw new RuntimeException("Could not find Transport module");
    }
}
