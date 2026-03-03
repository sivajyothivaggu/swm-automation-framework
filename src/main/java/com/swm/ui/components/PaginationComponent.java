package com.swm.ui.components;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BaseComponent;

public class PaginationComponent extends BaseComponent {
    
    @FindBy(id = "next-page")
    private WebElement nextPage;
    
    @FindBy(id = "prev-page")
    private WebElement prevPage;
    
    @FindBy(id = "page-number")
    private WebElement pageNumber;
    
    public void goToNextPage() {
        nextPage.click();
    }
    
    public void goToPreviousPage() {
        prevPage.click();
    }
    
    public String getCurrentPage() {
        return pageNumber.getText();
    }
}
