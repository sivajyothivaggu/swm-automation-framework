package com.swm.ui.pages.transport.VehicleManagement;

import com.swm.core.base.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class BaseVehiclePage extends BasePage {
    
    @FindBy(id = "search-box")
    protected WebElement searchBox;
    
    @FindBy(id = "filter-btn")
    protected WebElement filterButton;
    
    @FindBy(id = "export-btn")
    protected WebElement exportButton;
    
    public void searchVehicle(String searchText) {
        searchBox.sendKeys(searchText);
    }
    
    public void clickFilter() {
        filterButton.click();
    }
    
    public void clickExport() {
        exportButton.click();
    }
}
