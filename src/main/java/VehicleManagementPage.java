package com.swm.ui.pages.transport.VehicleManagement;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class VehicleManagementPage extends BaseVehiclePage {
    
    @FindBy(id = "total-vehicles-tab")
    private WebElement totalVehiclesTab;
    
    @FindBy(id = "active-vehicles-tab")
    private WebElement activeVehiclesTab;
    
    @FindBy(id = "idle-vehicles-tab")
    private WebElement idleVehiclesTab;
    
    @FindBy(id = "halted-vehicles-tab")
    private WebElement haltedVehiclesTab;
    
    @FindBy(id = "create-vehicle-btn")
    private WebElement createVehicleButton;
    
    public void navigateToTotalVehicles() {
        totalVehiclesTab.click();
    }
    
    public void navigateToActiveVehicles() {
        activeVehiclesTab.click();
    }
    
    public void navigateToIdleVehicles() {
        idleVehiclesTab.click();
    }
    
    public void navigateToHaltedVehicles() {
        haltedVehiclesTab.click();
    }
    
    public void clickCreateVehicle() {
        createVehicleButton.click();
    }
    
    public void searchVehicle(String searchText) {
        super.searchVehicle(searchText);
    }
}
