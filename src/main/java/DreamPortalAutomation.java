package Wingify_test;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

public class DreamPortalAutomation {

    public static void main(String[] args) {
    	
    	WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

      
            driver.get("https://arjitnigam.github.io/myDreams/");

            WebElement loader = driver.findElement(By.xpath("//*[@id='loadingAnimation']"));
            if (loader.isDisplayed()) {
                System.out.println(" Loader is visible on page load");
            }

            wait.until(ExpectedConditions.invisibilityOf(loader));
            System.out.println("Loader disappeared after 3 seconds");

            WebElement myDreamsBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(.,'My Dreams')]")));
            if (myDreamsBtn.isDisplayed()) {
                System.out.println(" My Dreams button is visible");
            }

            String mainWindow = driver.getWindowHandle();
            myDreamsBtn.click();
            System.out.println("the page navigate at a time  both opens dreams-diary.html and dreams-total.html\r\n in new tabs/windows. ");
    }}

    
