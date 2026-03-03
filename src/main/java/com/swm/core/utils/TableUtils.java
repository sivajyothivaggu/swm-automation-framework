package com.swm.core.utils;

import org.openqa.selenium.*;
import java.util.*;

public class TableUtils {
    
    public static List<WebElement> getTableRows(WebDriver driver, By tableLocator) {
        WebElement table = driver.findElement(tableLocator);
        return table.findElements(By.tagName("tr"));
    }
    
    public static List<String> getColumnData(WebDriver driver, By tableLocator, int columnIndex) {
        List<String> columnData = new ArrayList<>();
        List<WebElement> rows = getTableRows(driver, tableLocator);
        
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() > columnIndex) {
                columnData.add(cells.get(columnIndex).getText());
            }
        }
        return columnData;
    }
}
