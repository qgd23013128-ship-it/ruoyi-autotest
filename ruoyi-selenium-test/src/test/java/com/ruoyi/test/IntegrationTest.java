package com.ruoyi.test;

import com.ruoyi.test.base.BaseTest;
import com.ruoyi.test.pages.ConfigPage;
import com.ruoyi.test.pages.LoginPage;
import com.ruoyi.test.pages.NoticePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntegrationTest extends BaseTest {

    private static final String TEST_NOTICE_TITLE = "集成测试公告_" + System.currentTimeMillis();
    private static final String TEST_NOTICE_CONTENT = "这是一条由集成测试自动生成的公告内容。";
    private static final String TEST_CONFIG_NEW_VALUE = "true";

    @Test(description = "TC_INT_001: 深度3 - 登录→系统管理→通知公告→点击新增公告并保存")
    public void testAddNoticeDeep3() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login(ADMIN_USER, ADMIN_PASS);
        loginPage.waitForLoginSuccess();

        NoticePage noticePage = new NoticePage(driver);
        noticePage.open();
        Assert.assertTrue(noticePage.isNoticeListDisplayed(), "通知公告列表页面未加载成功");

        takeScreenshot("TC_INT_001_通知公告列表页");

        noticePage.clickAddButton();
        noticePage.enterNoticeTitle(TEST_NOTICE_TITLE);
        noticePage.selectNoticeType("1");
        noticePage.enterNoticeContent(TEST_NOTICE_CONTENT);
        noticePage.selectNoticeStatus("0");

        takeScreenshot("TC_INT_001_填写新增公告信息");

        boolean hasSuccess = noticePage.clickSubmitAndWaitResult();
        takeScreenshot("TC_INT_001_新增公告结果");

        Assert.assertTrue(hasSuccess, "新增公告后未收到成功提示");
    }

    @Test(description = "TC_INT_002: 深度4 - 登录→系统管理→参数设置→搜索参数→修改参数值并保存")
    public void testModifyConfigDeep4() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login(ADMIN_USER, ADMIN_PASS);
        loginPage.waitForLoginSuccess();

        ConfigPage configPage = new ConfigPage(driver);
        configPage.open();
        Assert.assertTrue(configPage.isConfigListDisplayed(), "参数设置列表页面未加载成功");

        takeScreenshot("TC_INT_002_参数设置列表页");

        configPage.searchByParamKey("sys.account.registerUser");

        Assert.assertTrue(configPage.hasDataInTable(), "搜索参数未能找到数据");

        takeScreenshot("TC_INT_002_搜索到目标参数");

        configPage.selectFirstRow();
        configPage.clickEditButton();

        configPage.modifyConfigValue(TEST_CONFIG_NEW_VALUE);

        takeScreenshot("TC_INT_002_修改参数值");

        boolean hasSuccess = configPage.clickSubmitAndWaitResult();
        takeScreenshot("TC_INT_002_修改参数结果");

        Assert.assertTrue(hasSuccess, "保存修改后未收到成功提示");
    }
}
