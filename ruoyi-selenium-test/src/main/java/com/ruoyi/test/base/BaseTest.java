package com.ruoyi.test.base;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseTest {

    protected static final String BASE_URL = "http://localhost:80";
    protected static final String ADMIN_USER = "admin";
    protected static final String ADMIN_PASS = "admin123";
    protected static final Duration TIMEOUT = Duration.ofSeconds(10);

    protected WebDriver driver;
    protected WebDriverWait wait;

    static {
        String chromeDriverPath = System.getProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
    }

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        wait = new WebDriverWait(driver, TIMEOUT);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            driver.quit();
        }
    }

    protected void takeScreenshot(String testName) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path targetDir = Paths.get("screenshots");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            Path targetPath = targetDir.resolve(testName + "_" + timestamp + ".png");
            Files.copy(srcFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("截图已保存: " + targetPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("截图失败: " + e.getMessage());
        }
    }
}
