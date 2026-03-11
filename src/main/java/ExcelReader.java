package com.swm.testdata;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility to read Excel (.xlsx) files and return sheet data as a list of maps.
 *
 * <p>Each map in the returned list represents a row keyed by the header names from the
 * first row (row 0) of the specified sheet. Empty header cells are assigned a
 * generated name "Column{index}". Blank or missing cells are returned as empty strings.
 *
 * <p>This class uses Apache POI to read the workbook and SLF4J for logging.
 */
public class ExcelReader {

    private static final Logger logger = LoggerFactory.getLogger(ExcelReader.class);

    /**
     * Reads the named sheet from the provided Excel (.xlsx) file and returns the data
     * as a list of maps where each map is a row keyed by the header column names.
     *
     * @param filePath  the path to the Excel file; must not be null
     * @param sheetName the name of the sheet to read; must not be null
     * @return a list of maps representing the sheet rows (excluding the header row)
     * @throws IOException if the file cannot be read or the sheet/header is missing
     */
    public static List<Map<String, String>> readExcel(String filePath, String sheetName) throws IOException {
        Objects.requireNonNull(filePath, "filePath must not be null");
        Objects.requireNonNull(sheetName, "sheetName must not be null");

        Path path = Path.of(filePath);
        if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
            logger.error("Excel file is not accessible: {}", filePath);
            throw new IOException("Excel file is not accessible: " + filePath);
        }

        List<Map<String, String>> records = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (Objects.isNull(sheet)) {
                logger.error("Sheet '{}' not found in file '{}'", sheetName, filePath);
                throw new IOException("Sheet '" + sheetName + "' not found in file: " + filePath);
            }

            Row headerRow = sheet.getRow(0);
            if (Objects.isNull(headerRow)) {
                logger.error("Header row (row 0) is missing in sheet '{}' of file '{}'", sheetName, filePath);
                throw new IOException("Header row (row 0) is missing in sheet: " + sheetName);
            }

            int lastRowNum = sheet.getLastRowNum();
            int lastCellNum = Math.max(0, headerRow.getLastCellNum()); // headerRow.getLastCellNum() can be -1

            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (Objects.isNull(row)) {
                    logger.debug("Skipping empty row at index {} in sheet '{}'", i, sheetName);
                    continue;
                }

                Map<String, String> rowData = new HashMap<>();
                for (int j = 0; j < lastCellNum; j++) {
                    Cell headerCell = headerRow.getCell(j);
                    String header = formatter.formatCellValue(headerCell);
                    if (header == null || header.isEmpty()) {
                        header = "Column" + j;
                    }

                    Cell cell = row.getCell(j);
                    String value = cell != null ? formatter.formatCellValue(cell) : "";
                    rowData.put(header, value);
                }
                records.add(rowData);
            }

        } catch (IOException e) {
            logger.error("I/O error while reading Excel file '{}': {}", filePath, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Unexpected error while reading Excel file '{}': {}", filePath, e.getMessage(), e);
            throw new IOException("Failed to read Excel file: " + filePath, e);
        }

        logger.info("Successfully read {} data rows from sheet '{}' in file '{}'", records.size(), sheetName, filePath);
        return records;
    }
}