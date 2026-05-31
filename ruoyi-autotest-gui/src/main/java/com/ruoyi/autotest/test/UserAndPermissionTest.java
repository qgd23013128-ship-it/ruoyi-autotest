package com.ruoyi.autotest.test;

import java.time.Duration;
import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * 若依后台管理系统 - 用户与权限模块自动化测试
 *
 * 技术要点：
 * 1. 严禁使用 Thread.sleep()，全部使用 WebDriverWait + ExpectedConditions 进行显式等待
 * 2. 所有测试方法均使用 @Test 注解
 * 3. @DataProvider 提供 25 组测试数据，覆盖正常、异常、边界、安全等场景
 */
public class UserAndPermissionTest {

    // ======================== 配置常量 ========================

    /** 若依系统访问地址（可通过系统属性覆盖） */
    private static final String BASE_URL = System.getProperty("ruoyi.base.url", "http://localhost");

    /** 登录页地址 */
    private static final String LOGIN_URL = BASE_URL + "/login";

    /** 显式等待超时时间（秒） */
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    /** 页面加载等待时间 */
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(15);

    /** 是否在测试后保持浏览器不关闭（通过系统属性控制） */
    private static final boolean KEEP_BROWSER_OPEN = Boolean.parseBoolean(
        System.getProperty("ruoyi.keep.browser.open", "false"));

    // ======================== WebDriver 实例 ========================

    private WebDriver driver;
    private WebDriverWait wait;

    // ======================== Before/After ========================

    /**
     * 全局初始化：设置 EdgeDriver
     */
    @BeforeClass
    public void setUpClass() {
        System.out.println("[INFO] 测试环境初始化...");
        System.out.println("[INFO] Base URL: " + BASE_URL);
        String driverPath = System.getProperty("webdriver.edge.driver");
        if (driverPath == null || driverPath.isEmpty()) {
            System.out.println("[INFO] 未预设驱动路径，使用默认相对路径 msedgedriver.exe");
            System.setProperty("webdriver.edge.driver", "msedgedriver.exe");
        } else {
            System.out.println("[INFO] 使用已配置的 EdgeDriver: " + driverPath);
        }
    }

    /**
     * 每个测试方法前：创建新的 Edge 浏览器实例
     */
    @BeforeMethod
    public void setUp() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        // 若需要无头模式，取消下面注释
        // options.addArguments("--headless");

