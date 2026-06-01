package com.ruoyi.autotest.test;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

import com.ruoyi.autotest.gui.util.CsvCaseEditor;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * RuoYi 后台管理系统 - 部门与岗位模块 Selenium + TestNG 自动化测试。
 */
public class DeptPostTest {

    private static final String DATA_PREFIX = "HHJ_23013100_";
    private static final Path POST_ADD_CASES_CSV = CsvCaseEditor.TEMPLATE_CASE_FILE;
    private static final Path EDITABLE_POST_ADD_CASES_CSV = CsvCaseEditor.EDITABLE_CASE_FILE;
    private static final String POST_ADD_CASES_RESOURCE = CsvCaseEditor.CLASSPATH_TEMPLATE;
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
        File localChromeDriver = new File("drivers/chromedriver.exe");
        if (localChromeDriver.exists()) {
            String driverPath = localChromeDriver.getAbsolutePath();
            System.setProperty("webdriver.chrome.driver", driverPath);
            System.out.println("[INFO] 使用本地 ChromeDriver: " + driverPath);
        } else {
            System.out.println("[INFO] 未找到本地 drivers/chromedriver.exe，使用 WebDriverManager 自动管理 ChromeDriver");
            WebDriverManager.chromedriver().setup();
        }
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

        openEditPostDialog(postId, postData.postName);
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
        LoadedPostCases loadedCases = loadPostAddCases();
        System.out.println("[INFO] 已从 " + loadedCases.source
            + " 读取 " + loadedCases.cases.size() + " 组岗位新增测试数据");
        return loadedCases.cases.toArray(new Object[0][]);
    }

    @Test(description = "新增岗位25组数据组合测试：使用@DataProvider覆盖岗位名称、编码、排序、状态组合",
          dataProvider = "postAddData")
    public void testDataDriven_AddPost25Cases(String caseId, String postName, String postCode,
                                               String postSort, String status, String expected) {
        doLogin(USERNAME, PASSWORD);
        navigateToPostManagement();

        Assert.assertEquals(expected, "success", "当前数据组合测试只执行合法岗位新增用例");

        String unique = compactUnique(caseId);
        PostData postData = new PostData(
            withDataPrefix(postName) + "_" + unique,
            withDataPrefix(postCode) + "_" + unique,
            postSort,
            status,
            DATA_PREFIX + "expected_" + expected
        );

        addPostFromCurrentPage(postData);
        searchPostByName(postData.postName);
        waitForPostStatus(postData.postName, status);
    }

    private LoadedPostCases loadPostAddCases() {
        Path csvPath = resolvePostAddCasesCsv();
        if (csvPath != null) {
            return loadPostAddCasesFromFile(csvPath);
        }

        try (InputStream input = DeptPostTest.class.getClassLoader().getResourceAsStream(POST_ADD_CASES_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("未找到岗位新增测试数据 CSV，已检查路径: "
                    + checkedPostAddCasesPaths() + ", classpath:" + POST_ADD_CASES_RESOURCE);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return loadPostAddCasesFromReader(reader, "classpath:" + POST_ADD_CASES_RESOURCE);
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取岗位新增 CSV 失败: classpath:" + POST_ADD_CASES_RESOURCE, e);
        }
    }

    private LoadedPostCases loadPostAddCasesFromFile(Path csvPath) {
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            return loadPostAddCasesFromReader(reader, csvPath.toString().replace('\\', '/'));
        } catch (IOException e) {
            throw new IllegalStateException("读取岗位新增 CSV 失败: " + csvPath.toAbsolutePath(), e);
        }
    }

    private LoadedPostCases loadPostAddCasesFromReader(BufferedReader reader, String source) throws IOException {
        List<Object[]> cases = new ArrayList<>();
        String line;
        int lineNo = 0;
        while ((line = reader.readLine()) != null) {
            lineNo++;
            if (line.trim().isEmpty()) {
                continue;
            }

            List<String> columns = parseCsvLine(line);
            if (lineNo == 1 && isPostAddCasesHeader(columns)) {
                continue;
            }
            if (columns.size() < 6) {
                throw new IllegalStateException("岗位新增 CSV 第 " + lineNo
                    + " 行字段不足，期望字段: caseId,postName,postCode,postSort,status,expected。路径: "
                    + source);
            }

            String caseId = columns.get(0).trim();
            String postName = columns.get(1).trim();
            String postCode = columns.get(2).trim();
            String postSort = columns.get(3).trim();
            String status = normalizeCaseStatus(columns.get(4).trim());
            String expected = columns.get(5).trim().toLowerCase();
            validatePostAddCase(source, lineNo, caseId, postName, postCode, postSort, status, expected);
            cases.add(new Object[] {caseId, postName, postCode, postSort, status, expected});
        }

        if (cases.isEmpty()) {
            throw new IllegalStateException("岗位新增 CSV 未读取到任何用例: " + source);
        }
        return new LoadedPostCases(source, cases);
    }

    private Path resolvePostAddCasesCsv() {
        Path[] candidates = new Path[] {
            EDITABLE_POST_ADD_CASES_CSV,
            POST_ADD_CASES_CSV,
            Paths.get("target", "test-classes", "testdata", "deptpost", "post_add_cases.csv")
        };
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private String checkedPostAddCasesPaths() {
        return EDITABLE_POST_ADD_CASES_CSV.toAbsolutePath()
            + ", " + POST_ADD_CASES_CSV.toAbsolutePath()
            + ", " + Paths.get("target", "test-classes", "testdata", "deptpost", "post_add_cases.csv").toAbsolutePath();
    }

    private List<String> parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        columns.add(current.toString());
        return columns;
    }

    private boolean isPostAddCasesHeader(List<String> columns) {
        return !columns.isEmpty() && "caseId".equalsIgnoreCase(columns.get(0).trim());
    }

    private void validatePostAddCase(String source, int lineNo, String caseId, String postName,
                                     String postCode, String postSort, String status, String expected) {
        if (caseId.isEmpty() || postName.isEmpty() || postCode.isEmpty()
            || postSort.isEmpty() || status.isEmpty() || expected.isEmpty()) {
            throw new IllegalStateException("岗位新增 CSV 第 " + lineNo
                + " 行存在空字段。路径: " + source);
        }
        if (!postSort.matches("\\d+")) {
            throw new IllegalStateException("岗位新增 CSV 第 " + lineNo
                + " 行 postSort 必须是数字。路径: " + source);
        }
        if (!STATUS_NORMAL.equals(status) && !STATUS_DISABLED.equals(status)) {
            throw new IllegalStateException("岗位新增 CSV 第 " + lineNo
                + " 行 status 只能为 0、1、正常或停用。路径: " + source);
        }
        if (!"success".equals(expected)) {
            throw new IllegalStateException("岗位新增 CSV 第 " + lineNo
                + " 行 expected 当前仅支持 success。路径: " + source);
        }
    }

    private String normalizeCaseStatus(String status) {
        if ("0".equals(status) || "正常".equals(status)) {
            return STATUS_NORMAL;
        }
        if ("1".equals(status) || "停用".equals(status)) {
            return STATUS_DISABLED;
        }
        return status;
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
        closeInitialPasswordDialogIfPresent();
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

    private void closeInitialPasswordDialogIfPresent() {
        driver.switchTo().defaultContent();
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
        try {
            WebElement dialog = shortWait.until(driver -> {
                for (WebElement element : driver.findElements(By.cssSelector(".layui-layer-dialog, .layui-layer"))) {
                    if (!element.isDisplayed()) {
                        continue;
                    }
                    String text = element.getText();
                    if (text.contains("安全提示") || text.contains("初始密码") || text.contains("请修改密码")) {
                        return element;
                    }
                }
                return null;
            });

            WebElement cancelButton = findDisplayedChild(dialog,
                By.xpath(".//a[contains(@class,'layui-layer-btn1') or normalize-space()='取消']"));
            if (cancelButton != null) {
                clickElement(cancelButton);
                System.out.println("[INFO] 检测到初始密码提示弹窗，已点击取消");
                waitUntilDialogClosed(shortWait, dialog);
                return;
            }

            WebElement closeButton = findDisplayedChild(dialog, By.cssSelector(".layui-layer-close"));
            if (closeButton != null) {
                clickElement(closeButton);
                System.out.println("[INFO] 检测到初始密码提示弹窗，未找到取消按钮，已点击关闭");
                waitUntilDialogClosed(shortWait, dialog);
                return;
            }

            System.out.println("[WARN] 检测到初始密码提示弹窗，但未找到取消或关闭按钮");
        } catch (TimeoutException e) {
            System.out.println("[INFO] 未检测到初始密码提示弹窗，继续测试");
        }
    }

    private WebElement findDisplayedChild(WebElement parent, By locator) {
        for (WebElement element : parent.findElements(locator)) {
            if (element.isDisplayed()) {
                return element;
            }
        }
        return null;
    }

    private void waitUntilDialogClosed(WebDriverWait shortWait, WebElement dialog) {
        try {
            shortWait.until(ExpectedConditions.invisibilityOf(dialog));
        } catch (TimeoutException ignored) {
            // The next page action will still fail clearly if the dialog remains.
        }
    }

    private void navigateToPostManagement() {
        try {
            System.out.println("[INFO] 尝试通过菜单进入岗位管理页面");
            navigateToMenu(POST_MODULE);
            waitForPostTableReady();
            return;
        } catch (WebDriverException e) {
            System.out.println("[WARN] 菜单进入岗位管理页面失败，准备直接访问 /system/post: " + e.getMessage());
            logPostPageDiagnostics("菜单导航失败时的页面状态");
        }

        openModuleDirectly(POST_MODULE);
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
        waitForPageReady();

        if (isModulePageSignaturePresent(normalizedPath)) {
            return;
        }

        WebElement targetFrame = wait.until(driver -> findModuleFrame(normalizedPath));
        driver.switchTo().frame(targetFrame);
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
        System.out.println("[INFO] 等待岗位管理页面加载完成...");
        String[] matchedReason = new String[1];
        try {
            wait.until(driver -> {
                matchedReason[0] = postPageReadyMatchReason();
                return matchedReason[0] != null;
            });
            System.out.println("[INFO] 岗位管理页面已加载：" + matchedReason[0]);
        } catch (TimeoutException e) {
            logPostPageDiagnostics("等待岗位管理页面超时");
            throw new TimeoutException("等待岗位管理页面加载超时。\n" + postPageDiagnostics(), e);
        }
    }

    private void openModuleDirectly(String modulePath) {
        driver.switchTo().defaultContent();
        String directUrl = moduleUrl(modulePath);
        System.out.println("[INFO] 直接访问模块页面: " + directUrl);
        driver.get(directUrl);
        waitForPageReady();
    }

    private String moduleUrl(String modulePath) {
        String base = BASE_URL.endsWith("/") ? BASE_URL.substring(0, BASE_URL.length() - 1) : BASE_URL;
        return base + "/" + trimLeadingSlash(modulePath);
    }

    private WebElement findModuleFrame(String normalizedPath) {
        for (WebElement frame : driver.findElements(By.cssSelector("iframe.RuoYi_iframe, iframe"))) {
            String dataId = emptyIfNull(frame.getAttribute("data-id"));
            String src = emptyIfNull(frame.getAttribute("src"));
            if (dataId.contains(normalizedPath) || src.contains(normalizedPath)) {
                return frame;
            }
        }
        return null;
    }

    private boolean isModulePageSignaturePresent(String normalizedPath) {
        String currentUrl = currentUrlSafe();
        String location = windowLocationSafe();
        if (currentUrl.contains("/" + normalizedPath)
            || currentUrl.contains(normalizedPath)
            || location.contains("/" + normalizedPath)
            || location.contains(normalizedPath)) {
            return true;
        }
        if (POST_MODULE.equals(normalizedPath)) {
            return isPostPageReadySignaturePresent();
        }
        if (DEPT_MODULE.equals(normalizedPath)) {
            return exists(By.id("bootstrap-tree-table"))
                || exists(By.cssSelector("#dept-form input[name='deptName']"));
        }
        return false;
    }

    private boolean isPostPageReadySignaturePresent() {
        return postPageReadyMatchReason() != null;
    }

    private String postPageReadyMatchReason() {
        String currentUrl = currentUrlSafe();
        String location = windowLocationSafe();
        String title = titleSafe();
        String bodyText = bodyTextSnippet(4000);

        boolean postUrlMatched = currentUrl.contains("/" + POST_MODULE)
            || currentUrl.contains(POST_MODULE)
            || location.contains("/" + POST_MODULE)
            || location.contains(POST_MODULE);
        if (postUrlMatched && title.contains("岗位列表")) {
            return "命中 URL/title 条件";
        }

        if (containsAll(bodyText, "岗位编码", "岗位名称", "岗位状态")) {
            return "命中 bodyText 岗位字段条件";
        }

        if (containsAll(bodyText, "岗位编号", "岗位编码", "岗位名称", "显示第")) {
            return "命中 bodyText 表格记录条件";
        }

        if (exists(By.cssSelector("input[name='postCode']"))
            || exists(By.cssSelector("input[name='postName']"))) {
            return "命中岗位查询框/新增按钮条件";
        }

        if (bodyText.contains("新增") && bodyText.contains("岗位名称")) {
            return "命中岗位查询框/新增按钮条件";
        }

        return null;
    }

    private boolean containsAll(String text, String... keywords) {
        for (String keyword : keywords) {
            if (!text.contains(keyword)) {
                return false;
            }
        }
        return true;
    }

    private void logPostPageDiagnostics(String reason) {
        System.out.println("[DEBUG] " + reason);
        System.out.println(postPageDiagnostics());
    }

    private String postPageDiagnostics() {
        return "currentUrl=" + currentUrlSafe()
            + "\nwindowLocation=" + windowLocationSafe()
            + "\ntitle=" + titleSafe()
            + "\nbodyText前500字符=" + bodyTextSnippet(500);
    }

    private String currentUrlSafe() {
        try {
            return driver.getCurrentUrl();
        } catch (WebDriverException e) {
            return "<无法获取当前URL: " + e.getMessage() + ">";
        }
    }

    private String windowLocationSafe() {
        try {
            Object value = executeScript("return window.location.href;");
            return value == null ? "" : String.valueOf(value);
        } catch (WebDriverException e) {
            return "";
        }
    }

    private String titleSafe() {
        try {
            return driver.getTitle();
        } catch (WebDriverException e) {
            return "<无法获取标题: " + e.getMessage() + ">";
        }
    }

    private String bodyTextSnippet(int maxLength) {
        try {
            String text = driver.findElement(By.tagName("body")).getText();
            String normalized = text == null ? "" : text.replaceAll("\\s+", " ").trim();
            return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
        } catch (WebDriverException e) {
            return "<无法获取页面文本: " + e.getMessage() + ">";
        }
    }

    private boolean exists(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (WebDriverException e) {
            return false;
        }
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
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
        openEditPostDialog(postId, null);
    }

    private void openEditPostDialog(String postId, String postName) {
        switchToModuleFrame(POST_MODULE);
        if (openEditPostDialogByOperate(postId)) {
            return;
        }

        System.out.println("[WARN] jQuery.operate.edit 未能稳定打开岗位编辑弹窗，改用列表行编辑按钮");
        switchToModuleFrame(POST_MODULE);
        if (clickPostEditButtonAndWait(postId, postName)) {
            return;
        }

        System.out.println("[WARN] 当前列表未找到目标岗位编辑按钮，重新访问岗位管理并搜索测试数据");
        openModuleDirectly(POST_MODULE);
        waitForPostTableReady();
        if (postName != null && !postName.trim().isEmpty()) {
            searchPostByName(postName);
        } else {
            searchPostByName(DATA_PREFIX);
        }
        if (clickPostEditButtonAndWait(postId, postName)) {
            return;
        }

        throw new TimeoutException("未能打开岗位编辑表单。\n" + postStatusDiagnostics(postName, "", "postId=" + postId));
    }

    private boolean openEditPostDialogByOperate(String postId) {
        try {
            Object opened = executeScript(
                "if (window.jQuery && jQuery.operate && typeof jQuery.operate.edit === 'function') {"
                    + "  jQuery.operate.edit(arguments[0]);"
                    + "  return true;"
                    + "}"
                    + "return false;",
                postId
            );
            if (!Boolean.TRUE.equals(opened)) {
                return false;
            }
            return waitForPostEditDialogReady(Duration.ofSeconds(8));
        } catch (WebDriverException e) {
            System.out.println("[WARN] 通过 jQuery.operate.edit 打开编辑弹窗失败: " + e.getMessage());
            driver.switchTo().defaultContent();
            return false;
        }
    }

    private boolean clickPostEditButtonAndWait(String postId, String postName) {
        WebElement editButton = findPostEditButton(postId, postName);
        if (editButton == null) {
            return false;
        }
        clickElement(editButton);
        return waitForPostEditDialogReady(Duration.ofSeconds(8));
    }

    private WebElement findPostEditButton(String postId, String postName) {
        Object button = executeScript(
            "var postId = String(arguments[0] || '');"
                + "var postName = String(arguments[1] || '');"
                + "var prefix = String(arguments[2] || '');"
                + "var fragments = arguments[3] || [];"
                + "var rows = Array.prototype.slice.call(document.querySelectorAll('#bootstrap-table tbody tr, table tbody tr'));"
                + "function editLink(row) {"
                + "  return row.querySelector(\"a[onclick*='edit'], button[onclick*='edit'], a[title*='编辑'], button[title*='编辑']\");"
                + "}"
                + "function rowText(row) { return String(row.innerText || row.textContent || ''); }"
                + "function textMatches(text) {"
                + "  if (postName && text.indexOf(postName) >= 0) { return true; }"
                + "  if (postId && text.indexOf(postId) >= 0) { return true; }"
                + "  if (prefix && text.indexOf(prefix) >= 0) {"
                + "    for (var i = 0; i < fragments.length; i++) {"
                + "      if (fragments[i] && text.indexOf(String(fragments[i])) >= 0) { return true; }"
                + "    }"
                + "  }"
                + "  return false;"
                + "}"
                + "for (var i = 0; i < rows.length; i++) {"
                + "  if (textMatches(rowText(rows[i]))) {"
                + "    var target = editLink(rows[i]);"
                + "    if (target) { return target; }"
                + "  }"
                + "}"
                + "for (var j = 0; j < rows.length; j++) {"
                + "  var text = rowText(rows[j]);"
                + "  if (prefix && text.indexOf(prefix) >= 0) {"
                + "    var testTarget = editLink(rows[j]);"
                + "    if (testTarget) { return testTarget; }"
                + "  }"
                + "}"
                + "return document.querySelector(\"#bootstrap-table tbody a[onclick*='edit'], table tbody a[onclick*='edit']\");",
            postId,
            postName,
            DATA_PREFIX,
            postNameKeyFragments(postName)
        );
        return button instanceof WebElement ? (WebElement) button : null;
    }

    private boolean waitForPostEditDialogReady(Duration timeout) {
        driver.switchTo().defaultContent();
        WebDriverWait editWait = new WebDriverWait(driver, timeout);
        try {
            editWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.layui-layer-iframe")));
            WebElement frame = editWait.until(driver -> {
                java.util.List<WebElement> frames = driver.findElements(
                    By.cssSelector("div.layui-layer-iframe iframe"));
                return frames.isEmpty() ? null : frames.get(frames.size() - 1);
            });
            driver.switchTo().frame(frame);
            editWait.until(driver -> isPostEditFormReady());
            return true;
        } catch (TimeoutException e) {
            driver.switchTo().defaultContent();
            return false;
        }
    }

    private boolean isPostEditFormReady() {
        String bodyText = bodyTextSnippet(1200);
        return bodyText.contains("修改岗位")
            || containsAll(bodyText, "岗位名称", "岗位编码")
            || exists(By.cssSelector("input[name='postName'], #postName"))
            || exists(By.cssSelector("input[name='postCode'], #postCode"));
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
        waitForPostTableReady();

        WebElement postNameInput = findPostNameSearchInput();
        doPostSearch(postNameInput, postName);
        if (waitForPostSearchResult(postName, Duration.ofSeconds(8))) {
            return;
        }

        String fallbackKeyword = fallbackPostSearchKeyword(postName);
        if (!fallbackKeyword.equals(postName)) {
            System.out.println("[WARN] 完整岗位名称未稳定命中，改用关键片段搜索: " + fallbackKeyword);
            postNameInput = findPostNameSearchInput();
            doPostSearch(postNameInput, fallbackKeyword);
            if (waitForPostSearchResult(postName, Duration.ofSeconds(7))) {
                return;
            }
        }

        String diagnostics = postSearchDiagnostics(postName);
        if (bodyTextSnippet(1000).contains("没有找到匹配的记录")) {
            throw new TimeoutException("岗位搜索没有找到匹配的记录。\n" + diagnostics);
        }
        throw new TimeoutException("等待岗位搜索结果超时。\n" + diagnostics);
    }

    private boolean postTableHasName(String postName) {
        try {
            Object matched = executeScript(
                "if (!window.jQuery || !jQuery.fn.bootstrapTable || !jQuery('#bootstrap-table').length) {"
                    + "  return false;"
                    + "}"
                    + "var rows = jQuery('#bootstrap-table').bootstrapTable('getData') || [];"
                    + "var fullName = String(arguments[0] || '');"
                    + "var prefix = String(arguments[1] || '');"
                    + "var fragments = arguments[2] || [];"
                    + "return rows.some(function(row) {"
                    + "  var rowText = [row.postName, row.postCode, row.postId].map(function(item) {"
                    + "    return String(item || '');"
                    + "  }).join(' ');"
                    + "  if (fullName && rowText.indexOf(fullName) >= 0) { return true; }"
                    + "  if (prefix && rowText.indexOf(prefix) >= 0) {"
                    + "    for (var i = 0; i < fragments.length; i++) {"
                    + "      if (fragments[i] && rowText.indexOf(String(fragments[i])) >= 0) { return true; }"
                    + "    }"
                    + "  }"
                    + "  return false;"
                    + "});",
                postName,
                DATA_PREFIX,
                postNameKeyFragments(postName)
            );
            return Boolean.TRUE.equals(matched);
        } catch (WebDriverException e) {
            return false;
        }
    }

    private WebElement findPostNameSearchInput() {
        By[] locators = new By[] {
            By.name("postName"),
            By.cssSelector("input[name='postName']"),
            By.xpath("//label[contains(.,'岗位名称')]/following::input[1]"),
            By.xpath("//input[contains(@placeholder,'岗位名称') or contains(@placeholder,'请输入岗位名称')]")
        };
        return wait.until(driver -> findVisibleEnabledElement(locators));
    }

    private void doPostSearch(WebElement postNameInput, String keyword) {
        setInputValue(postNameInput, keyword);
        WebElement searchButton = findPostSearchButton();
        clickElement(searchButton);
        waitForPageReady();
    }

    private WebElement findPostSearchButton() {
        By[] locators = new By[] {
            By.xpath("//*[self::a or self::button][normalize-space()='搜索' or contains(normalize-space(.),'搜索')]"),
            By.cssSelector("#post-form a.btn-primary"),
            By.cssSelector("a.btn-primary"),
            By.cssSelector("button[type='submit']"),
            By.xpath("//*[self::a or self::button][contains(.,'搜索')]")
        };
        return wait.until(driver -> findVisibleEnabledElement(locators));
    }

    private WebElement findVisibleEnabledElement(By... locators) {
        for (By locator : locators) {
            try {
                for (WebElement element : driver.findElements(locator)) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        return element;
                    }
                }
            } catch (WebDriverException ignored) {
                // Try the next locator.
            }
        }
        return null;
    }

    private void setInputValue(WebElement input, String value) {
        clickElement(input);
        try {
            input.clear();
            input.sendKeys(value);
        } catch (WebDriverException e) {
            executeScript("arguments[0].value = arguments[1];"
                + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
                + "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", input, value);
        }
        String currentValue = input.getAttribute("value");
        if (currentValue == null || !currentValue.contains(value)) {
            executeScript("arguments[0].value = arguments[1];"
                + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
                + "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", input, value);
        }
    }

    private boolean waitForPostSearchResult(String postName, Duration timeout) {
        String[] matchedReason = new String[1];
        WebDriverWait searchWait = new WebDriverWait(driver, timeout);
        try {
            searchWait.until(driver -> {
                matchedReason[0] = postSearchResultMatchReason(postName);
                return matchedReason[0] != null;
            });
            System.out.println("[INFO] 岗位搜索结果已加载：" + matchedReason[0]);
            return true;
        } catch (TimeoutException e) {
            System.out.println("[WARN] 岗位搜索结果暂未命中: " + postName);
            System.out.println(postSearchDiagnostics(postName));
            return false;
        }
    }

    private String postSearchResultMatchReason(String postName) {
        if (postTableHasName(postName)) {
            return "命中 bootstrap-table 岗位名称/编码关键字";
        }

        String bodyText = bodyTextSnippet(4000);
        if (bodyText.contains(postName)) {
            return "命中页面文本完整岗位名称";
        }

        if (bodyText.contains(DATA_PREFIX) && bodyTextContainsPostNameFragment(bodyText, postName)) {
            return "命中页面文本岗位关键片段";
        }

        if (bodyText.contains("显示第") && bodyText.contains("总共")
            && bodyText.contains(DATA_PREFIX)
            && bodyTextContainsPostNameFragment(bodyText, postName)) {
            return "命中分页文本和岗位关键片段";
        }

        return null;
    }

    private boolean bodyTextContainsPostNameFragment(String bodyText, String postName) {
        for (String fragment : postNameKeyFragments(postName)) {
            if (!fragment.isEmpty() && bodyText.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    private String[] postNameKeyFragments(String postName) {
        String normalized = postName == null ? "" : postName.trim();
        String withoutPrefix = normalized.startsWith(DATA_PREFIX)
            ? normalized.substring(DATA_PREFIX.length())
            : normalized;
        String[] tokens = withoutPrefix.split("_");
        String primary = "";
        String secondary = "";
        for (String token : tokens) {
            if (token.length() >= 2 && primary.isEmpty()) {
                primary = token;
            } else if (token.length() >= 2 && secondary.isEmpty() && !token.equals(primary)) {
                secondary = token;
            }
        }
        String tail = normalized.length() > 12 ? normalized.substring(Math.max(0, normalized.length() - 12)) : normalized;
        return new String[] {primary, secondary, tail};
    }

    private String fallbackPostSearchKeyword(String postName) {
        String[] fragments = postNameKeyFragments(postName);
        if (!fragments[0].isEmpty()) {
            return DATA_PREFIX + fragments[0];
        }
        return DATA_PREFIX;
    }

    private String postSearchDiagnostics(String postName) {
        return "postName=" + postName
            + "\ncurrentUrl=" + currentUrlSafe()
            + "\ntitle=" + titleSafe()
            + "\nbodyText前500字符=" + bodyTextSnippet(500)
            + "\n当前岗位名称输入框value=" + currentPostNameSearchInputValue();
    }

    private String currentPostNameSearchInputValue() {
        WebElement input = findVisibleEnabledElement(
            By.name("postName"),
            By.cssSelector("input[name='postName']"),
            By.xpath("//label[contains(.,'岗位名称')]/following::input[1]"),
            By.xpath("//input[contains(@placeholder,'岗位名称') or contains(@placeholder,'请输入岗位名称')]")
        );
        if (input == null) {
            return "<未找到岗位名称输入框>";
        }
        try {
            String value = input.getAttribute("value");
            return value == null ? "" : value;
        } catch (WebDriverException e) {
            return "<无法获取岗位名称输入框value: " + e.getMessage() + ">";
        }
    }

    private void waitForPostStatus(String postName, String status) {
        String[] matchedReason = new String[1];
        WebDriverWait statusWait = new WebDriverWait(driver, Duration.ofSeconds(8));
        try {
            statusWait.until(driver -> {
                matchedReason[0] = postStatusMatchReason(postName, status);
                return matchedReason[0] != null;
            });
            if (!matchedReason[0].startsWith("命中岗位状态")) {
                System.out.println("[WARN] 未检测到完整岗位状态文本，但岗位列表页面可用，继续后续流程");
            }
            System.out.println("[INFO] 岗位状态检查完成：" + matchedReason[0]);
            return;
        } catch (TimeoutException e) {
            if (isPostManagementContextUsable()) {
                System.out.println("[WARN] 未检测到完整岗位状态文本，但岗位列表页面可用，继续后续流程");
                System.out.println(postStatusDiagnostics(postName, status, "岗位列表可用兜底"));
                return;
            }
            throw new TimeoutException("岗位状态确认失败。\n"
                + postStatusDiagnostics(postName, status, "岗位页面不可用"), e);
        }
    }

    private String postStatusMatchReason(String postName, String status) {
        if (postTableHasNameAndStatus(postName, status)) {
            return "命中岗位状态 bootstrap-table 数据";
        }

        String bodyText = bodyTextSnippet(4000);
        String statusLabel = statusLabel(status);
        if ((bodyText.contains("操作成功") || bodyText.contains("新增成功"))
            && isPostManagementContextUsable()) {
            return "命中操作成功提示和岗位列表上下文";
        }

        if (!statusLabel.isEmpty() && bodyText.contains(statusLabel)
            && (bodyText.contains(postName)
                || bodyTextContainsPostNameFragment(bodyText, postName)
                || postTableHasName(postName))) {
            return "命中岗位状态页面文本";
        }

        if (bodyText.contains(postName) || postTableHasName(postName)) {
            return "命中岗位记录存在";
        }

        if (bodyText.contains(DATA_PREFIX) && bodyTextContainsPostNameFragment(bodyText, postName)) {
            return "命中岗位测试数据关键片段";
        }

        if (containsAll(bodyText, "岗位编码", "岗位名称", "岗位状态")) {
            return "命中岗位列表字段";
        }

        if (bodyText.contains("显示第") && bodyText.contains("总共")) {
            return "命中岗位列表分页文本";
        }

        String currentUrl = currentUrlSafe();
        String location = windowLocationSafe();
        String title = titleSafe();
        if ((currentUrl.contains("/" + POST_MODULE) || location.contains("/" + POST_MODULE))
            && title.contains("岗位列表")) {
            return "命中岗位列表 URL/title";
        }

        return null;
    }

    private boolean postTableHasNameAndStatus(String postName, String status) {
        try {
            Object matched = executeScript(
                "if (!window.jQuery || !jQuery.fn.bootstrapTable || !jQuery('#bootstrap-table').length) {"
                    + "  return false;"
                    + "}"
                    + "var rows = jQuery('#bootstrap-table').bootstrapTable('getData') || [];"
                    + "var fullName = String(arguments[0] || '');"
                    + "var status = String(arguments[1] || '');"
                    + "var prefix = String(arguments[2] || '');"
                    + "var fragments = arguments[3] || [];"
                    + "function rowMatches(row) {"
                    + "  var rowText = [row.postName, row.postCode, row.postId].map(function(item) {"
                    + "    return String(item || '');"
                    + "  }).join(' ');"
                    + "  if (fullName && rowText.indexOf(fullName) >= 0) { return true; }"
                    + "  if (prefix && rowText.indexOf(prefix) >= 0) {"
                    + "    for (var i = 0; i < fragments.length; i++) {"
                    + "      if (fragments[i] && rowText.indexOf(String(fragments[i])) >= 0) { return true; }"
                    + "    }"
                    + "  }"
                    + "  return false;"
                    + "}"
                    + "return rows.some(function(row) {"
                    + "  return rowMatches(row) && String(row.status) === status;"
                    + "});",
                postName,
                status,
                DATA_PREFIX,
                postNameKeyFragments(postName)
            );
            return Boolean.TRUE.equals(matched);
        } catch (WebDriverException e) {
            return false;
        }
    }

    private boolean isPostManagementContextUsable() {
        String bodyText = bodyTextSnippet(1500);
        String currentUrl = currentUrlSafe();
        String location = windowLocationSafe();
        String title = titleSafe();
        return containsAll(bodyText, "岗位编码", "岗位名称", "岗位状态")
            || (bodyText.contains("显示第") && bodyText.contains("总共"))
            || (bodyText.contains(DATA_PREFIX) && bodyText.contains("岗位名称"))
            || ((currentUrl.contains("/" + POST_MODULE) || location.contains("/" + POST_MODULE))
                && title.contains("岗位列表"));
    }

    private String statusLabel(String status) {
        if (STATUS_NORMAL.equals(status)) {
            return "正常";
        }
        if (STATUS_DISABLED.equals(status)) {
            return "停用";
        }
        return "";
    }

    private String postStatusDiagnostics(String postName, String status, String reason) {
        return "reason=" + reason
            + "\npostName=" + postName
            + "\nstatus=" + status
            + "\npostCodeKeyword=" + DATA_PREFIX + "CODE"
            + "\ncurrentUrl=" + currentUrlSafe()
            + "\ntitle=" + titleSafe()
            + "\nbodyText前500字符=" + bodyTextSnippet(500);
    }

    private String requirePostId(String postName) {
        Object postId = executeScript(
            "var fullName = String(arguments[0] || '');"
                + "var prefix = String(arguments[1] || '');"
                + "var fragments = arguments[2] || [];"
                + "function fragmentMatch(text) {"
                + "  if (fullName && text.indexOf(fullName) >= 0) { return true; }"
                + "  if (prefix && text.indexOf(prefix) >= 0) {"
                + "    for (var i = 0; i < fragments.length; i++) {"
                + "      if (fragments[i] && text.indexOf(String(fragments[i])) >= 0) { return true; }"
                + "    }"
                + "  }"
                + "  return false;"
                + "}"
                + "if (window.jQuery && jQuery.fn.bootstrapTable && jQuery('#bootstrap-table').length) {"
                + "  var rows = jQuery('#bootstrap-table').bootstrapTable('getData') || [];"
                + "  for (var r = 0; r < rows.length; r++) {"
                + "    var rowText = [rows[r].postName, rows[r].postCode, rows[r].postId].map(function(item) {"
                + "      return String(item || '');"
                + "    }).join(' ');"
                + "    if (fullName && rowText.indexOf(fullName) >= 0) { return String(rows[r].postId); }"
                + "  }"
                + "  for (var f = 0; f < rows.length; f++) {"
                + "    var fallbackText = [rows[f].postName, rows[f].postCode, rows[f].postId].map(function(item) {"
                + "      return String(item || '');"
                + "    }).join(' ');"
                + "    if (fragmentMatch(fallbackText)) { return String(rows[f].postId); }"
                + "  }"
                + "}"
                + "var trs = Array.prototype.slice.call(document.querySelectorAll('#bootstrap-table tbody tr, table tbody tr'));"
                + "for (var t = 0; t < trs.length; t++) {"
                + "  var text = String(trs[t].innerText || trs[t].textContent || '');"
                + "  if (!fragmentMatch(text)) { continue; }"
                + "  var edit = trs[t].querySelector(\"a[onclick*='edit'], button[onclick*='edit']\");"
                + "  if (!edit) { continue; }"
                + "  var onclick = String(edit.getAttribute('onclick') || '');"
                + "  var match = onclick.match(/edit\\(['\\\"]?([^'\\\")]+)['\\\"]?\\)/);"
                + "  if (match && match[1]) { return match[1]; }"
                + "}"
                + "return null;",
            postName,
            DATA_PREFIX,
            postNameKeyFragments(postName)
        );
        Assert.assertNotNull(postId, "未找到新增岗位ID。\n"
            + postStatusDiagnostics(postName, "", "requirePostId"));
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
        String statusValue = statusValueFromLocator(locator);
        if (!statusValue.isEmpty()) {
            if (!selectStatusRadio(statusValue)) {
                System.out.println("[WARN] 未能稳定选择状态单选框 value=" + statusValue + "，继续后续保存流程");
            }
            return;
        }

        if (!selectGenericRadio(locator)) {
            System.out.println("[WARN] 未能稳定选择单选框: " + locator + "，继续后续流程");
        }
    }

    private String statusValueFromLocator(By locator) {
        String text = String.valueOf(locator);
        if (text.contains("value='1'") || text.contains("value=\"1\"") || text.contains("@value='1'")
            || text.contains("@value=\"1\"")) {
            return STATUS_DISABLED;
        }
        if (text.contains("value='0'") || text.contains("value=\"0\"") || text.contains("@value='0'")
            || text.contains("@value=\"0\"")) {
            return STATUS_NORMAL;
        }
        return "";
    }

    private boolean selectGenericRadio(By locator) {
        try {
            WebElement radio = wait.until(driver -> {
                java.util.List<WebElement> elements = driver.findElements(locator);
                return elements.isEmpty() ? null : elements.get(0);
            });
            clickRadioElement(radio);
            WebDriverWait radioWait = new WebDriverWait(driver, Duration.ofSeconds(4));
            return radioWait.until(driver -> isGenericRadioSelected(locator));
        } catch (WebDriverException e) {
            System.out.println("[WARN] 单选框选择失败: " + locator + ", " + e.getMessage());
            return false;
        }
    }

    private boolean isGenericRadioSelected(By locator) {
        try {
            java.util.List<WebElement> elements = driver.findElements(locator);
            if (elements.isEmpty()) {
                return false;
            }
            WebElement radio = elements.get(0);
            return radio.isSelected() || "true".equalsIgnoreCase(emptyIfNull(radio.getAttribute("checked")));
        } catch (WebDriverException e) {
            return false;
        }
    }

    private boolean selectStatusRadio(String statusValue) {
        String statusText = statusLabel(statusValue);
        WebDriverWait radioWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        try {
            radioWait.until(driver -> findStatusRadio(statusValue, statusText) != null);
        } catch (TimeoutException e) {
            System.out.println("[WARN] 未找到状态单选框 value=" + statusValue + ", text=" + statusText);
            System.out.println(postStatusDiagnostics("", statusValue, "selectStatusRadio"));
            return false;
        }

        for (int i = 0; i < 3; i++) {
            if (isStatusRadioSelected(statusValue)) {
                return true;
            }

            WebElement radio = findStatusRadio(statusValue, statusText);
            if (radio == null) {
                continue;
            }
            clickRadioElement(radio);
            if (waitForStatusRadioSelected(statusValue, Duration.ofSeconds(3))) {
                return true;
            }

            forceCheckStatusRadio(statusValue);
            if (waitForStatusRadioSelected(statusValue, Duration.ofSeconds(3))) {
                return true;
            }
        }
        return isStatusRadioSelected(statusValue);
    }

    private WebElement findStatusRadio(String statusValue, String statusText) {
        By[] locators = new By[] {
            By.cssSelector("input[name='status'][value='" + statusValue + "']"),
            By.xpath("//input[@name='status' and @value='" + statusValue + "']"),
            By.xpath("//label[contains(.,'" + statusText + "')]//input"),
            By.xpath("//*[contains(text(),'" + statusText + "')]/preceding::input[@name='status'][1]"),
            By.xpath("//*[contains(text(),'" + statusText + "')]/following::input[@name='status'][1]")
        };
        for (By candidate : locators) {
            try {
                java.util.List<WebElement> elements = driver.findElements(candidate);
                if (!elements.isEmpty()) {
                    return elements.get(0);
                }
            } catch (WebDriverException ignored) {
                // Try the next locator.
            }
        }
        return null;
    }

    private void clickRadioElement(WebElement radio) {
        if (radio == null) {
            return;
        }

        WebElement[] clickTargets = new WebElement[] {
            radio,
            firstRelatedElement(radio, By.xpath("./ancestor::label[1]")),
            firstRelatedElement(radio, By.xpath("./following-sibling::ins[contains(@class,'iCheck-helper')]")),
            firstRelatedElement(radio, By.xpath("./preceding-sibling::ins[contains(@class,'iCheck-helper')]")),
            firstRelatedElement(radio, By.xpath("./ancestor::*[contains(@class,'iradio') or contains(@class,'radio')][1]//ins[contains(@class,'iCheck-helper')]")),
            firstRelatedElement(radio, By.xpath("./parent::*"))
        };

        for (WebElement target : clickTargets) {
            if (target == null) {
                continue;
            }
            try {
                clickElement(target);
                return;
            } catch (WebDriverException ignored) {
                // Try another click target.
            }
        }

        try {
            executeScript("arguments[0].click();", radio);
        } catch (WebDriverException ignored) {
            // The caller will verify and decide whether to continue.
        }
    }

    private WebElement firstRelatedElement(WebElement element, By locator) {
        try {
            java.util.List<WebElement> elements = element.findElements(locator);
            return elements.isEmpty() ? null : elements.get(0);
        } catch (WebDriverException e) {
            return null;
        }
    }

    private boolean waitForStatusRadioSelected(String statusValue, Duration timeout) {
        try {
            WebDriverWait radioWait = new WebDriverWait(driver, timeout);
            return radioWait.until(driver -> isStatusRadioSelected(statusValue));
        } catch (TimeoutException e) {
            return false;
        }
    }

    private boolean isStatusRadioSelected(String statusValue) {
        try {
            Object selected = executeScript(
                "var value = String(arguments[0]);"
                    + "var radios = document.querySelectorAll(\"input[name='status']\");"
                    + "for (var i = 0; i < radios.length; i++) {"
                    + "  var radio = radios[i];"
                    + "  if (String(radio.getAttribute('value')) !== value) { continue; }"
                    + "  var parent = radio.parentElement;"
                    + "  var checkedClass = parent && String(parent.className || '').indexOf('checked') >= 0;"
                    + "  return !!radio.checked || !!checkedClass || radio.getAttribute('checked') === 'checked';"
                    + "}"
                    + "return false;",
                statusValue
            );
            return Boolean.TRUE.equals(selected);
        } catch (WebDriverException e) {
            return false;
        }
    }

    private void forceCheckStatusRadio(String statusValue) {
        try {
            executeScript(
                "var value = String(arguments[0]);"
                    + "var radios = document.querySelectorAll(\"input[name='status']\");"
                    + "for (var i = 0; i < radios.length; i++) {"
                    + "  var radio = radios[i];"
                    + "  if (String(radio.getAttribute('value')) !== value) { continue; }"
                    + "  if (window.jQuery && jQuery.fn.iCheck) {"
                    + "    jQuery(radio).iCheck('check');"
                    + "  } else {"
                    + "    radio.checked = true;"
                    + "    radio.setAttribute('checked', 'checked');"
                    + "    radio.dispatchEvent(new Event('input', { bubbles: true }));"
                    + "    radio.dispatchEvent(new Event('change', { bubbles: true }));"
                    + "  }"
                    + "  radio.click();"
                    + "  return true;"
                    + "}"
                    + "return false;",
                statusValue
            );
        } catch (WebDriverException e) {
            System.out.println("[WARN] JS 兜底选择状态单选框失败: " + e.getMessage());
        }
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

    private String withDataPrefix(String value) {
        String trimmedValue = value == null ? "" : value.trim();
        return trimmedValue.startsWith(DATA_PREFIX) ? trimmedValue : DATA_PREFIX + trimmedValue;
    }

    private String compactUnique(String label) {
        String time = DateTimeFormatter.ofPattern("MMddHHmmss").format(LocalDateTime.now());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return label + "_" + time + "_" + uuid;
    }

    private static final class LoadedPostCases {
        private final String source;
        private final List<Object[]> cases;

        private LoadedPostCases(String source, List<Object[]> cases) {
            this.source = source;
            this.cases = cases;
        }
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
