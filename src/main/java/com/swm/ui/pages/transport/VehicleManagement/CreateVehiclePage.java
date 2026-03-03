package com.swm.ui.pages.transport.VehicleManagement;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BasePage;

public class CreateVehiclePage extends BasePage {
    
    @FindBy(id = "vehicle-number")
    private WebElement vehicleNumber;
    
    @FindBy(id = "vehicle-type")
    private WebElement vehicleType;
    
    @FindBy(id = "capacity")
    private WebElement capacity;
    
    @FindBy(id = "submit-btn")
    private WebElement submitButton;
    
    public void createVehicle(String number, String type, String cap) {
        vehicleNumber.sendKeys(number);
        vehicleType.sendKeys(type);
        capacity.sendKeys(cap);
        submitButton.click();
    }
}
