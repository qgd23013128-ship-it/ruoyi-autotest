package com.ruoyi.test.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class ConfigPage {

    private WebDriver driver;
    private WebDriverWait wait;

    public ConfigPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void open() {
        driver.get("http://localhost:80/system/config");
        waitForPageLoad();
    }

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#bootstrap-table")));
    }

    public boolean isConfigListDisplayed() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#bootstrap-table")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void searchByParamName(String keyword) {
        WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='configName']")));
        searchInput.clear();
        searchInput.sendKeys(keyword);
        WebElement searchBtn = driver.findElement(By.cssSelector(".search-collapse .btn-primary"));
        searchBtn.click();
        sleep(1000);
    }

    public void searchByParamKey(String keyword) {
        WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='configKey']")));
        searchInput.clear();
        searchInput.sendKeys(keyword);
        WebElement searchBtn = driver.findElement(By.cssSelector(".search-collapse .btn-primary"));
        searchBtn.click();
        sleep(1000);
    }

    public void selectFirstRow() {
        sleep(1500);
        try {
            WebElement firstCheckbox = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("#bootstrap-table tbody tr td:first-child input[type='checkbox']")));
            firstCheckbox.click();
            sleep(500);
        } catch (Exception e) {
            System.out.println("选择首行失败: " + e.getMessage());
        }
    }

    public void clickEditButton() {
        WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#toolbar .btn-primary.single")));
        editBtn.click();
        sleep(1500);
        switchToLayerIframe();
    }

    private void switchToLayerIframe() {
        try {
            WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".layui-layer-content iframe")));
            driver.switchTo().frame(iframe);
        } catch (Exception e) {
            System.out.println("未找到layer iframe");
        }
    }

    public void modifyConfigValue(String newValue) {
        WebElement valueTextarea = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("configValue")));
        valueTextarea.clear();
        valueTextarea.sendKeys(newValue);
    }

    public boolean clickSubmitAndWaitResult() {
        driver.switchTo().defaultContent();
        sleep(300);
        try {
            WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".layui-layer-btn0")));
            confirmBtn.click();
        } catch (Exception e) {
            System.out.println("点击确定按钮失败: " + e.getMessage());
            try {
                ((JavascriptExecutor) driver).executeScript("$('.layui-layer-btn0').click()");
            } catch (Exception e2) {
                System.out.println("JS点击也失败");
                return false;
            }
        }
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement toast = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".layui-layer-dialog")));
            String text = toast.getText();
            System.out.println("Toast消息: " + text);
            boolean success = text.contains("成功");
            sleep(2000);
            return success;
        } catch (TimeoutException e) {
            System.out.println("未检测到Toast成功消息");
            try {
                boolean hasLayer = driver.findElement(By.cssSelector(".layui-layer-page")).isDisplayed();
                System.out.println("仍有layer页面打开");
            } catch (Exception e2) {
                System.out.println("Layer已关闭");
            }
            return false;
        }
    }

    public boolean hasDataInTable() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#bootstrap-table tbody tr")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getCurrentConfigValue() {
        WebElement valueTextarea = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("configValue")));
        return valueTextarea.getAttribute("value");
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
