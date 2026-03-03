package Wingify_test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DreamsStatsValidation {

    public static void main(String[] args) {

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://arjitnigam.github.io/myDreams/dreams-diary.html");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Wait for table rows
        List<WebElement> rows = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//tbody/tr"))
        );

        int goodCount = 0;
        int badCount = 0;
        int recurringCount = 0;

        // Use Map to track how many times each dream appears
        Map<String, Integer> dreamFrequency = new HashMap<>();

        // Only these two dreams are considered recurring
        List<String> recurringDreamNames = new ArrayList<>();
        recurringDreamNames.add("Flying over mountains");
        recurringDreamNames.add("Lost in maze");

        for (WebElement row : rows) {
            List<WebElement> cols = row.findElements(By.tagName("td"));
            if (cols.size() != 3) continue; // skip invalid rows

            String dreamName = cols.get(0).getText().trim();
            String dreamType = cols.get(2).getText().trim();

            // Count Good/Bad dreams
            if (dreamType.equalsIgnoreCase("Good")) {
                goodCount++;
            } else if (dreamType.equalsIgnoreCase("Bad")) {
                badCount++;
            }

            // Count frequency
            dreamFrequency.put(dreamName, dreamFrequency.getOrDefault(dreamName, 0) + 1);
        }

        // Check recurring dreams
        for (String recurring : recurringDreamNames) {
            if (dreamFrequency.getOrDefault(recurring, 0) > 1) {
                recurringCount++;
            }
        }

        int totalDreams = rows.size();

        // Print stats
        System.out.println("Good Dreams: " + goodCount);
        System.out.println("Bad Dreams: " + badCount);
        System.out.println("Total Dreams: " + totalDreams);
        System.out.println("Recurring Dreams: " + recurringCount);

        // Validate stats
        System.out.println(goodCount == 6 ? "PASS: Good Dreams count is correct" : "FAIL: Expected 6 Good Dreams, found " + goodCount);
        System.out.println(badCount == 4 ? "PASS: Bad Dreams count is correct" : "FAIL: Expected 4 Bad Dreams, found " + badCount);
        System.out.println(totalDreams == 10 ? "PASS: Total Dreams count is correct" : "FAIL: Expected 10 Total Dreams, found " + totalDreams);
        System.out.println(recurringCount == 2 ? "PASS: Recurring Dreams count is correct" : "FAIL: Expected 2 Recurring Dreams, found " + recurringCount);

    
    }
}
