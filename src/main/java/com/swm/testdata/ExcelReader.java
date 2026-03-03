package com.swm.testdata;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.*;

public class ExcelReader {
    
    public static List<Map<String, String>> readExcel(String filePath, String sheetName) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheet(sheetName);
        
        Row headerRow = sheet.getRow(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Map<String, String> rowData = new HashMap<>();
            
            for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                String header = headerRow.getCell(j).getStringCellValue();
                String value = cell != null ? cell.toString() : "";
                rowData.put(header, value);
            }
            data.add(rowData);
        }
        
        workbook.close();
        fis.close();
        return data;
    }
}
