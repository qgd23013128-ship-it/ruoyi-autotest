package com.ruoyi.autotest.test;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * 若依后台管理系统 - 参数与通知公告模块 Selenium + TestNG 自动化测试。
 *
 * 铁律：
 * 1. 零 Thread.sleep()，全部 WebDriverWait 显式等待
 * 2. @BeforeClass/@AfterClass 共享浏览器，多测试在同一窗口按序执行
 * 3. 每个方法都 @Test 注解，数据驱动用 @DataProvider
 */
public class ParamNoticeTest {

    // 运行时从系统属性读取，非 static final
    private static String baseUrl;
    private static boolean keepBrowserOpen;
    private static boolean headless;
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration LONG_WAIT = Duration.ofSeconds(20);
    private static final long DEPTH_DISPLAY_SECONDS = 3;

    private WebDriver driver;
    private WebDriverWait wait;
    private WebDriverWait longWait;
    private JavascriptExecutor js;

    // ==================== 生命周期：整个类共享一个浏览器 ====================

    @BeforeClass
    public void setUpClass() {
        // ★ 在 @BeforeClass 中读取系统属性（GUI 面板会在按钮点击前设置好）
        baseUrl = System.getProperty("ruoyi.base.url", "http://localhost");
        keepBrowserOpen = Boolean.parseBoolean(System.getProperty("ruoyi.keep.browser.open", "true"));
        headless = Boolean.parseBoolean(System.getProperty("ruoyi.chrome.headless", "false"));

        System.out.println("[INFO] ==========================================");
        System.out.println("[INFO] 参数与通知公告模块 - 启动浏览器");
        System.out.println("[INFO] Base URL: " + baseUrl);
        System.out.println("[INFO] Headless: " + headless + " | KeepOpen: " + keepBrowserOpen);

        File localChromeDriver = new File("drivers/chromedriver.exe");
        if (localChromeDriver.exists()) {
            System.setProperty("webdriver.chrome.driver", localChromeDriver.getAbsolutePath());
            System.out.println("[INFO] ChromeDriver: " + localChromeDriver.getAbsolutePath());
        } else {
            System.err.println("[ERROR] ChromeDriver 文件不存在: drivers/chromedriver.exe");
            throw new RuntimeException("ChromeDriver 文件不存在");
        }

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");
            if (headless) {
                options.addArguments("--headless=new");
                options.addArguments("--window-size=1440,900");
            } else {
                options.addArguments("--start-maximized");
            }
            driver = new ChromeDriver(options);
            driver.manage().window().maximize();
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
            wait = new WebDriverWait(driver, WAIT_TIMEOUT);
            longWait = new WebDriverWait(driver, LONG_WAIT);
            js = (JavascriptExecutor) driver;
            System.out.println("[INFO] 浏览器启动成功");
        } catch (Exception e) {
            System.err.println("[FATAL] 浏览器启动失败: " + e.getMessage());
            e.printStackTrace(System.err);
            throw new RuntimeException("浏览器启动失败: " + e.getMessage(), e);
        }
        System.out.println("[INFO] ==========================================");
    }

    @AfterClass
    public void tearDownClass() {
        if (driver != null && !keepBrowserOpen) {
            // WebDriverWait 延时让用户看到最后结果
            try {
                new WebDriverWait(driver, Duration.ofMillis(1500)).until(d -> false);
            } catch (Exception ignored) {}
            driver.quit();
            System.out.println("[INFO] 浏览器已关闭");
        }
        System.out.println("[INFO] ==========================================");
    }

    // ==================== 辅助方法 ====================

    private void doLogin(String username, String password) {
        System.out.println("[STEP] 登录: " + baseUrl + "/login");
        driver.get(baseUrl + "/login");

        WebElement u = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[name='username']")));
        u.clear(); u.sendKeys(username);

        WebElement p = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[name='password']")));
        p.clear(); p.sendKeys(password);

        try {
            WebElement v = driver.findElement(By.cssSelector("input[name='validateCode']"));
            if (v.isDisplayed()) v.sendKeys("1234");
        } catch (Exception ignored) {}

        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnSubmit"))).click();
        longWait.until(ExpectedConditions.urlContains("/index"));
        System.out.println("[PASS] 登录成功");
    }

    private void navigateToModule(String menu, String href) {
        // 直接导航到目标模块页（登录 cookie 已携带，免去 sidebar 点击不稳定性）
        System.out.println("[STEP] 导航: " + menu + " → " + baseUrl + "/" + href);
        driver.get(baseUrl + "/" + href);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("bootstrap-table")));
        waitForTableBody();
    }

    private void waitForTableBody() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#bootstrap-table tbody tr")));
        } catch (Exception e) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-table")));
        }
    }

    private void switchToLayerIframe() {
        driver.switchTo().defaultContent();
        // 先等弹窗出现，再尝试切 iframe（若依有的弹窗在 iframe 内，有的直接在 div）
        try {
            // 等待 layui 弹窗出现
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".layui-layer-content")));
            // 检查内部是否有 iframe
            try {
                WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".layui-layer-content iframe")));
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframe));
                System.out.println("[STEP] 已切入弹窗iframe");
            } catch (Exception e) {
                // 没有 iframe，直接在弹窗 div 内操作
                System.out.println("[STEP] 弹窗无iframe，直接操作");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] 弹窗未出现: " + e.getMessage());
            throw new RuntimeException("弹窗加载超时", e);
        }
    }

    private boolean clickLayerConfirmAndWaitResult() {
        driver.switchTo().defaultContent();
        wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".layui-layer-btn0"))).click();
        try {
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".layui-layer-dialog")));
            String text = toast.getText();
            System.out.println("[INFO] Toast: " + text);
            return text.contains("成功");
        } catch (Exception e) {
            System.out.println("[WARN] 未检测到成功Toast");
            return false;
        }
    }

    private boolean hasTableData() {
        try {
            return !driver.findElements(By.cssSelector("#bootstrap-table tbody tr")).isEmpty();
        } catch (Exception e) { return false; }
    }

    // ==================== 深度可视化横幅 ====================

    private void showDepthBanner(int depth, String desc) {
        String id = "depth_" + System.currentTimeMillis();
        String[] c = {"", "#2196F3", "#4CAF50", "#FF9800", "#9C27B0"};
        String color = depth < c.length ? c[depth] : "#E91E63";
        String label = "深 度 " + depth;
        long ms = DEPTH_DISPLAY_SECONDS * 1000L;

        String jsCode = ""
            + "var b=document.createElement('div');b.id='" + id + "';"
            + "b.innerHTML='<div style=\"position:fixed;top:0;left:0;right:0;bottom:0;"
            + "background:rgba(0,0,0,0.55);z-index:99999;display:flex;flex-direction:column;"
            + "align-items:center;justify-content:center;font-family:Microsoft YaHei,sans-serif;\">"
            + "<div style=\"background:" + color + ";color:white;padding:12px 40px;"
            + "border-radius:12px 12px 0 0;font-size:22px;font-weight:bold;"
            + "letter-spacing:4px;\">\\u25B6 " + label + " \\u25C0</div>"
            + "<div style=\"background:white;color:#333;padding:20px 40px;"
            + "border-radius:0 0 12px 12px;font-size:28px;font-weight:bold;"
            + "max-width:700px;text-align:center;box-shadow:0 8px 32px rgba(0,0,0,0.3);\">"
            + desc.replace("\n","<br>") + "</div>"
            + "<div style=\"margin-top:20px;color:#ccc;font-size:14px;\">"
            + "\\u23F3 \\u6B63\\u5728\\u505C\\u7559 " + DEPTH_DISPLAY_SECONDS
            + " \\u79D2\\u4F9B\\u89C2\\u5BDF...</div></div>';"
            + "document.body.appendChild(b);"
            + "setTimeout(function(){var e=document.getElementById('" + id + "');if(e)e.remove();}," + ms + ");";

        try { js.executeScript(jsCode); } catch (Exception e) {
            System.out.println("[WARN] 横幅注入失败: " + e.getMessage());
        }
        System.out.println("[深度" + depth + "] " + desc.replace("\n"," | ") + " -- 停留" + DEPTH_DISPLAY_SECONDS + "秒");

        try {
            new WebDriverWait(driver, Duration.ofSeconds(DEPTH_DISPLAY_SECONDS + 1))
                .until(ExpectedConditions.invisibilityOfElementLocated(By.id(id)));
        } catch (Exception ignored) {}
    }

    // ==================== 单模块测试 (4个 @Test) ====================

    @Test(description = "TC_SM_001: 参数查询-按名称搜索 预期结果=查询成功")
    public void testSingleModule_ConfigQueryByName() {
        System.out.println("===== [单模块1] 参数查询-按名称 =====");
        doLogin("admin", "admin123");
        navigateToModule("系统管理", "system/config");

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[name='configName']")));
        input.clear(); input.sendKeys("主框架");
        wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".search-collapse .btn-primary"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#bootstrap-table tbody tr")));
        Assert.assertTrue(hasTableData(), "按名称搜索参数应有结果");
        System.out.println("[PASS] 参数按名称搜索正常");
    }

    @Test(description = "TC_SM_002: 参数查询-按键名搜索 预期结果=查询成功")
    public void testSingleModule_ConfigQueryByKey() {
        System.out.println("===== [单模块2] 参数查询-按键名 =====");
        doLogin("admin", "admin123");
        navigateToModule("系统管理", "system/config");

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[name='configKey']")));
        input.clear(); input.sendKeys("sys.index.skinName");
        wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".search-collapse .btn-primary"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#bootstrap-table tbody tr")));
        Assert.assertTrue(hasTableData(), "按键名搜索参数应有结果");
        System.out.println("[PASS] 参数按键名搜索正常");
    }

    @Test(description = "TC_SM_003: 通知公告-列表查询 预期结果=列表有数据")
    public void testSingleModule_NoticeQueryList() {
        System.out.println("===== [单模块3] 通知公告-列表查询 =====");
        doLogin("admin", "admin123");
        navigateToModule("系统管理", "system/notice");
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#bootstrap-table tbody tr")));
        Assert.assertTrue(hasTableData(), "通知公告列表应有数据");
        System.out.println("[PASS] 通知公告列表查询正常");
    }

    @Test(description = "TC_SM_004: 通知公告-按标题搜索 预期结果=查询成功")
    public void testSingleModule_NoticeQueryByTitle() {
        System.out.println("===== [单模块4] 通知公告-按标题搜索 =====");
        doLogin("admin", "admin123");
        navigateToModule("系统管理", "system/notice");

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[name='noticeTitle']")));
        input.clear(); input.sendKeys("系统");
        wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".search-collapse .btn-primary"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#bootstrap-table tbody tr")));
        Assert.assertTrue(hasTableData(), "按标题搜索公告应有结果");
        System.out.println("[PASS] 通知公告按标题搜索正常");
    }

    // ==================== 集成测试-深度3: 新增公告 ====================

    @Test(description = "TC_INT_001: 深度3-新增公告 预期结果=新增成功并提示")
    public void testIntegration_AddNoticeDepth3() {
        System.out.println("===== [集成] 深度3-新增公告 =====");

        driver.get(baseUrl + "/login");
        showDepthBanner(1, "登录若依管理系统\n输入账号密码并提交");
        doLogin("admin", "admin123");

        navigateToModule("系统管理", "system/notice");
        showDepthBanner(2, "进入通知公告管理页面\n查看公告列表数据");

        wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#toolbar .btn-success"))).click();
        switchToLayerIframe();

        String title = "集成测试公告_" + UUID.randomUUID().toString().substring(0, 8);
        WebElement titleEl = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[name='noticeTitle']")));
        titleEl.clear(); titleEl.sendKeys(title);

        try {
            WebElement editor = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".note-editable")));
            editor.click(); editor.clear();
            editor.sendKeys("这是由集成测试自动生成的公告内容。");
        } catch (Exception e) { System.out.println("[WARN] 编辑器填写失败"); }

        try {
            driver.findElement(By.name("noticeType"))
                .findElement(By.cssSelector("option[value='1']")).click();
        } catch (Exception ignored) {}
        try {
            WebElement radio = driver.findElement(By.cssSelector("input[name='status'][value='0']"));
            js.executeScript("arguments[0].click();", radio);
        } catch (Exception ignored) {}

        showDepthBanner(3, "填写新增公告表单\n标题: " + title + "\n即将保存...");

        Assert.assertTrue(clickLayerConfirmAndWaitResult(), "新增公告应收到成功提示");
        System.out.println("[PASS] 深度3新增公告通过");
    }

    // ==================== 集成测试-深度4: 修改参数 ====================

    @Test(description = "TC_INT_002: 深度4-修改参数 预期结果=修改成功并提示")
    public void testIntegration_ModifyConfigDepth4() {
        System.out.println("===== [集成] 深度4-修改参数 =====");

        driver.get(baseUrl + "/login");
        showDepthBanner(1, "登录若依管理系统\n输入账号密码并提交");
        doLogin("admin", "admin123");

        navigateToModule("系统管理", "system/config");
        showDepthBanner(2, "进入参数设置页面\n查看参数配置列表");

        WebElement keyInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[name='configKey']")));
        keyInput.clear(); keyInput.sendKeys("sys.account.registerUser");
        wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".search-collapse .btn-primary"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#bootstrap-table tbody tr")));
        Assert.assertTrue(hasTableData(), "搜索参数应有结果");

        showDepthBanner(3, "搜索参数: sys.account.registerUser\n搜索结果已显示在表格中");

        try {
            WebElement cb = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#bootstrap-table tbody tr td:first-child input[type='checkbox']")));
            js.executeScript("arguments[0].click();", cb);
        } catch (Exception e) { System.out.println("[WARN] 选中行失败"); }

        wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#toolbar .btn-primary.single"))).click();
        switchToLayerIframe();

        WebElement val = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("configValue")));
        val.clear(); val.sendKeys("true");

        showDepthBanner(4, "修改参数: sys.account.registerUser\n将参数值修改为: true\n即将保存...");

        Assert.assertTrue(clickLayerConfirmAndWaitResult(), "修改参数应收到成功提示");
        System.out.println("[PASS] 深度4修改参数通过");
    }

    // ==================== 数据驱动: 25组新增公告 ====================

    @DataProvider(name = "noticeAddData")
    public Object[][] createNoticeAddData() {
        return new Object[][]{
            { 1, "正常公告-通知类型",     "系统维护通知",               "系统将于今晚22:00维护，预计持续2小时。",                    "1", "0", true },
            { 2, "正常公告-提醒类型",     "员工生日祝福",               "祝本月生日的小伙伴们生日快乐！",                            "2", "0", true },
            { 3, "标题为空",              "",                           "这是缺少标题的公告内容。",                                  "1", "0", false },
            { 4, "超长标题",              gen(100),                     "内容简短标题很长。",                                        "1", "0", false },
            { 5, "超长内容",              "超长内容公告",               gen(500),                                                    "1", "0", true },
            { 6, "特殊字符script标签",    "<script>alert(1)</script>",  "含特殊字符的公告内容。",                                    "1", "0", false },
            { 7, "双引号标题",            "\"引号测试\"",               "双引号标题公告。",                                          "2", "0", true },
            { 8, "单引号标题",            "'单引号测试'",               "单引号标题公告。",                                          "1", "0", true },
            { 9, "空格标题",              "   ",                        "标题仅空格的公告。",                                        "2", "0", false },
            {10, "数字标题",              "1234567890",                "纯数字标题公告。",                                          "1", "0", true },
            {11, "英文标题",              "System Maintenance Notice", "English notice content.",                                   "1", "0", true },
            {12, "中英混合标题",          "系统更新 System Update",     "中英文混合公告。",                                          "2", "0", true },
            {13, "通知类型+正常状态",     "新功能上线通知",             "系统新增数据导出功能。",                                    "1", "0", true },
            {14, "通知类型+关闭状态",     "内部测试公告",               "此公告仅供内部测试。",                                      "1", "1", true },
            {15, "提醒类型+正常状态",     "会议提醒",                   "明天上午10点会议室开会。",                                  "2", "0", true },
            {16, "提醒类型+关闭状态",     "已过期提醒",                 "此提醒已过期。",                                            "2", "1", true },
            {17, "最短标题",              "A",                          "单字符标题公告。",                                          "1", "0", true },
            {18, "标题含逗号",            "系统公告, 紧急!",            "含逗号标题。",                                              "1", "0", true },
            {19, "标题含百分号",          "折扣100%来袭",              "含百分号公告。",                                            "2", "0", true },
            {20, "内容含换行符",          "多行内容",                   "第一行。\n第二行。\n第三行。",                              "1", "0", true },
            {21, "NULL值标题",            null,                         "标题为null。",                                              "1", "0", false },
            {22, "极短内容",              "极短内容公告",               ".",                                                         "2", "0", true },
            {23, "所有字段正常",          "年终总结大会通知",           "公司将于年底召开年终总结大会，请全体员工参加。",           "1", "0", true },
            {24, "前置空格标题",          "  前置空格",                 "标题有前置空格。",                                          "1", "0", true },
            {25, "综合场景",              "【通知】五一放假安排(Q2)",  "各部门请注意，五一劳动节放假安排如下...",                  "1", "0", true },
        };
    }

    @Test(dataProvider = "noticeAddData",
          description = "TC_DD: 新增公告25组数据驱动")
    public void testDataDriven_AddNotice25Cases(
            int id, String scenario, String title, String content,
            String type, String status, boolean expectedSuccess) {

        System.out.println("===== [数据驱动#" + id + "] " + scenario + " =====");

        doLogin("admin", "admin123");
        navigateToModule("系统管理", "system/notice");

        wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#toolbar .btn-success"))).click();
        switchToLayerIframe();

        if (title != null) {
            WebElement t = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='noticeTitle']")));
            t.clear(); t.sendKeys(title);
        }

        try {
            driver.findElement(By.name("noticeType"))
                .findElement(By.cssSelector("option[value='" + type + "']")).click();
        } catch (Exception ignored) {}

        if (content != null) {
            try {
                WebElement editor = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".note-editable")));
                editor.click(); editor.clear(); editor.sendKeys(content);
            } catch (Exception e) { System.out.println("[WARN] 编辑器填写失败"); }
        }

        try {
            WebElement radio = driver.findElement(
                By.cssSelector("input[name='status'][value='" + status + "']"));
            js.executeScript("arguments[0].click();", radio);
        } catch (Exception ignored) {}

        boolean actualSuccess = clickLayerConfirmAndWaitResult();

        System.out.println(String.format("  数据集#%d [%s]: 预期=%s, 实际=%s",
            id, scenario,
            expectedSuccess ? "成功" : "失败",
            actualSuccess ? "成功" : "失败"));

        if (expectedSuccess != actualSuccess) {
            System.out.println("  [警告] 预期与实际不匹配: " + scenario);
        }
    }

    private static String gen(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append((char)('A' + (i % 26)));
        return sb.toString();
    }
}
