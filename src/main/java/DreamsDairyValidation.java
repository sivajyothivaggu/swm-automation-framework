package Wingify_test;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class DreamsDairyValidation {

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        try {
            driver.get("https://arjitnigam.github.io/myDreams/dreams-diary.html");

            // Wait until rows are visible
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            List<WebElement> rows = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//table/tbody/tr"))
            );

            // Validate number of rows
            if (rows.size() == 10) {
                System.out.println(" Exactly 10 dream entries present" );
            } else {
                System.out.println(" Expected 10 dream entries but found: " + rows.size());
            }

            boolean typeCheck = true;
            boolean columnCheck = true;

            // Validate each row
            for (WebElement row : rows) {
                List<WebElement> columns = row.findElements(By.tagName("td"));

                if (columns.size() == 3) {
                    String dreamName = columns.get(0).getText().trim();
                    String daysAgo = columns.get(1).getText().trim();
                    String dreamType = columns.get(2).getText().trim();

                    if (dreamName.isEmpty() || daysAgo.isEmpty() || dreamType.isEmpty()) {
                        columnCheck = false;
                    }

                    if (!(dreamType.equalsIgnoreCase("Good") || dreamType.equalsIgnoreCase("Bad"))) {
                        typeCheck = false;
                    }
                } else {
                    columnCheck = false;
                }
            }

            if (columnCheck) {
                System.out.println(" All rows have Dream Name, Days Ago, and Dream Type filled");
            } else {
                System.out.println(" Some rows have missing data in columns");
            }

            if (typeCheck) {
                System.out.println(" The dream types are only “Good” or “Bad”.");
            } else {
                System.out.println(" Some dream types are invalid");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }
}
