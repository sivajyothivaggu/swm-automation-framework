package com.swm.ui.pages.dashboard;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BaseComponent;

public class SidebarComponent extends BaseComponent {
    
    @FindBy(id = "transport-link")
    private WebElement transportLink;
    
    @FindBy(id = "vehicle-link")
    private WebElement vehicleLink;
    
    @FindBy(id = "route-link")
    private WebElement routeLink;
    
    public void clickTransport() {
        transportLink.click();
    }
    
    public void clickVehicle() {
        vehicleLink.click();
    }
    
    public void clickRoute() {
        routeLink.click();
    }
}
