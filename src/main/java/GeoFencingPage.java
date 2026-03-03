package com.swm.ui.pages.transport.geofencing;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BasePage;

public class GeoFencingPage extends BasePage {
    
    @FindBy(id = "create-geofence-btn")
    private WebElement createGeofenceButton;
    
    public void clickCreateGeofence() {
        createGeofenceButton.click();
    }
}
