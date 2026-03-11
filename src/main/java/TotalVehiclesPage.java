package com.swm.ui.pages.transport.VehicleManagement.TotalVehicles;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.swm.ui.pages.transport.VehicleManagement.BaseVehiclePage;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class TotalVehiclesPage extends BaseVehiclePage {
    
    @FindBy(xpath = "//*[contains(text(), 'Vehicle Management')]")
    private WebElement vehicleManagementCard;
    
    @FindBy(xpath = "//input[@placeholder='AP36AATB2189' or contains(@placeholder, 'Search') or contains(@placeholder, 'vehicle')]")
    private WebElement searchBox;
    
    @FindBy(xpath = "//button[normalize-space()='Search'] | //button[contains(@class, 'search')] | //button[@type='submit']")
    private WebElement searchButton;
    
    @FindBy(xpath = "//span[contains(text(), 'active') or contains(@class, 'badge')]")
    private WebElement activeFilterBadge;
    
    @FindBy(xpath = "//input[@placeholder='dd/mm/yyyy'][1] | //input[contains(@placeholder, 'dd/mm/yyyy')][1] | //label[contains(text(), 'From')]/following::input[1]")
    private WebElement fromDateField;
    
    @FindBy(xpath = "//input[@placeholder='dd/mm/yyyy'][2] | //input[contains(@placeholder, 'dd/mm/yyyy')][2] | //label[contains(text(), 'To')]/following::input[1]")
    private WebElement toDateField;
    
    @FindBy(xpath = "//button[contains(text(), 'Clear All') or contains(@class, 'clear')]")
    private WebElement clearAllButton;
    
    @FindBy(xpath = "//table//tbody//tr")
    private List<WebElement> tableRows;
    
    @FindBy(xpath = "//*[contains(text(), 'No Data Found') or contains(text(), 'No records') or contains(text(), 'No data')]")
    private WebElement noDataMessage;
    
    @FindBy(xpath = "//button[contains(text(), 'Create Vehicle')]")
    private WebElement createVehicleButton;
    
    public void navigateToVehicleManagement() {
        wait.waitForElementClickable(By.xpath("//*[contains(text(), 'Vehicle Management')]")).click();
    }
    
    public void navigateToTotalVehicles() {
        wait.waitForElementClickable(By.xpath("//*[contains(text(), 'Total Vehicles')]/ancestor::*[contains(@class, 'card')]")).click();
        wait.waitForElementVisible(By.xpath("//button[contains(text(), 'Create Vehicle')]"));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void enterSearchText(String text) {
        WebDriverWait explicitWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        explicitWait.until(ExpectedConditions.elementToBeClickable(searchBox));
        searchBox.clear();
        searchBox.sendKeys(text);
    }
    
    public void clickSearchButton() {
        try {
            WebDriverWait explicitWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            explicitWait.until(ExpectedConditions.elementToBeClickable(searchBox));
            searchBox.sendKeys(Keys.ENTER);
            waitForTableToLoad();
        } catch (Exception e) {
            # System.out.println("Search button not found, search may happen automatically");  # TODO: Use logging
            waitForTableToLoad();
        }
    }
    
    public void clearSearch() {
        searchBox.clear();
        searchBox.sendKeys(Keys.BACK_SPACE);
        waitForTableToLoad();
    }
    
    public void selectVehicleType(String type) {
        selectDropdownFilter("All Vehicle Types", type);
    }
    
    public void selectStatus(String status) {
        selectDropdownFilter("All Status", status);
    }
    
    public void selectWard(String ward) {
        selectDropdownFilter("All Wards", ward);
    }
    
    public void selectDepartment(String department) {
        selectDropdownFilter("All Departments", department);
    }
    
    private void selectDropdownFilter(String filterName, String optionValue) {
        WebDriverWait explicitWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        // Find and click the dropdown button
        By dropdownLocator = By.xpath("//button[contains(text(), '" + filterName + "')]" +
                                      " | //button[contains(., '" + filterName + "')]" +
                                      " | //div[contains(text(), '" + filterName + "')]/parent::button");
        
        WebElement dropdown = explicitWait.until(ExpectedConditions.elementToBeClickable(dropdownLocator));
        dropdown.click();
        
        // Wait for dropdown options to appear and select the option
        By optionLocator = By.xpath("//button[normalize-space()='" + optionValue + "']" +
                                   " | //button[contains(text(), '" + optionValue + "')]" +
                                   " | //li[normalize-space()='" + optionValue + "']" +
                                   " | //div[normalize-space()='" + optionValue + "']");
        
        WebElement option = explicitWait.until(ExpectedConditions.elementToBeClickable(optionLocator));
        option.click();
        
        // Small wait for dropdown to process selection
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void enterFromDate(String date) {
        try {
            WebDriverWait explicitWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            explicitWait.until(ExpectedConditions.elementToBeClickable(fromDateField));
            fromDateField.clear();
            fromDateField.sendKeys(date);
        } catch (Exception e) {
            # System.out.println("From Date field not available: " + e.getMessage());  # TODO: Use logging
        }
    }
    
    public void enterToDate(String date) {
        try {
            WebDriverWait explicitWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            explicitWait.until(ExpectedConditions.elementToBeClickable(toDateField));
            toDateField.clear();
            toDateField.sendKeys(date);
        } catch (Exception e) {
            # System.out.println("To Date field not available: " + e.getMessage());  # TODO: Use logging
        }
    }
    
    public void clickClearAll() {
        clearAllButton.click();
        waitForTableToLoad();
    }
    
    public int getTableRowCount() {
        waitForTableToLoad();
        return tableRows.size();
    }
    
    private void waitForTableToLoad() {
        WebDriverWait explicitWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            // Wait for either table rows or no data message
            explicitWait.until(driver -> 
                !driver.findElements(By.xpath("//table//tbody//tr")).isEmpty() ||
                !driver.findElements(By.xpath("//*[contains(text(), 'No Data') or contains(text(), 'No data')]")).isEmpty()
            );
        } catch (Exception e) {
            // Table might be empty, continue
        }
    }
    
    public List<String> getColumnValues(int columnIndex) {
        waitForTableToLoad();
        // Re-fetch table rows to avoid stale element
        List<WebElement> freshRows = driver.findElements(By.xpath("//table//tbody//tr"));
        return freshRows.stream()
            .map(row -> {
                try {
                    List<WebElement> cells = row.findElements(By.tagName("td"));
                    if (cells.size() > columnIndex) {
                        return cells.get(columnIndex).getText().trim();
                    }
                } catch (Exception e) {
                    // Handle stale element
                }
                return "";
            })
            .filter(text -> !text.isEmpty())
            .collect(Collectors.toList());
    }
    
    public String getColumnValueByHeader(String headerName, int rowIndex) {
        waitForTableToLoad();
        List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th"));
        int columnIndex = -1;
        
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).getText().trim().equalsIgnoreCase(headerName)) {
                columnIndex = i;
                break;
            }
        }
        
        if (columnIndex == -1 || tableRows.size() <= rowIndex) {
            return "";
        }
        
        List<WebElement> cells = tableRows.get(rowIndex).findElements(By.tagName("td"));
        return cells.size() > columnIndex ? cells.get(columnIndex).getText().trim() : "";
    }
    
    public boolean isNoDataMessageDisplayed() {
        try {
            return noDataMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isTableDisplayed() {
        return !tableRows.isEmpty();
    }
    
    public String getActiveFilterCount() {
        try {
            return activeFilterBadge.getText().trim();
        } catch (Exception e) {
            return "0";
        }
    }
    
    public boolean isActiveFilterBadgeDisplayed() {
        try {
            return activeFilterBadge.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getSearchText() {
        return searchBox.getAttribute("value");
    }
    
    public boolean verifyColumnContainsValue(String headerName, String expectedValue) {
        List<WebElement> headers = driver.findElements(By.xpath("//table//thead//th"));
        int columnIndex = -1;
        
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).getText().trim().equalsIgnoreCase(headerName)) {
                columnIndex = i;
                break;
            }
        }
        
        if (columnIndex == -1) return false;
        
        List<String> columnValues = getColumnValues(columnIndex);
        return columnValues.stream().anyMatch(value -> 
            value.toLowerCase().contains(expectedValue.toLowerCase()));
    }
    
    public boolean isTotalVehiclesPageDisplayed() {
        try {
            return createVehicleButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
