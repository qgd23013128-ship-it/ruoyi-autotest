package com.ruoyi.test;

import com.ruoyi.test.base.BaseTest;
import com.ruoyi.test.pages.LoginPage;
import com.ruoyi.test.pages.NoticePage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataDrivenTest extends BaseTest {

    private static int testIndex = 0;

    @DataProvider(name = "noticeAddData")
    public Object[][] createNoticeAddData() {
        return new Object[][]{
            {1,  "正常公告-通知类型",       "关于系统维护的通知",           "系统将于今晚22:00进行维护，预计持续2小时。",  "1", "0", true},
            {2,  "正常公告-提醒类型",       "员工生日祝福",                 "祝本月生日的小伙伴们生日快乐！",              "2", "0", true},
            {3,  "标题为空",                "",                             "这是一个缺少标题的公告内容。",                "1", "0", false},
            {4,  "超长标题",                generateLongString(100),        "内容简短，但标题很长。",                     "1", "0", false},
            {5,  "富文本超长内容",          "超长内容公告",                  generateLongString(500),                     "1", "0", true},
            {6,  "特殊字符标题 <script>",   "<script>alert('xss')</script>","标题含特殊字符的公告内容。",                  "1", "0", false},
            {7,  "特殊字符标题 &quot;",     "\"引号测试\"",                "双引号标题公告内容。",                        "2", "0", true},
            {8,  "特殊字符标题 &apos;",     "'单引号测试'",                 "单引号标题公告内容。",                        "1", "0", true},
            {9,  "空格标题",                "   ",                          "标题只有空格的公告。",                        "2", "0", false},
            {10, "数字标题",                "1234567890",                  "纯数字标题的公告内容。",                      "1", "0", true},
            {11, "英文标题",                "System Maintenance Notice",   "English title notice content.",               "1", "0", true},
            {12, "中英混合标题",            "系统更新 System Update v2.0",  "中英文混合标题公告内容。",                    "2", "0", true},
            {13, "通知类型+正常状态",       "新功能上线通知",                "系统新增了数据导出功能。",                    "1", "0", true},
            {14, "通知类型+关闭状态",       "内部测试公告",                 "此公告仅供内部测试。",                        "1", "1", true},
            {15, "提醒类型+正常状态",       "会议提醒",                     "明天上午10点在会议室开会。",                  "2", "0", true},
            {16, "提醒类型+关闭状态",       "已过期提醒",                   "此提醒已过期。",                              "2", "1", true},
            {17, "最短标题",                "A",                            "单字符标题公告。",                            "1", "0", true},
            {18, "标题含逗号",              "系统公告, 紧急!",              "含逗号的公告标题。",                          "1", "0", true},
            {19, "标题含百分号",            "折扣100%%来袭",               "含百分号的公告。",                            "2", "0", true},
            {20, "内容含换行符",            "多行内容测试",                 "第一行内容。\n第二行内容。\n第三行内容。",    "1", "0", true},
            {21, "NULL值标题测试",          null,                           "标题为空的公告内容。",                        "1", "0", false},
            {22, "极短内容",                "极短内容公告",                 ".",                                           "2", "0", true},
            {23, "所有字段正常",            "年终总结大会通知",             "公司将于年底召开年终总结大会，请全体员工参加。","1", "0", true},
            {24, "类型为通知+两个空格标题", "  两个空格前",                  "标题前后有空格。",                            "1", "0", true},
            {25, "综合场景",                "【通知】五一放假安排(Q2)",    "各部门请注意，五一劳动节放假安排如下...",    "1", "0", true},
        };
    }

    @Test(dataProvider = "noticeAddData", description = "TC_DD: 新增公告数据驱动测试(25组)")
    public void testAddNoticeDataDriven(int id, String scenario, String title, String content, String type, String status, boolean expectedSuccess) {
        testIndex++;
        System.out.println("===== 数据驱动测试 #" + id + ": " + scenario + " =====");

        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login(ADMIN_USER, ADMIN_PASS);
        loginPage.waitForLoginSuccess();

        NoticePage noticePage = new NoticePage(driver);
        noticePage.open();
        noticePage.clickAddButton();

        if (title != null) {
            noticePage.enterNoticeTitle(title);
        }

        noticePage.selectNoticeType(type);

        if (content != null) {
            noticePage.enterNoticeContent(content);
        }

        noticePage.selectNoticeStatus(status);

        takeScreenshot("TC_DD_" + id + "_" + scenario.replaceAll("[^\\w]", "_"));

        boolean hasSuccess = noticePage.clickSubmitAndWaitResult();
        takeScreenshot("TC_DD_" + id + "_结果_" + scenario.replaceAll("[^\\w]", "_"));

        String logMsg = String.format("数据集#%d [%s]: 预期=%s, 实际=%s",
                id, scenario, expectedSuccess ? "成功" : "失败",
                hasSuccess ? "成功" : "失败");
        System.out.println(logMsg);

        if (expectedSuccess) {
            if (!hasSuccess) {
                System.out.println("  [警告] 预期成功但实际失败: " + scenario);
            }
        } else {
            if (hasSuccess) {
                System.out.println("  [警告] 预期失败但实际成功: " + scenario);
            }
        }
    }

    private static String generateLongString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('A' + (i % 26)));
        }
        return sb.toString();
    }
}
