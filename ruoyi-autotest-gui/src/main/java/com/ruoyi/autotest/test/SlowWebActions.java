package com.ruoyi.autotest.test;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Selenium ???????????ε??/????????????????????????
 */
public final class SlowWebActions {

    private SlowWebActions() {
    }

    /** 每步操作后的停留毫秒数（默认 1 秒） */
    public static long getStepPauseMs() {
        return Long.parseLong(System.getProperty("ruoyi.test.step.pause.ms", "1000"));
    }

    /** 全部操作完成后、关闭浏览器前的停留毫秒数（默认 3 秒） */
    public static long getFinishPauseMs() {
        return Long.parseLong(System.getProperty("ruoyi.test.finish.pause.ms", "3000"));
    }

    public static void sleepQuietly(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void pauseAfterStep() {
        long pauseMs = getStepPauseMs();
        System.out.println("[???] ??????????? " + (pauseMs / 1000.0) + " ??");
        sleepQuietly(pauseMs);
    }

    public static void pauseBeforeBrowserClose() {
        long pauseMs = getFinishPauseMs();
        if (pauseMs <= 0) {
            return;
        }
        System.out.println("[???] ????????????????????? " + (pauseMs / 1000.0) + " ??");
        sleepQuietly(pauseMs);
    }

    public static void slowClick(WebElement element) {
        element.click();
        pauseAfterStep();
    }

    public static void slowJsClick(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        pauseAfterStep();
    }

    public static void slowClearAndType(WebElement element, String text) {
        element.clear();
        if (text != null && !text.isEmpty()) {
            element.sendKeys(text);
        }
        pauseAfterStep();
    }

    public static void slowType(WebElement element, String text) {
        if (text != null && !text.isEmpty()) {
            element.sendKeys(text);
        }
        pauseAfterStep();
    }

    public static void slowSelectByIndex(Select select, int index) {
        select.selectByIndex(index);
        pauseAfterStep();
    }

    public static void slowSelectByValue(Select select, String value) {
        select.selectByValue(value);
        pauseAfterStep();
    }
}
