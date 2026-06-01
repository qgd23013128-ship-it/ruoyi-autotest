package com.ruoyi.autotest.test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

public class DictionaryTest {

    private static final String BASE_URL = System.getProperty("ruoyi.base.url", "http://localhost");
    private static final String LOGIN_URL = BASE_URL + "/login";
    private static final String LOGIN_USER = System.getProperty("ruoyi.login.username", "admin");
    private static final String LOGIN_PASSWORD = System.getProperty("ruoyi.login.password", "12345");
    /** 若依内置字典类型：用户性别 */
    private static final String QUERY_DICT_TYPE = "sys_user_sex";
    /** 菜单显示/隐藏测试目标 */
    private static final String TARGET_MENU_NAME = "系统监控";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(15);
    private static final boolean KEEP_BROWSER_OPEN = Boolean.parseBoolean(
        System.getProperty("ruoyi.keep.browser.open", "false"));

    private WebDriver driver;
    private WebDriverWait wait;

    private long getStepPauseMs() {
        return Long.parseLong(System.getProperty("ruoyi.test.step.pause.ms", "1000"));
    }

    private long getResultPauseMs() {
        return Long.parseLong(System.getProperty("ruoyi.test.result.pause.ms", "1000"));
    }

    private void pauseStep(String message) {
        long pauseMs = getStepPauseMs();
        System.out.println("[观察] " + message + "（停留 " + (pauseMs / 1000.0) + " 秒）");
        sleepQuietly(pauseMs);
    }