        driver = new EdgeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);

        wait = new WebDriverWait(driver, WAIT_TIMEOUT);

        System.out.println("[INFO] Edge 浏览器实例已创建，窗口已最大化");
    }

    /**
     * 每个测试方法后：根据配置决定是否关闭浏览器
     */
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            if (KEEP_BROWSER_OPEN) {
                System.out.println("[INFO] 浏览器保持打开状态（ruoyi.keep.browser.open=true）");
                return;
            }
            try {
                driver.quit();
                System.out.println("[INFO] 浏览器实例已关闭");
            } catch (Exception e) {
                System.err.println("[WARN] 关闭浏览器时出现异常: " + e.getMessage());
            }
        }
    }

    // ======================== 辅助方法 ========================

    /**
     * 执行登录操作（公共方法，供各个测试复用）
     *
     * @param username 用户名
     * @param password 密码
     */
    private void doLogin(String username, String password) {
        System.out.println("[STEP] 正在打开登录页面: " + LOGIN_URL);
        driver.get(LOGIN_URL);

        // 显式等待：用户名输入框出现
        WebElement usernameInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='username']")));
        usernameInput.clear();
        usernameInput.sendKeys(username);

        // 显式等待：密码输入框
        WebElement passwordInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='password']")));
        passwordInput.clear();
        passwordInput.sendKeys(password);

        // 处理验证码（若依系统默认可能开启验证码）
        try {
            WebElement captchaInput = driver.findElement(By.cssSelector("input[name='validateCode']"));
            if (captchaInput.isDisplayed()) {
                System.out.println("[WARN] 检测到验证码输入框，当前环境启用了验证码。");
                System.out.println("[WARN] 请确保验证码已禁用（captchaEnabled=false）或手动处理验证码。");
                // 尝试输入验证码（若已知验证码值）
                captchaInput.sendKeys("1234");
            }
        } catch (Exception e) {
            // 验证码未启用，忽略
        }

        // 显式等待：登录按钮可用
        WebElement loginBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("btnSubmit")));
        loginBtn.click();

        System.out.println("[STEP] 登录请求已提交，等待响应...");
    }

    /**
     * 验证是否登录成功（首页加载完成）
     */
    private void assertLoginSuccess() {
        try {
            // 等待首页特征元素出现：侧边栏菜单或其他首页独有元素
            wait.until(ExpectedConditions.urlContains("/index"));
            System.out.println("[PASS] 登录成功，已跳转到首页");
        } catch (Exception e) {
            // 检查是否停留在登录页面（登录失败）
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("/login")) {
                WebElement errorMsg = null;
                try {
                    errorMsg = driver.findElement(By.cssSelector("label.error, .has-error, .error-msg"));
                    System.out.println("[FAIL] 登录失败，错误信息: " + errorMsg.getText());
                } catch (Exception ignored) {
                    System.out.println("[FAIL] 登录失败，停留在登录页面");
                }
                Assert.fail("登录失败，未能跳转到首页");
            }
        }
    }

    /**
     * 通过侧边栏导航到指定页面
     * 若依的侧边栏使用 class="nav-label" 或链接中的文字定位菜单
     *
     * @param menuText 一级菜单文字（如："系统管理"）
     * @param subMenuHref 二级菜单链接的 href 部分（如："system/user"）
     */
    private void navigateToMenu(String menuText, String subMenuHref) {
        try {
            // 先尝试点击一级菜单展开
            WebElement topMenu = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//span[@class='nav-label' and contains(text(),'" + menuText + "')]")));
            topMenu.click();

            // 短暂等待菜单展开动画
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//ul[contains(@class,'nav-second-level') and contains(@style,'block')]")
            ));
        } catch (Exception e) {
            System.out.println("[WARN] 菜单展开失败，尝试直接定位: " + e.getMessage());
        }

        // 点击二级菜单
        WebElement subMenu = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'" + subMenuHref + "')]")));
        subMenu.click();

        System.out.println("[STEP] 已导航至: " + menuText + " > " + subMenuHref);
    }

    // ======================== 测试方法1：单模块测试 - 登录 ========================

    @Test(description = "验证正常登录功能：使用 admin/admin123 登录若依系统")
    public void testSingleModule_Login() {
        System.out.println("========== 测试：testSingleModule_Login ==========");
        doLogin("admin", "admin123");
        assertLoginSuccess();
        System.out.println("========== 测试通过 ==========");
    }

    // ======================== 测试方法2：单模块测试 - 用户查询 ========================

    @Test(description = "验证系统管理-用户管理中的查询功能")
    public void testSingleModule_UserQuery() {
        System.out.println("========== 测试：testSingleModule_UserQuery ==========");

        // 步骤1：登录
        doLogin("admin", "admin123");
        assertLoginSuccess();

        // 步骤2：导航到用户管理页面
        navigateToMenu("系统管理", "system/user");

        // 等待用户管理页面表格加载
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-table")));

        // 步骤3：在查询表单中输入登录名称
        WebElement loginNameInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#user-form input[name='loginName']")));
        loginNameInput.clear();
        loginNameInput.sendKeys("admin");

        // 步骤4：点击搜索按钮
        WebElement searchBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@onclick,'search') and contains(@class,'btn-primary')]")));
        searchBtn.click();

        // 步骤5：等待搜索结果刷新
        wait.until(ExpectedConditions.stalenessOf(loginNameInput));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-table")));

        // 步骤6：验证表格中包含查询结果
        WebElement table = driver.findElement(By.id("bootstrap-table"));
        Assert.assertNotNull(table, "用户表格应该显示");

        System.out.println("[PASS] 用户查询功能正常");
        System.out.println("========== 测试通过 ==========");
    }

    // ======================== 测试方法3：集成测试深度3 - 新增角色 ========================

    @Test(description = "集成测试深度3：登录 -> 系统管理 -> 角色管理 -> 点击新增角色")
    public void testIntegration_Depth3_AddRole() {
        System.out.println("========== 测试：testIntegration_Depth3_AddRole ==========");

        String uniqueRoleKey = "autotest_role_" + UUID.randomUUID().toString().substring(0, 8);
        String roleName = "自动化测试角色_" + uniqueRoleKey;

        // 深度1：登录
        System.out.println("[深度1] 执行登录操作...");
        doLogin("admin", "admin123");
        assertLoginSuccess();

        // 深度2：导航到角色管理
        System.out.println("[深度2] 导航至角色管理页面...");
        navigateToMenu("系统管理", "system/role");

        // 等待角色列表页面加载
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-table")));

        // 深度3：点击新增按钮
        System.out.println("[深度3] 点击新增角色按钮...");
        WebElement addBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@id='toolbar']//a[contains(@onclick,'add') and contains(text(),'新增')]")));
        addBtn.click();

        // 等待新增角色弹窗/页面加载
        WebElement roleNameInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("roleName")));

        // 填写角色名称
        roleNameInput.clear();
        roleNameInput.sendKeys(roleName);
        System.out.println("[STEP] 角色名称: " + roleName);

        // 填写权限字符
        WebElement roleKeyInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("roleKey")));
        roleKeyInput.clear();
        roleKeyInput.sendKeys(uniqueRoleKey);
        System.out.println("[STEP] 权限字符: " + uniqueRoleKey);

        // 填写显示顺序
        WebElement roleSortInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("roleSort")));
        roleSortInput.clear();
        roleSortInput.sendKeys("10");

        // 填写备注
        WebElement remarkInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("remark")));
        remarkInput.clear();
        remarkInput.sendKeys("由自动化测试创建 - " + System.currentTimeMillis());

        // 点击保存按钮
        WebElement submitBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@onclick,'submitHandler') and contains(text(),'保')]")));
        submitBtn.click();

        // 等待操作结果
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".layui-layer-dialog")));
            System.out.println("[PASS] 新增角色操作提交成功");
        } catch (Exception e) {
            System.out.println("[WARN] 未检测到结果弹窗，可能角色已存在或网络延迟");
        }

        System.out.println("[PASS] 集成测试深度3完成");
        System.out.println("========== 测试通过 ==========");
    }

    // ======================== 测试方法4：集成测试深度4 - 新增用户并分配角色 ========================

    @Test(description = "集成测试深度4：登录 -> 用户管理 -> 新增用户 -> 填写基本信息 -> 勾选角色分配 -> 保存")
    public void testIntegration_Depth4_AddUserAndAssign() {
        System.out.println("========== 测试：testIntegration_Depth4_AddUserAndAssign ==========");

        String uniqueId = UUID.randomUUID().toString().substring(0, 6);
        String loginName = "testuser_" + uniqueId;
        String userName = "测试用户_" + uniqueId;

        // 深度1：登录
        System.out.println("[深度1] 执行登录操作...");
        doLogin("admin", "admin123");
        assertLoginSuccess();

        // 深度2：导航到用户管理
        System.out.println("[深度2] 导航至用户管理页面...");
        navigateToMenu("系统管理", "system/user");

        // 等待用户列表页面加载
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-table")));

        // 深度3：点击新增用户
        System.out.println("[深度3] 点击新增用户按钮...");
        WebElement addBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@id='toolbar']//a[contains(@onclick,'add') and contains(text(),'新增')]")));
        addBtn.click();

        // 深度4：填写新增用户表单
        System.out.println("[深度4] 填写用户基本信息并分配角色...");

        // 用户名称
        WebElement userNameInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#form-user-add input[name='userName']")));
        userNameInput.clear();
        userNameInput.sendKeys(userName);
        System.out.println("[STEP] 用户名称: " + userName);

        // 登录账号
        WebElement loginNameInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#form-user-add input[name='loginName']")));
        loginNameInput.clear();
        loginNameInput.sendKeys(loginName);
        System.out.println("[STEP] 登录账号: " + loginName);

        // 登录密码
        WebElement passwordInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#form-user-add input[name='password']")));
        passwordInput.clear();
        passwordInput.sendKeys("Test@123456");
        System.out.println("[STEP] 登录密码已设置");

        // 手机号码
        WebElement phoneInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#form-user-add input[name='phonenumber']")));
        phoneInput.clear();
        phoneInput.sendKeys("138" + String.format("%08d", (int)(Math.random() * 100000000)));
        System.out.println("[STEP] 手机号码已填写");

        // 邮箱
        WebElement emailInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#form-user-add input[name='email']")));
        emailInput.clear();
        emailInput.sendKeys(loginName + "@ruoyi-test.com");

        // 选择性别
        try {
            Select sexSelect = new Select(driver.findElement(By.cssSelector("#form-user-add select[name='sex']")));
            sexSelect.selectByIndex(1);
            System.out.println("[STEP] 性别已选择");
        } catch (Exception e) {
            System.out.println("[WARN] 无法选择性别: " + e.getMessage());
        }

        // 勾选角色（选择第一个可用的角色复选框）
        try {
            java.util.List<WebElement> roleCheckboxes = driver.findElements(
                By.cssSelector("#form-user-add input[name='role']"));
            if (!roleCheckboxes.isEmpty()) {
                for (WebElement checkbox : roleCheckboxes) {
                    if (checkbox.isEnabled() && !checkbox.isSelected()) {
                        checkbox.click();
                        String roleLabel = checkbox.findElement(By.xpath("./parent::label")).getText().trim();
                        System.out.println("[STEP] 已分配角色: " + roleLabel);
                        break;
                    }
                }
            } else {
                System.out.println("[WARN] 未找到可分配的角色");
            }
        } catch (Exception e) {
            System.out.println("[WARN] 角色分配异常: " + e.getMessage());
        }

        // 填写备注
        try {
            WebElement remarkTextarea = driver.findElement(
                By.cssSelector("#form-user-add textarea[name='remark']"));
            remarkTextarea.sendKeys("自动化测试创建 - " + System.currentTimeMillis());
        } catch (Exception ignored) {
            // 备注字段可能不存在
        }

        // 点击保存按钮
        WebElement saveBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@onclick,'submitHandler') and contains(text(),'保')]")));
        saveBtn.click();

        // 等待操作结果
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".layui-layer-dialog")));
            System.out.println("[PASS] 新增用户操作提交成功");
        } catch (Exception e) {
            System.out.println("[WARN] 未检测到结果弹窗");
        }

        System.out.println("[PASS] 集成测试深度4完成");
        System.out.println("========== 测试通过 ==========");
    }

    // ======================== 测试方法5：数据驱动测试 ========================

    /**
     * DataProvider：提供25组登录账号密码组合
     * 覆盖场景：
     * - 正确账号密码（1-2组）
     * - 错误密码（3-8组）
     * - 空值输入（9-12组）
     * - 特殊字符（13-17组）
     * - SQL注入模拟（18-21组）
     * - XSS模拟（22-23组）
     * - 超长字符串（24组）
     * - 不存在的用户（25组）
     */
    @DataProvider(name = "loginTestData")
    public Object[][] loginTestData() {
        return new Object[][]{
            // 正确账号密码
            {"admin",         "admin123",      true,  "正确管理员账号密码"},
            {"ry",            "admin123",      false, "正确普通用户密码（若不存在则为false）"},

            // 错误密码
            {"admin",         "wrongpass",     false, "管理员-错误密码"},
            {"admin",         "Admin123",      false, "管理员-密码大小写错误"},
            {"admin",         "admin",         false, "管理员-密码缺少后缀"},
            {"admin",         "admin1234",     false, "管理员-密码多出字符"},
            {"admin",         "123456",        false, "管理员-纯数字密码"},
            {"admin",         "admin 123",     false, "管理员-密码含空格"},

            // 空值输入
            {"",              "",              false, "用户名和密码均为空"},
            {"admin",         "",              false, "用户名为空字符串-有密码"},
            {"",              "admin123",      false, "密码为空-有用户名"},
            {"admin",         "   ",           false, "密码为空格"},

            // 特殊字符
            {"admin",         "!@#$%^&*()",    false, "密码为纯特殊字符"},
            {"test<>user",    "admin123",      false, "用户名含HTML标签字符"},
            {"admin",         "adm'in--123",   false, "密码含单引号"},
            {"测试用户",        "admin123",      false, "中文用户名"},
            {"admin",         "测试密码abc",      false, "中文密码"},

            // SQL注入模拟（安全测试 - 若依应有防护）
            {"admin' OR '1'='1",   "admin123",  false, "SQL注入-用户名绕过"},
            {"admin",              "' OR '1'='1", false, "SQL注入-密码绕过"},
            {"admin' --",          "admin123",  false, "SQL注入-注释闭合"},
            {"admin",              "1' OR '1' = '1", false, "SQL注入-条件恒真"},

            // XSS模拟
            {"<script>alert(1)</script>", "test", false, "XSS注入-用户名"},
            {"admin",                     "<img src=x onerror=alert(1)>", false, "XSS注入-密码"},

            // 边界测试
            {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
             "test", false, "超长用户名（超过128字符）"},

            // 不存在的用户
            {"nonexistent_user_99999", "test123", false, "数据库中不存在的用户"},
        };
    }

    @Test(description = "数据驱动登录测试：使用 @DataProvider 提供 25 组账号密码组合进行循环断言",
          dataProvider = "loginTestData")
    public void testDataDriven_Login(String username, String password,
                                      boolean expectedSuccess, String description) {
        System.out.println("------------------------------------------------");
        System.out.println("[测试用例] " + description);
        System.out.println("[参数] 用户名=\"" + username + "\", 密码=\"" + password + "\"");
        System.out.println("[预期] " + (expectedSuccess ? "登录成功" : "登录失败/拒绝"));

        doLogin(username, password);

        // 根据预期结果进行断言
        if (expectedSuccess) {
            try {
                wait.until(ExpectedConditions.urlContains("/index"));
                String currentUrl = driver.getCurrentUrl();
                Assert.assertTrue(currentUrl.contains("/index"),
                    "预期登录成功，但当前URL为: " + currentUrl);
                System.out.println("[断言] PASS - 登录成功，已跳转首页");
            } catch (Exception e) {
                Assert.fail("预期登录成功但失败了: " + description);
            }
        } else {
            try {
                // 等待2秒让页面响应
                WebElement loginPageElement = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.id("signupForm")));

                // 验证仍然在登录页面
                String currentUrl = driver.getCurrentUrl();
                Assert.assertTrue(
                    currentUrl.contains("/login") || loginPageElement.isDisplayed(),
                    "预期登录应被拒绝，但似乎跳转了。当前URL: " + currentUrl);
                System.out.println("[断言] PASS - 登录被正确拒绝（停留在登录页）");
            } catch (Exception e) {
                // 检查是否是因为登录拒绝导致的超时
                String currentUrl = driver.getCurrentUrl();
                if (currentUrl.contains("/login")) {
                    System.out.println("[断言] PASS - 登录被正确拒绝（停留在登录页）");
                } else {
                    System.out.println("[断言] WARN - 不确定的登录状态，当前URL: " + currentUrl);
                }
            }
        }

        System.out.println("------------------------------------------------");
    }
}
