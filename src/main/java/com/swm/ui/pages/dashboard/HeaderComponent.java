package com.swm.ui.pages.dashboard;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BaseComponent;

public class HeaderComponent extends BaseComponent {
    
    @FindBy(id = "user-profile")
    private WebElement userProfile;
    
    @FindBy(id = "logout-btn")
    private WebElement logoutButton;
    
    public void logout() {
        userProfile.click();
        logoutButton.click();
    }
}
