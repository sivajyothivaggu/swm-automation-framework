package TestNG;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

	public class DreamsDairyValidationTest {

	    WebDriver driver;
	    WebDriverWait wait;

	    @BeforeClass
	    public void setup() {
	        WebDriverManager.chromedriver().setup();
	        driver = new ChromeDriver();
	        driver.manage().window().maximize();
	        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	    }

	    @Test
	    public void validateDreamsDiary() {
	        driver.get("https://arjitnigam.github.io/myDreams/dreams-diary.html");

	        // Wait until rows are visible
	        List<WebElement> rows = wait.until(
	            ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//table/tbody/tr"))
	        );

	        // 1. Validate number of rows
	        Assert.assertEquals(rows.size(), 10, "There should be exactly 10 dream entries");

	        boolean columnCheck = true;
	        boolean typeCheck = true;

	        // 2. Validate each row
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

	        // 3. Assertions for column data
	        Assert.assertTrue(columnCheck, "All rows should have Dream Name, Days Ago, and Dream Type filled");
	        Assert.assertTrue(typeCheck, "All dream types should be either 'Good' or 'Bad'");
	    }

	    @AfterClass
	    public void tearDown() {
	        if (driver != null) {
	            driver.quit();
	        }
	    }
	}


