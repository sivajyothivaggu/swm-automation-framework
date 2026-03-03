package com.swm.ui.pages.transport.route;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BasePage;

public class RouteManagementPage extends BasePage {
    
    @FindBy(id = "create-route-btn")
    private WebElement createRouteButton;
    
    @FindBy(id = "route-name")
    private WebElement routeName;
    
    public void clickCreateRoute() {
        createRouteButton.click();
    }
    
    public void enterRouteName(String name) {
        routeName.sendKeys(name);
    }
}
