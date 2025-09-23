package TestNG;



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

	import java.time.Duration;
	import java.util.List;
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.Map;

	public class DreamsStatsValidationTest {

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
	    public void validateDreamStats() {
	        driver.get("https://arjitnigam.github.io/myDreams/dreams-diary.html");

	        // Wait for table rows
	        List<WebElement> rows = wait.until(
	                ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//tbody/tr"))
	        );

	        int goodCount = 0;
	        int badCount = 0;
	        int recurringCount = 0;

	        // Map to track frequency
	        Map<String, Integer> dreamFrequency = new HashMap<>();

	        // Recurring dreams
	        List<String> recurringDreamNames = new ArrayList<>();
	        recurringDreamNames.add("Flying over mountains");
	        recurringDreamNames.add("Lost in maze");

	        for (WebElement row : rows) {
	            List<WebElement> cols = row.findElements(By.tagName("td"));
	            if (cols.size() != 3) continue;

	            String dreamName = cols.get(0).getText().trim();
	            String dreamType = cols.get(2).getText().trim();

	            if (dreamType.equalsIgnoreCase("Good")) goodCount++;
	            else if (dreamType.equalsIgnoreCase("Bad")) badCount++;

	            dreamFrequency.put(dreamName, dreamFrequency.getOrDefault(dreamName, 0) + 1);
	        }

	        // Count recurring dreams
	        for (String recurring : recurringDreamNames) {
	            if (dreamFrequency.getOrDefault(recurring, 0) > 1) {
	                recurringCount++;
	            }
	        }

	        int totalDreams = rows.size();

	        // TestNG assertions
	        Assert.assertEquals(goodCount, 6, "Good Dreams count is incorrect");
	        Assert.assertEquals(badCount, 4, "Bad Dreams count is incorrect");
	        Assert.assertEquals(totalDreams, 10, "Total Dreams count is incorrect");
	        Assert.assertEquals(recurringCount, 2, "Recurring Dreams count is incorrect");
	    }

	    @AfterClass
	    public void tearDown() {
	        if (driver != null) driver.quit();
	    }
	}


