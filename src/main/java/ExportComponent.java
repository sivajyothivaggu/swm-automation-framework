package com.swm.ui.components;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.swm.core.base.BaseComponent;

public class ExportComponent extends BaseComponent {
    
    @FindBy(id = "export-excel")
    private WebElement exportExcel;
    
    @FindBy(id = "export-pdf")
    private WebElement exportPdf;
    
    @FindBy(id = "export-csv")
    private WebElement exportCsv;
    
    public void exportAsExcel() {
        exportExcel.click();
    }
    
    public void exportAsPdf() {
        exportPdf.click();
    }
    
    public void exportAsCsv() {
        exportCsv.click();
    }
}
