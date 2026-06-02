package com.ruoyi.test.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class NoticePage {

    private WebDriver driver;
    private WebDriverWait wait;

    public NoticePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void open() {
        driver.get("http://localhost:80/system/notice");
        waitForPageLoad();
    }

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#bootstrap-table")));
    }

    public boolean isNoticeListDisplayed() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#bootstrap-table")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void clickAddButton() {
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#toolbar .btn-success")));
        addBtn.click();
        sleep(2000);
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

    public void enterNoticeTitle(String title) {
        WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("noticeTitle")));
        titleInput.clear();
        titleInput.sendKeys(title);
    }

    public void enterNoticeContent(String content) {
        WebElement editor = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".note-editable")));
        editor.click();
        editor.clear();
        editor.sendKeys(content);
    }

    public void selectNoticeType(String typeValue) {
        try {
            WebElement typeSelect = driver.findElement(By.name("noticeType"));
            typeSelect.findElement(By.cssSelector("option[value='" + typeValue + "']")).click();
        } catch (Exception e) {
            System.out.println("公告类型选择失败: " + e.getMessage());
        }
    }

    public void selectNoticeStatus(String statusValue) {
        try {
            WebElement statusRadio = driver.findElement(
                    By.cssSelector("input[name='status'][value='" + statusValue + "']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", statusRadio);
        } catch (Exception e) {
            System.out.println("公告状态选择失败: " + e.getMessage());
        }
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
            boolean success = text.contains("成功") || text.contains("成功");
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

    public void searchByTitle(String keyword) {
        WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='noticeTitle']")));
        searchInput.clear();
        searchInput.sendKeys(keyword);
        WebElement searchBtn = driver.findElement(By.cssSelector(".search-collapse .btn-primary"));
        searchBtn.click();
        sleep(1000);
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

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
