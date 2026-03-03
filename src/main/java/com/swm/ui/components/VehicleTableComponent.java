package com.swm.ui.components;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BaseComponent;
import java.util.List;

public class VehicleTableComponent extends BaseComponent {
    
    @FindBy(xpath = "//table[@id='vehicle-table']//tbody//tr")
    private List<WebElement> tableRows;
    
    @FindBy(xpath = "//table[@id='vehicle-table']//th")
    private List<WebElement> tableHeaders;
    
    public int getRowCount() {
        return tableRows.size();
    }
    
    public List<WebElement> getTableRows() {
        return tableRows;
    }
}
