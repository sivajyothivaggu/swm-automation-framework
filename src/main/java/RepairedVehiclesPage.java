package com.swm.ui.pages.transport.repaired;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BasePage;

public class RepairedVehiclesPage extends BasePage {
    
    @FindBy(id = "repaired-vehicles-table")
    private WebElement repairedVehiclesTable;
    
    public boolean isTableDisplayed() {
        return repairedVehiclesTable.isDisplayed();
    }
}
