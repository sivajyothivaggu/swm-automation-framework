package com.swm.ui.pages.transport.vehiclemaster;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BasePage;

public class VehicleMasterManagementPage extends BasePage {
    
    @FindBy(id = "add-master-btn")
    private WebElement addMasterButton;
    
    public void clickAddMaster() {
        addMasterButton.click();
    }
}
