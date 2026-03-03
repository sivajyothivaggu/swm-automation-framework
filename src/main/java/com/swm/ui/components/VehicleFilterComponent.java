package com.swm.ui.components;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BaseComponent;

public class VehicleFilterComponent extends BaseComponent {
    
    @FindBy(id = "status-filter")
    private WebElement statusFilter;
    
    @FindBy(id = "type-filter")
    private WebElement typeFilter;
    
    @FindBy(id = "apply-filter-btn")
    private WebElement applyFilterButton;
    
    public void filterByStatus(String status) {
        statusFilter.sendKeys(status);
        applyFilterButton.click();
    }
    
    public void filterByType(String type) {
        typeFilter.sendKeys(type);
        applyFilterButton.click();
    }
}
