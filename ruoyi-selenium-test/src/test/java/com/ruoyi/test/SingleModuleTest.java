package com.ruoyi.test;

import com.ruoyi.test.base.BaseTest;
import com.ruoyi.test.pages.ConfigPage;
import com.ruoyi.test.pages.LoginPage;
import com.ruoyi.test.pages.NoticePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SingleModuleTest extends BaseTest {

    @Test(description = "TC_SM_001: 参数查询 - 按参数名称搜索已有参数")
    public void testConfigQueryByName() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login(ADMIN_USER, ADMIN_PASS);
        loginPage.waitForLoginSuccess();

        ConfigPage configPage = new ConfigPage(driver);
        configPage.open();
        Assert.assertTrue(configPage.isConfigListDisplayed(), "参数列表页面未加载成功");

        configPage.searchByParamName("用户管理");
        Assert.assertTrue(configPage.hasDataInTable(), "按参数名称搜索未能找到数据");

        takeScreenshot("TC_SM_001_参数查询_按名称搜索");
    }

    @Test(description = "TC_SM_002: 参数查询 - 按参数键名搜索")
    public void testConfigQueryByKey() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login(ADMIN_USER, ADMIN_PASS);
        loginPage.waitForLoginSuccess();

        ConfigPage configPage = new ConfigPage(driver);
        configPage.open();
        Assert.assertTrue(configPage.isConfigListDisplayed(), "参数列表页面未加载成功");

        configPage.searchByParamKey("sys.user.initPassword");
        Assert.assertTrue(configPage.hasDataInTable(), "按参数键名搜索未能找到数据");

        takeScreenshot("TC_SM_002_参数查询_按键名搜索");
    }

    @Test(description = "TC_SM_003: 公告查询 - 查询通知公告列表")
    public void testNoticeQueryList() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login(ADMIN_USER, ADMIN_PASS);
        loginPage.waitForLoginSuccess();

        NoticePage noticePage = new NoticePage(driver);
        noticePage.open();
        Assert.assertTrue(noticePage.isNoticeListDisplayed(), "通知公告列表页面未加载成功");

        takeScreenshot("TC_SM_003_公告查询_列表展示");
    }

    @Test(description = "TC_SM_004: 公告查询 - 按标题搜索公告")
    public void testNoticeQueryByTitle() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login(ADMIN_USER, ADMIN_PASS);
        loginPage.waitForLoginSuccess();

        NoticePage noticePage = new NoticePage(driver);
        noticePage.open();
        Assert.assertTrue(noticePage.isNoticeListDisplayed(), "通知公告列表页面未加载成功");

        noticePage.searchByTitle("测试");
        takeScreenshot("TC_SM_004_公告查询_按标题搜索");
    }
}