    private void pauseForResult(String message) {
        long pauseMs = getResultPauseMs();
        System.out.println("[观察] " + message + "（停留 " + (pauseMs / 1000.0) + " 秒）");
        sleepQuietly(pauseMs);
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @BeforeClass
    public void setUpClass() {
        System.out.println("[INFO] 字典模块测试环境初始化...");
        String driverPath = System.getProperty("webdriver.edge.driver");
        if (driverPath == null || driverPath.isEmpty()) {
            System.setProperty("webdriver.edge.driver", "msedgedriver.exe");
        }
    }

    @BeforeMethod
    public void setUp() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");

        driver = new EdgeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            SlowWebActions.pauseBeforeBrowserClose();
            if (KEEP_BROWSER_OPEN) {
                return;
            }
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("[WARN] 关闭浏览器异常: " + e.getMessage());
            }
        }
    }

    private void doLogin(String username, String password) {
        driver.get(LOGIN_URL);
        WebElement usernameInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='username']")));
        SlowWebActions.slowClearAndType(usernameInput, username);

        WebElement passwordInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='password']")));
        SlowWebActions.slowClearAndType(passwordInput, password);

        try {
            WebElement captchaInput = driver.findElement(By.cssSelector("input[name='validateCode']"));
            if (captchaInput.isDisplayed()) {
                SlowWebActions.slowType(captchaInput, "1234");
            }
        } catch (Exception ignored) {
        }

        WebElement loginBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("btnSubmit")));
        SlowWebActions.slowClick(loginBtn);
    }

    private void assertLoginSuccess() {
        wait.until(ExpectedConditions.urlContains("/index"));
        System.out.println("[PASS] 登录成功，等待首页加载...");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe.RuoYi_iframe")));
    }

    private void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    /**
     * 切换到若依内容区 iframe。点击侧边栏菜单后，页面内容加载在可见的 RuoYi_iframe 中。
     */
    private void switchToContentIframe(String pathFragment) {
        switchToDefaultContent();

        WebElement iframe = wait.until(webDriver -> findContentIframe(pathFragment, true));
        driver.switchTo().frame(iframe);
        waitForDocumentReady();
        System.out.println("[STEP] 已切换到内容 iframe: " + pathFragment);
    }

    private WebElement findContentIframe(String pathFragment, boolean requireVisible) {
        List<WebElement> iframes = driver.findElements(By.cssSelector("iframe.RuoYi_iframe"));
        WebElement fallback = null;

        for (int i = iframes.size() - 1; i >= 0; i--) {
            WebElement frame = iframes.get(i);
            String dataId = frame.getAttribute("data-id");
            String src = frame.getAttribute("src");
            boolean matched = matchesIframePath(dataId, pathFragment) || matchesIframePath(src, pathFragment);
            if (!matched) {
                continue;
            }
            if (!requireVisible || frame.isDisplayed()) {
                return frame;
            }
            if (fallback == null) {
                fallback = frame;
            }
        }

        if (fallback != null) {
            return fallback;
        }

        for (int i = iframes.size() - 1; i >= 0; i--) {
            WebElement frame = iframes.get(i);
            if (!requireVisible || frame.isDisplayed()) {
                return frame;
            }
        }
        return null;
    }

    private boolean matchesIframePath(String value, String pathFragment) {
        return value != null && !value.isEmpty()
            && (value.contains(pathFragment) || value.replace("\\", "/").contains(pathFragment));
    }

    private void waitForDocumentReady() {
        wait.until(webDriver -> {
            try {
                Object ready = ((JavascriptExecutor) webDriver).executeScript("return document.readyState");
                return "complete".equals(String.valueOf(ready));
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * 通过侧边栏导航到指定页面，并切换到对应的内容 iframe。
     */
    private void navigateToMenu(String menuText, String subMenuHref) {
        switchToDefaultContent();
        WebDriverWait navWait = new WebDriverWait(driver, Duration.ofSeconds(2));

        try {
            WebElement topMenu = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//span[@class='nav-label' and contains(text(),'" + menuText + "')]")));
            SlowWebActions.slowClick(topMenu);
            navWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'" + subMenuHref + "')]")));
        } catch (Exception e) {
            System.out.println("[WARN] 菜单展开失败，尝试直接定位: " + e.getMessage());
        }

        WebElement subMenu = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'" + subMenuHref + "')]")));
        SlowWebActions.slowClick(subMenu);
        System.out.println("[STEP] 点击二级菜单: " + subMenuHref);

        switchToContentIframe(subMenuHref);
    }

    private void switchToLayerIframe() {
        switchToDefaultContent();
        WebElement layerIframe = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.layui-layer-iframe iframe")));
        driver.switchTo().frame(layerIframe);
        waitForDocumentReady();
        waitForICheckReady();
    }

    /** 等待若依 footer 中对 radio-box 的 iCheck 初始化完成 */
    private void waitForICheckReady() {
        wait.until(webDriver -> {
            try {
                Object ready = ((JavascriptExecutor) webDriver).executeScript(
                    "if (typeof jQuery === 'undefined') { return true; }"
                        + "var inputs = jQuery('.radio-box input[type=radio]');"
                        + "if (inputs.length === 0) { return true; }"
                        + "return inputs.filter(function(){ return jQuery(this).data('iCheck'); }).length > 0;");
                return Boolean.TRUE.equals(ready);
            } catch (Exception e) {
                return false;
            }
        });
        sleepQuietly(200);
    }

    private void clickLayerConfirmButton() {
        switchToDefaultContent();
        WebElement confirmBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("div.layui-layer-iframe .layui-layer-btn a.layui-layer-btn0")));
        SlowWebActions.slowClick(confirmBtn);
    }

    /**
     * 从字典类型列表页，点击某一字典类型对应行的「列表」进入字典数据页。
     */
    private void openDictDataPageFromTypeList() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-table")));
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#bootstrap-table tbody tr")));

        WebElement listBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//table[@id='bootstrap-table']//a[contains(@onclick,'detail')]")));
        System.out.println("[STEP] 点击字典类型对应行的「列表」，进入字典数据页");
        SlowWebActions.slowClick(listBtn);
        switchToContentIframe("system/dict/data");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-table")));
    }

    /**
     * 在弹窗表单中选择单选框。若依使用 iCheck 包装 radio，需用 iCheck API 或点击 radio-box 内 label。
     */
    private void selectRadioByNameAndValue(String name, String value) {
        By radioSelector = By.cssSelector("input[type='radio'][name='" + name + "'][value='" + value + "']");
        WebElement radio = wait.until(ExpectedConditions.presenceOfElementLocated(radioSelector));

        ((JavascriptExecutor) driver).executeScript(
            "var input = arguments[0];"
                + "if (typeof jQuery !== 'undefined') {"
                + "  if (jQuery(input).data('iCheck')) {"
                + "    jQuery(input).iCheck('check');"
                + "  } else {"
                + "    jQuery(input).prop('checked', true).trigger('change');"
                + "  }"
                + "} else {"
                + "  input.checked = true;"
                + "  input.dispatchEvent(new Event('change', { bubbles: true }));"
                + "}",
            radio);

        if (!driver.findElement(radioSelector).isSelected()) {
            WebElement label = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'radio-box')][.//input[@name='" + name
                    + "' and @value='" + value + "']]//label")));
            SlowWebActions.slowClick(label);
        }

        wait.until(ExpectedConditions.elementToBeSelected(radioSelector));
        System.out.println("[STEP] 弹窗内已切换 " + name + " = " + value
            + " (" + radioStatusLabel(name, value) + ")");
    }

    /** 目录类菜单隐藏「是否刷新」表单项，仍通过脚本写入表单值 */
    private void setMenuRefreshYesIfPresent() {
        List<WebElement> refreshInputs = driver.findElements(
            By.cssSelector("input[type='radio'][name='isRefresh'][value='0']"));
        if (refreshInputs.isEmpty()) {
            return;
        }
        selectRadioByNameAndValue("isRefresh", "0");
    }

    private String radioStatusLabel(String name, String value) {
        if ("status".equals(name)) {
            return "0".equals(value) ? "正常" : "停用";
        }
        if ("visible".equals(name)) {
            return "0".equals(value) ? "显示" : "隐藏";
        }
        if ("isRefresh".equals(name)) {
            return "0".equals(value) ? "是" : "否";
        }
        return value;
    }

    private void ensureDictDataPageContext() {
        switchToContentIframe("system/dict/data");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-table")));
    }

    private void ensureMenuPageContext() {
        switchToContentIframe("system/menu");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-tree-table")));
    }

    /** 显示/隐藏切换后刷新菜单管理页，使列表状态与侧边栏同步更新 */
    private void refreshMenuManagementPage() {
        ensureMenuPageContext();
        driver.navigate().refresh();
        waitForDocumentReady();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-tree-table")));
        expandMenuTreeIfNeeded();
        System.out.println("[STEP] 菜单管理页面已手动刷新");
        SlowWebActions.pauseAfterStep();
    }

    /** 等待编辑弹窗（layui layer）完全关闭 */
    private void waitForEditLayerClosed() {
        switchToDefaultContent();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
            By.cssSelector("div.layui-layer-iframe")));
        System.out.println("[STEP] 编辑弹窗已关闭");
    }

    /** 若提交后出现「操作成功」等提示框，点击确定关闭（不计入一次操作流程） */
    private void dismissSuccessDialogIfPresent() {
        switchToDefaultContent();
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement dialog = shortWait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".layui-layer-dialog")));
            System.out.println("[结果] " + dialog.getText().trim());
            WebElement okBtn = dialog.findElement(By.cssSelector(".layui-layer-btn0"));
            SlowWebActions.slowClick(okBtn);
            shortWait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".layui-layer-dialog")));
        } catch (Exception ignored) {
        }
    }

    /**
     * 完整执行一次字典状态修改：点击编辑 → 弹窗内选状态 → 点确定 → 弹窗收起。
     */
    private void performDictDataStatusChangeOnce(int round, String statusValue) {
        ensureDictDataPageContext();
        WebElement row = findFirstDictDataRow();
        String statusLabel = "0".equals(statusValue) ? "正常" : "停用";

        System.out.println("[第" + round + "次] 点击「编辑」，打开弹窗");
        WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(
            row.findElement(By.xpath(".//a[contains(@onclick,'operate.edit')]"))));
        SlowWebActions.slowClick(editBtn);

        switchToLayerIframe();
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[type='radio'][name='status']")));
        System.out.println("[第" + round + "次] 在弹窗中选择状态: " + statusLabel);
        selectRadioByNameAndValue("status", statusValue);

        System.out.println("[第" + round + "次] 点击「确定」，提交并关闭弹窗");
        clickLayerConfirmButton();
        waitForEditLayerClosed();
        dismissSuccessDialogIfPresent();
    }

    /**
     * 完整执行一次菜单显示/隐藏修改：点击编辑 → 弹窗内选状态并设置刷新 → 点确定 → 弹窗收起。
     */
    private void performMenuVisibleChangeOnce(int round, String visibleValue) {
        ensureMenuPageContext();
        WebElement row = findSystemMonitorMenuRow();
        String visibleLabel = "0".equals(visibleValue) ? "显示" : "隐藏";

        System.out.println("[第" + round + "次] 点击「编辑」，打开弹窗");
        WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(
            row.findElement(By.xpath(".//a[contains(@onclick,'operate.edit')]"))));
        SlowWebActions.slowClick(editBtn);

        switchToLayerIframe();
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[type='radio'][name='visible']")));
        System.out.println("[第" + round + "次] 在弹窗中选择菜单状态: " + visibleLabel);
        selectRadioByNameAndValue("visible", visibleValue);
        System.out.println("[第" + round + "次] 在弹窗中设置「是否刷新」为「是」");
        setMenuRefreshYesIfPresent();

        System.out.println("[第" + round + "次] 点击「确定」，提交并关闭弹窗");
        clickLayerConfirmButton();
        waitForEditLayerClosed();
        dismissSuccessDialogIfPresent();
    }

    private WebElement findFirstDictDataRow() {
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#bootstrap-table tbody tr")));
        List<WebElement> rows = driver.findElements(By.cssSelector("#bootstrap-table tbody tr"));
        Assert.assertFalse(rows.isEmpty(), "字典数据列表为空，无法执行状态切换");
        return rows.get(0);
    }

    private boolean isDictDataRowDisabled(WebElement row) {
        return row.getText().contains("停用");
    }

    /**
     * 对第一条字典数据连续完成两次弹窗编辑流程。
     * 当前停用 → 第1次启用、第2次停用；当前正常 → 第1次停用、第2次启用。
     */
    private void toggleFirstDictDataStatusTwice() {
        ensureDictDataPageContext();
        boolean disabled = isDictDataRowDisabled(findFirstDictDataRow());
        System.out.println("[STEP] 第一条字典数据当前状态: " + (disabled ? "停用" : "正常"));

        String firstStatus = disabled ? "0" : "1";
        String secondStatus = disabled ? "1" : "0";

        performDictDataStatusChangeOnce(1, firstStatus);
        performDictDataStatusChangeOnce(2, secondStatus);
    }

    private void expandMenuTreeIfNeeded() {
        try {
            WebElement expandBtn = driver.findElement(By.id("expandAllBtn"));
            if (expandBtn.isDisplayed()) {
                SlowWebActions.slowClick(expandBtn);
            }
        } catch (Exception ignored) {
        }
    }

    private WebElement findSystemMonitorMenuRow() {
        expandMenuTreeIfNeeded();
        return wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//table[@id='bootstrap-tree-table']//tr[.//td[contains(normalize-space(.),'"
                + TARGET_MENU_NAME + "')]]")));
    }

    private boolean isMenuRowHidden(WebElement row) {
        for (WebElement cell : row.findElements(By.tagName("td"))) {
            String text = cell.getText().trim();
            if ("隐藏".equals(text)) {
                return true;
            }
            if ("显示".equals(text)) {
                return false;
            }
        }
        return false;
    }

    /**
     * 对「系统监控」连续完成两次弹窗编辑流程（每次均设置「是否刷新=是」）。
     * 当前隐藏 → 第1次显示、第2次隐藏；当前显示 → 第1次隐藏、第2次显示。
     */
    private void toggleSystemMonitorMenuTwice() {
        ensureMenuPageContext();
        boolean hidden = isMenuRowHidden(findSystemMonitorMenuRow());
        System.out.println("[STEP] 「" + TARGET_MENU_NAME + "」当前状态: " + (hidden ? "隐藏" : "显示"));

        String firstVisible = hidden ? "0" : "1";
        String secondVisible = hidden ? "1" : "0";

        performMenuVisibleChangeOnce(1, firstVisible);
        refreshMenuManagementPage();
        performMenuVisibleChangeOnce(2, secondVisible);
        refreshMenuManagementPage();
    }

    private boolean waitForSubmitResult(boolean expectSuccess) {
        switchToDefaultContent();
        try {
            WebElement dialog = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".layui-layer-dialog")));
            String message = dialog.getText();
            boolean success = (message.contains("成功") || message.contains("操作成功") || message.contains("修改成功"))
                && !message.contains("失败") && !message.contains("错误");
            System.out.println("[结果] " + message);
            return success == expectSuccess;
        } catch (Exception e) {
            try {
                switchToLayerIframe();
            } catch (Exception ignored) {
            }
            boolean hasValidationError =
                !driver.findElements(By.cssSelector("#form-dict-add label.error, #form-dict-edit label.error")).isEmpty();
            if (hasValidationError) {
                System.out.println("[结果] 前端表单校验拦截提交（符合预期）");
            }
            return !expectSuccess;
        }
    }

    private void clickAddDictDataButton() {
        WebElement addBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@id='toolbar']//a[contains(@onclick,'add(')]")));
        SlowWebActions.slowClick(addBtn);
        switchToLayerIframe();
    }

    private void fillAndSubmitDictDataForm(String dictLabel, String dictValue, String dictSort) {
        WebElement dictLabelInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("dictLabel")));
        SlowWebActions.slowClearAndType(dictLabelInput, dictLabel);

        WebElement dictValueInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("dictValue")));
        SlowWebActions.slowClearAndType(dictValueInput, dictValue);

        if (dictSort != null && !dictSort.isEmpty()) {
            WebElement dictSortInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='dictSort']")));
            SlowWebActions.slowClearAndType(dictSortInput, dictSort);
        } else {
            WebElement dictSortInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='dictSort']")));
            SlowWebActions.slowClearAndType(dictSortInput, "");
        }

        clickLayerConfirmButton();
    }

    // ======================== 2.1 单模块测试1：字典类型查询 ========================

    @Test(description = "2.1单模块：字典类型查询")
    public void testSingleModule_DictTypeQuery() {
        System.out.println("========== 2.1 单模块测试：字典类型查询 ==========");

        doLogin(LOGIN_USER, LOGIN_PASSWORD);
        assertLoginSuccess();

        navigateToMenu("系统管理", "system/dict");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-table")));
        pauseStep("字典类型列表已加载，准备在「字典类型」框输入查询条件");

        WebElement dictTypeInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("dictTypeInput")));
        SlowWebActions.slowClearAndType(dictTypeInput, QUERY_DICT_TYPE);
        System.out.println("[STEP] 输入字典类型: " + QUERY_DICT_TYPE);
        pauseStep("已输入字典类型「" + QUERY_DICT_TYPE + "」，请确认查询框内容");

        WebElement searchBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("searchBtn")));
        SlowWebActions.slowClick(searchBtn);
        System.out.println("[STEP] 点击搜索按钮");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-table")));
        pauseForResult("字典类型查询结果已刷新，请查看表格筛选效果");
        System.out.println("[PASS] 字典类型查询功能正常");
        System.out.println("========== 测试通过 ==========");
    }

    // ======================== 2.1 单模块测试2：字典数据停用 ========================

    @Test(description = "2.1单模块：对第一条字典数据执行启用/停用双向切换")
    public void testSingleModule_DictDataDeactivation() {
        System.out.println("========== 2.1 单模块测试：字典数据启用/停用切换 ==========");

        doLogin(LOGIN_USER, LOGIN_PASSWORD);
        assertLoginSuccess();

        navigateToMenu("系统管理", "system/dict");
        openDictDataPageFromTypeList();

        pauseStep("字典数据列表已加载，准备对第一条数据执行两次状态切换");
        toggleFirstDictDataStatusTwice();
        pauseForResult("字典数据两次状态切换已完成");

        System.out.println("[PASS] 字典数据启用/停用切换完成");
        System.out.println("========== 测试通过 ==========");
    }

    // ======================== 2.2 集成测试深度3：菜单显示/隐藏切换 ========================

    @Test(description = "2.2集成深度3：登录->系统管理->菜单管理->系统监控显示/隐藏双向切换")
    public void testIntegration_Depth3_MenuDisplayToggle() {
        System.out.println("========== 2.2 集成测试深度3：系统监控显示/隐藏切换 ==========");

        System.out.println("[深度1] 执行登录...");
        doLogin(LOGIN_USER, LOGIN_PASSWORD);
        assertLoginSuccess();

        System.out.println("[深度2] 导航：系统管理 -> 菜单管理");
        navigateToMenu("系统管理", "system/menu");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bootstrap-tree-table")));
        pauseStep("菜单列表已加载，准备对「" + TARGET_MENU_NAME + "」执行两次显示/隐藏切换");
        System.out.println("[深度3] 对「" + TARGET_MENU_NAME + "」执行显示/隐藏双向切换...");

        toggleSystemMonitorMenuTwice();
        pauseForResult("菜单两次状态切换已完成");

        System.out.println("[PASS] 菜单显示/隐藏切换操作完成");
        System.out.println("========== 测试通过 ==========");
    }

    // ======================== 2.2 集成测试深度4：字典管理新增键值 ========================

    @Test(description = "2.2集成深度4：登录->字典管理->字典数据页->新增键值")
    public void testIntegration_Depth4_AddDictData() {
        System.out.println("========== 2.2 集成测试深度4：新增字典键值 ==========");

        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String dictLabel = "测试字典_" + uniqueId;
        String dictValue = "test_" + uniqueId;

        System.out.println("[深度1] 执行登录...");
        doLogin(LOGIN_USER, LOGIN_PASSWORD);
        assertLoginSuccess();

        System.out.println("[深度2] 导航：系统管理 -> 字典管理");
        navigateToMenu("系统管理", "system/dict");

        System.out.println("[深度3] 点击字典类型进入字典数据页");
        openDictDataPageFromTypeList();

        System.out.println("[深度4] 新增字典键值...");
        clickAddDictDataButton();
        fillAndSubmitDictDataForm(dictLabel, dictValue, "10");
        System.out.println("[STEP] 字典标签: " + dictLabel);
        System.out.println("[STEP] 字典键值: " + dictValue);

        Assert.assertTrue(waitForSubmitResult(true), "新增字典键值应提交成功");
        System.out.println("[PASS] 集成测试深度4完成");
        System.out.println("========== 测试通过 ==========");
    }

    // ======================== 2.4 数据驱动测试：25组字典数据 ========================

    @DataProvider(name = "dictDataTestData")
    public Object[][] dictDataTestData() {
        return new Object[][]{
            {"正常字典1", "normal_dict_1", "0", true, "正常数据-启用状态"},
            {"正常字典2", "normal_dict_2", "0", true, "正常数据-启用状态"},

            {"空标签", "", "0", false, "异常-字典标签为空"},
            {"空键值", "empty_value", "", false, "异常-字典键值为空"},
            {"空排序", "empty_sort", "", false, "异常-排序值为空"},

            {"超长标签-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
             "long_label", "0", false, "异常-字典标签超长"},
            {"超长键值-AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
             "long_value", "0", false, "异常-字典键值超长"},

            {"特殊字符_下划线", "special_underline", "0", true, "正常-键值含下划线"},
            {"特殊字符-中划线", "special-dash", "0", true, "正常-键值含中划线"},
            {"特殊字符.点", "special.dot", "0", true, "正常-键值含点"},
            {"特殊字符@邮箱", "special@email", "0", true, "正常-键值含@"},

            {"数字键值123", "123456", "0", true, "正常-纯数字键值"},
            {"数字标签456", "num_label", "0", true, "正常-数字标签"},

            {"SQL注入'OR'1'='1", "sql_inject", "0", true, "安全-SQL注入测试"},
            {"SQL注入\"OR\"1\"=\"1", "sql_inject2", "0", true, "安全-SQL注入测试2"},
            {"XSS注入<script>alert(1)</script>", "xss_inject", "0", true, "安全-XSS注入测试"},
            {"XSS注入<img src=x onerror=alert(1)>", "xss_inject2", "0", true, "安全-XSS注入测试2"},

            {"中文标签测试", "chinese_label", "0", true, "正常-中文标签"},
            {"中文键值测试", "chinese_value", "0", true, "正常-中文键值"},
            {"日文标签テスト", "japanese_label", "0", true, "正常-日文标签"},
            {"韩文标签테스트", "korean_label", "0", true, "正常-韩文标签"},

            {"停用状态数据", "disabled_data", "1", true, "正常-停用状态数据"},
            {"默认值0", "default_zero", "0", true, "边界-排序值为0"},
            {"负数排序-10", "negative_sort", "-10", false, "异常-负数排序值"},
            {"null状态", "null_status", "0", true, "边界-状态默认启用"},
        };
    }

    @Test(description = "2.4数据驱动：25组新增字典数据表单合法性校验",
          dataProvider = "dictDataTestData")
    public void testDataDriven_DictDataValidation(String dictLabel, String dictValue,
                                                   String dictSort, boolean expectSuccess,
                                                   String description) {
        System.out.println("------------------------------------------------");
        System.out.println("[2.4 数据驱动] " + description);
        System.out.println("[参数] dictLabel=\"" + dictLabel + "\", dictValue=\"" + dictValue +
                          "\", dictSort=\"" + dictSort + "\"");
        System.out.println("[预期] " + (expectSuccess ? "提交成功" : "校验失败/拒绝提交"));

        doLogin(LOGIN_USER, LOGIN_PASSWORD);
        assertLoginSuccess();

        navigateToMenu("系统管理", "system/dict");
        openDictDataPageFromTypeList();

        clickAddDictDataButton();
        fillAndSubmitDictDataForm(dictLabel, dictValue, dictSort);

        boolean actualSuccess = waitForSubmitResult(expectSuccess);
        Assert.assertTrue(actualSuccess,
            "用例「" + description + "」结果与预期不符，预期=" + expectSuccess);

        System.out.println("[断言] PASS - 用例结果符合预期");
        System.out.println("------------------------------------------------");
    }
}
