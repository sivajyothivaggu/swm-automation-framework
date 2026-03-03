package com.swm.ui.components;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BaseComponent;

public class CommonModalComponent extends BaseComponent {
    
    @FindBy(id = "modal-title")
    private WebElement modalTitle;
    
    @FindBy(id = "modal-close")
    private WebElement closeButton;
    
    @FindBy(id = "modal-confirm")
    private WebElement confirmButton;
    
    public String getModalTitle() {
        return modalTitle.getText();
    }
    
    public void closeModal() {
        closeButton.click();
    }
    
    public void confirmModal() {
        confirmButton.click();
    }
}
