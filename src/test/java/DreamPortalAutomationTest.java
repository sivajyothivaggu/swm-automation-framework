package TestNG;



	import java.time.Duration;
	import java.util.Set;

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

	public class DreamPortalAutomationTest {
	    WebDriver driver;
	    WebDriverWait wait;

	    @BeforeClass
	    public void setup() {
	        WebDriverManager.chromedriver().setup();
	        driver = new ChromeDriver();
	        driver.manage().window().maximize();
	        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	    }

	    @Test
	    public void testDreamPortal() {
	        // 1. Open Dream Portal Home page
	        driver.get("https://arjitnigam.github.io/myDreams/");
	        System.out.println("Dream Portal Home page opened");

	        // 2. Check loader appears
	        WebElement loader = driver.findElement(By.id("loadingAnimation"));
	        Assert.assertTrue(loader.isDisplayed(), "Loader should be visible on page load");
	        System.out.println("Loader is visible on page load");

	        // 3. Wait for loader to disappear
	        wait.until(ExpectedConditions.invisibilityOf(loader));
	        System.out.println("Loader disappeared after 3 seconds successfully");

	        // 4. Verify My Dreams button visible
	        WebElement myDreamsBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(.,'My Dreams')]")));
	        Assert.assertTrue(myDreamsBtn.isDisplayed(), "My Dreams button should be visible");
	        System.out.println("My Dreams button is visible");

	        // 5. Click My Dreams button → new tabs open
	        String mainWindow = driver.getWindowHandle();
	        myDreamsBtn.click();
	        System.out.println("My Dreams button clicked");

	        // 6. Handle multiple windows
	        Set<String> allWindows = driver.getWindowHandles();
	        Assert.assertTrue(allWindows.size() > 1, "New tabs should open after clicking My Dreams");

	        for (String window : allWindows) {
	            if (!window.equals(mainWindow)) {
	                driver.switchTo().window(window);
	                System.out.println("Switched to window: " + driver.getTitle());
	            }
	        }
	    }

	    @AfterClass
	    public void tearDown() {
	        driver.quit();
	        System.out.println("Browser closed");
	    }
	}


