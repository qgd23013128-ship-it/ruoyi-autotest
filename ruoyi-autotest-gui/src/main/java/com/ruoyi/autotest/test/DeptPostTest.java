package com.ruoyi.autotest.test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * RuoYi 后台管理系统 - 部门与岗位模块 Selenium + TestNG 自动化测试。
 */
public class DeptPostTest {

    private static final String DATA_PREFIX = "HHJ_23013100_";
    private static final String BASE_URL = System.getProperty("ruoyi.base.url", "http://localhost");
    private static final String LOGIN_URL = BASE_URL + "/login";
    private static final String USERNAME = System.getProperty("ruoyi.username", "admin");
    private static final String PASSWORD = System.getProperty("ruoyi.password", "admin123");

    private static final String POST_MODULE = "system/post";
    private static final String DEPT_MODULE = "system/dept";
    private static final String STATUS_NORMAL = "0";
    private static final String STATUS_DISABLED = "1";

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(20);
    private static final boolean KEEP_BROWSER_OPEN = Boolean.parseBoolean(
        System.getProperty("ruoyi.keep.browser.open", "false"));
    private static final boolean HEADLESS = Boolean.parseBoolean(
        System.getProperty("ruoyi.chrome.headless", "false"));

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUpClass() {
        System.out.println("[INFO] DeptPostTest init. Base URL: " + BASE_URL);
        WebDriverManager.chromedriver().setup();
    }

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        if (HEADLESS) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1440,900");
        }

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterMethod
    public void tearDown() {
        if (driver == null || KEEP_BROWSER_OPEN) {
            return;
        }
        try {
            driver.quit();
        } catch (Exception e) {
            System.err.println("[WARN] Failed to close browser: " + e.getMessage());
        }
    }

    @Test(description = "部门查询单模块测试：系统管理 -> 部门管理 -> 查询研发部门")
    public void testSingleModule_DeptQuery() {
        doLogin(USERNAME, PASSWORD);
        navigateToDeptManagement();

        type(By.cssSelector("#dept-form input[name='deptName']"), "研发部门");
        clickWhenReady(By.cssSelector("#dept-form a[onclick*='treeTable.search']"));

        wait.until(driver -> tableText(By.id("bootstrap-tree-table")).contains("研发部门"));
        Assert.assertTrue(tableText(By.id("bootstrap-tree-table")).contains("研发部门"),
            "部门查询结果应包含研发部门");
    }

    @Test(description = "岗位新增单模块测试：系统管理 -> 岗位管理 -> 新增岗位并保存")
    public void testSingleModule_PostAdd() {
        doLogin(USERNAME, PASSWORD);
        navigateToPostManagement();

        PostData postData = uniquePostData("SADD", STATUS_NORMAL);
        addPostFromCurrentPage(postData);
        searchPostByName(postData.postName);
        waitForPostStatus(postData.postName, STATUS_NORMAL);
    }

    @Test(description = "岗位状态停用单模块测试：新增测试岗位 -> 修改状态为停用 -> 保存")
    public void testSingleModule_PostDisableStatus() {
        doLogin(USERNAME, PASSWORD);
        navigateToPostManagement();

        PostData postData = uniquePostData("DIS", STATUS_NORMAL);
        addPostFromCurrentPage(postData);
        searchPostByName(postData.postName);
        String postId = requirePostId(postData.postName);

        openEditPostDialog(postId);
        selectRadio(By.cssSelector("input[name='status'][value='" + STATUS_DISABLED + "']"));
        submitLayerFormAndWaitClosed();

        switchToModuleFrame(POST_MODULE);
        searchPostByName(postData.postName);
        waitForPostStatus(postData.postName, STATUS_DISABLED);
    }

    @Test(description = "集成路径1：登录 -> 系统管理 -> 岗位管理 -> 新增岗位保存")
    public void testIntegration_AddPost() {
        doLogin(USERNAME, PASSWORD);
        navigateToPostManagement();

        PostData postData = uniquePostData("INTP", STATUS_NORMAL);
        addPostFromCurrentPage(postData);
        searchPostByName(postData.postName);
        waitForPostStatus(postData.postName, STATUS_NORMAL);
    }

    @Test(description = "集成路径2：登录 -> 系统管理 -> 部门管理 -> 展开部门树 -> 点击修改 -> 校验表单回显 -> 取消返回")
    public void testIntegration_EditDeptLeaderForm() {
        doLogin(USERNAME, PASSWORD);
        navigateToDeptManagement();

        executeScript("jQuery.bttTable.bootstrapTreeTable('expandAll');");
        wait.until(driver -> !driver.findElements(By.cssSelector(
            "#bootstrap-tree-table input[name='select_item'][value='103']")).isEmpty());

        WebElement deptRadio = driver.findElement(By.cssSelector(
            "#bootstrap-tree-table input[name='select_item'][value='103']"));
        clickWithJs(deptRadio);
        clickWhenReady(By.cssSelector("#toolbar a[onclick*='edit']"));

        switchToLayerFrame();
        Assert.assertEquals(inputValue(By.cssSelector("input[name='deptId']")), "103",
            "修改表单应回显研发部门ID");
        Assert.assertEquals(inputValue(By.id("deptName")), "研发部门",
            "修改表单应回显部门名称");
        Assert.assertFalse(inputValue(By.cssSelector("input[name='orderNum']")).isEmpty(),
            "修改表单应回显显示顺序");
        Assert.assertNotNull(wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#form-dept-edit input[name='leader']"))), "修改表单应包含负责人字段");

        cancelLayerAndReturnTo(DEPT_MODULE);
        Assert.assertTrue(driver.findElement(By.id("bootstrap-tree-table")).isDisplayed(),
            "取消后应返回部门管理列表");
    }

    @DataProvider(name = "postAddData")
    public Object[][] postAddData() {
        return new Object[][] {
            {"01", "A01", "1", STATUS_NORMAL, "normal-min-sort"},
            {"02", "A02", "2", STATUS_NORMAL, "normal-even-sort"},
            {"03", "A03", "3", STATUS_NORMAL, "normal-odd-sort"},
            {"04", "A04", "4", STATUS_NORMAL, "normal-short-remark"},
            {"05", "A05", "5", STATUS_NORMAL, "normal-middle-sort"},
            {"06", "A06", "6", STATUS_DISABLED, "disabled-min-sort"},
            {"07", "A07", "7", STATUS_DISABLED, "disabled-even-sort"},
            {"08", "A08", "8", STATUS_DISABLED, "disabled-odd-sort"},
            {"09", "A09", "9", STATUS_DISABLED, "disabled-short-remark"},
            {"10", "A10", "10", STATUS_DISABLED, "disabled-two-digit-sort"},
            {"11", "B01", "11", STATUS_NORMAL, "name-code-combo-01"},
            {"12", "B02", "12", STATUS_NORMAL, "name-code-combo-02"},
            {"13", "B03", "13", STATUS_NORMAL, "name-code-combo-03"},
            {"14", "B04", "14", STATUS_NORMAL, "name-code-combo-04"},
            {"15", "B05", "15", STATUS_NORMAL, "name-code-combo-05"},
            {"16", "C01", "16", STATUS_DISABLED, "name-code-combo-06"},
            {"17", "C02", "17", STATUS_DISABLED, "name-code-combo-07"},
            {"18", "C03", "18", STATUS_DISABLED, "name-code-combo-08"},
            {"19", "C04", "19", STATUS_DISABLED, "name-code-combo-09"},
            {"20", "C05", "20", STATUS_DISABLED, "name-code-combo-10"},
            {"21", "D01", "21", STATUS_NORMAL, "boundary-sort-21"},
            {"22", "D02", "22", STATUS_NORMAL, "boundary-sort-22"},
            {"23", "D03", "23", STATUS_DISABLED, "boundary-sort-23"},
            {"24", "D04", "24", STATUS_DISABLED, "boundary-sort-24"},
            {"25", "D05", "25", STATUS_NORMAL, "boundary-sort-25"}
        };
    }

    @Test(description = "新增岗位25组数据组合测试：使用@DataProvider覆盖岗位名称、编码、排序、状态组合",
          dataProvider = "postAddData")
    public void testDataDriven_AddPost25Cases(String caseNo, String codeSuffix, String sort,
                                               String status, String remarkSuffix) {
        doLogin(USERNAME, PASSWORD);
        navigateToPostManagement();

        String unique = compactUnique(caseNo);
        PostData postData = new PostData(
            DATA_PREFIX + "DP" + caseNo + "_" + unique,
            DATA_PREFIX + "CODE" + codeSuffix + "_" + unique,
            sort,
            status,
            DATA_PREFIX + remarkSuffix
        );

        addPostFromCurrentPage(postData);
        searchPostByName(postData.postName);
        waitForPostStatus(postData.postName, status);
    }

    private void doLogin(String username, String password) {
        driver.get(LOGIN_URL);

        type(By.cssSelector("input[name='username']"), username);
        type(By.cssSelector("input[name='password']"), password);

        try {
            WebElement captchaInput = driver.findElement(By.cssSelector("input[name='validateCode']"));
            if (captchaInput.isDisplayed()) {
                captchaInput.clear();
                captchaInput.sendKeys(System.getProperty("ruoyi.validate.code", "1234"));
            }
        } catch (NoSuchElementException ignored) {
            // Captcha is disabled in the usual local test environment.
        }

        clickWhenReady(By.id("btnSubmit"));
        assertLoginSuccess();
    }

    private void assertLoginSuccess() {
        try {
            wait.until(driver -> driver.getCurrentUrl().contains("/index")
                || !driver.findElements(By.cssSelector(".navbar-static-side, .mainContent")).isEmpty());
        } catch (TimeoutException e) {
            Assert.fail("登录失败，当前URL: " + driver.getCurrentUrl());
        }
        Assert.assertFalse(driver.getCurrentUrl().contains("/login"), "登录后不应停留在登录页");
    }

    private void navigateToPostManagement() {
        navigateToMenu(POST_MODULE);
        waitForPostTableReady();
    }

    private void navigateToDeptManagement() {
        navigateToMenu(DEPT_MODULE);
        waitForDeptTreeReady();
    }

    private void navigateToMenu(String modulePath) {
        driver.switchTo().defaultContent();
        String normalizedPath = trimLeadingSlash(modulePath);
        By subMenuLocator = By.xpath("//a[contains(@href,'" + normalizedPath + "')]");

        if (visibleElement(subMenuLocator) == null) {
            By parentMenuLocator = By.xpath("//a[contains(@href,'" + normalizedPath
                + "')]/ancestor::ul[contains(@class,'nav-second-level')][1]/preceding-sibling::a[1]");
            WebElement parentMenu = wait.until(ExpectedConditions.elementToBeClickable(parentMenuLocator));
            clickElement(parentMenu);
        }

        WebElement subMenu = wait.until(driver -> visibleElement(subMenuLocator));
        clickElement(subMenu);
        switchToModuleFrame(normalizedPath);
    }

    private void switchToModuleFrame(String modulePath) {
        driver.switchTo().defaultContent();
        String normalizedPath = trimLeadingSlash(modulePath);
        By frameLocator = By.cssSelector("iframe.RuoYi_iframe[data-id$='" + normalizedPath + "']");
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
        waitForPageReady();
    }

    private void switchToLayerFrame() {
        driver.switchTo().defaultContent();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.layui-layer-iframe")));
        WebElement frame = wait.until(driver -> {
            java.util.List<WebElement> frames = driver.findElements(
                By.cssSelector("div.layui-layer-iframe iframe"));
            return frames.isEmpty() ? null : frames.get(frames.size() - 1);
        });
        driver.switchTo().frame(frame);
        waitForPageReady();
    }

    private void waitForPageReady() {
        wait.until(driver -> "complete".equals(executeScript("return document.readyState")));
    }

    private void waitForPostTableReady() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bootstrap-table")));
        wait.until(driver -> jsBoolean(
            "return !!window.jQuery && !!jQuery.fn.bootstrapTable "
                + "&& !!jQuery('#bootstrap-table').data('bootstrap.table');"));
        wait.until(driver -> jsBoolean(
            "return !window.jQuery || jQuery('.fixed-table-loading:visible').length === 0;"));
    }

    private void waitForDeptTreeReady() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bootstrap-tree-table")));
        wait.until(driver -> jsBoolean(
            "return !!window.jQuery && !!jQuery.fn.bootstrapTreeTable "
                + "&& !!jQuery('#bootstrap-tree-table').data('bootstrap.tree.table');"));
        wait.until(driver -> jsBoolean(
            "return jQuery('#bootstrap-tree-table tbody tr').length > 0;"));
    }

    private void addPostFromCurrentPage(PostData postData) {
        clickWhenReady(By.cssSelector("#toolbar a[onclick*='add']"));
        switchToLayerFrame();

        type(By.id("postName"), postData.postName);
        type(By.id("postCode"), postData.postCode);
        type(By.id("postSort"), postData.postSort);
        selectRadio(By.cssSelector("input[name='status'][value='" + postData.status + "']"));
        type(By.id("remark"), postData.remark);

        submitLayerFormAndWaitClosed();
        switchToModuleFrame(POST_MODULE);
        waitForPostTableReady();
    }

    private void openEditPostDialog(String postId) {
        executeScript("jQuery.operate.edit(arguments[0]);", postId);
        switchToLayerFrame();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("postName")));
    }

    private void submitLayerFormAndWaitClosed() {
        executeScript("submitHandler();");
        waitForLayerClosed();
    }

    private void waitForLayerClosed() {
        driver.switchTo().defaultContent();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.layui-layer-iframe")));
    }

    private void cancelLayerAndReturnTo(String modulePath) {
        driver.switchTo().defaultContent();
        WebElement cancelButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".layui-layer-btn .layui-layer-btn1")));
        clickElement(cancelButton);
        waitForLayerClosed();
        switchToModuleFrame(modulePath);
    }

    private void searchPostByName(String postName) {
        switchToModuleFrame(POST_MODULE);
        type(By.cssSelector("#post-form input[name='postName']"), postName);
        clickWhenReady(By.cssSelector("#post-form a[onclick*='table.search']"));
        waitForPostTableReady();
        wait.until(driver -> postTableHasName(postName));
    }

    private boolean postTableHasName(String postName) {
        return jsBoolean(
            "var rows = jQuery('#bootstrap-table').bootstrapTable('getData') || [];"
                + "return rows.some(function(row) {"
                + "  return String(row.postName || '').indexOf(arguments[0]) >= 0;"
                + "});",
            postName
        );
    }

    private void waitForPostStatus(String postName, String status) {
        wait.until(driver -> jsBoolean(
            "var rows = jQuery('#bootstrap-table').bootstrapTable('getData') || [];"
                + "return rows.some(function(row) {"
                + "  return String(row.postName || '') === String(arguments[0])"
                + "    && String(row.status) === String(arguments[1]);"
                + "});",
            postName,
            status
        ));
    }

    private String requirePostId(String postName) {
        Object postId = executeScript(
            "var rows = jQuery('#bootstrap-table').bootstrapTable('getData') || [];"
                + "for (var i = 0; i < rows.length; i++) {"
                + "  if (String(rows[i].postName || '') === String(arguments[0])) {"
                + "    return String(rows[i].postId);"
                + "  }"
                + "}"
                + "return null;",
            postName
        );
        Assert.assertNotNull(postId, "未找到新增岗位ID: " + postName);
        return String.valueOf(postId);
    }

    private void type(By locator, String value) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        input.clear();
        input.sendKeys(value);
    }

    private void clickWhenReady(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        clickElement(element);
    }

    private void clickElement(WebElement element) {
        try {
            element.click();
        } catch (WebDriverException e) {
            clickWithJs(element);
        }
    }

    private void clickWithJs(WebElement element) {
        executeScript("arguments[0].scrollIntoView({block:'center', inline:'nearest'});"
            + "arguments[0].click();", element);
    }

    private void selectRadio(By locator) {
        WebElement radio = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        if (!radio.isSelected()) {
            clickWithJs(radio);
        }
        wait.until(driver -> radio.isSelected());
    }

    private String inputValue(By locator) {
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        String value = input.getAttribute("value");
        return value == null ? "" : value.trim();
    }

    private String tableText(By locator) {
        return driver.findElement(locator).getText();
    }

    private WebElement visibleElement(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            if (element.isDisplayed()) {
                return element;
            }
        }
        return null;
    }

    private Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    private boolean jsBoolean(String script, Object... args) {
        return Boolean.TRUE.equals(executeScript(script, args));
    }

    private String trimLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private PostData uniquePostData(String label, String status) {
        String unique = compactUnique(label);
        return new PostData(
            DATA_PREFIX + label + "_" + unique,
            DATA_PREFIX + "CODE_" + label + "_" + unique,
            "10",
            status,
            DATA_PREFIX + "created_by_selenium"
        );
    }

    private String compactUnique(String label) {
        String time = DateTimeFormatter.ofPattern("MMddHHmmss").format(LocalDateTime.now());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return label + "_" + time + "_" + uuid;
    }

    private static final class PostData {
        private final String postName;
        private final String postCode;
        private final String postSort;
        private final String status;
        private final String remark;

        private PostData(String postName, String postCode, String postSort, String status, String remark) {
            this.postName = postName;
            this.postCode = postCode;
            this.postSort = postSort;
            this.status = status;
            this.remark = remark;
        }
    }
}
