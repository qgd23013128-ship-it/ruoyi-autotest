package com.ruoyi.autotest.gui.runner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * TestNG 动态测试运行器
 * 负责在 GUI 中动态创建 TestNG 实例并执行指定的测试类或测试方法
 */
public class TestRunner {

    /** 若依系统的默认访问地址（可根据实际环境修改） */
    private String baseUrl = "http://localhost";

    /** 全部可用的测试方法名 */
    public static final String[] ALL_TEST_METHODS = {
        "testSingleModule_Login",
        "testSingleModule_UserQuery",
        "testIntegration_Depth3_AddRole",
        "testIntegration_Depth4_AddUserAndAssign",
        "testDataDriven_Login"
    };

    /** 字典模块全部可用的测试方法名 */
    public static final String[] DICT_TEST_METHODS = {
        "testSingleModule_DictTypeQuery",
        "testSingleModule_DictDataDeactivation",
        "testIntegration_Depth3_MenuDisplayToggle",
        "testIntegration_Depth4_AddDictData",
        "testDataDriven_DictDataValidation"
    };

    /** 字典模块测试项说明（对应 2.1 ~ 2.4） */
    public static final String[] DICT_TEST_DESCRIPTIONS = {
        "2.1 单模块 - 字典类型查询（按字典类型字段）",
        "2.1 单模块 - 字典数据停用（编辑已启用数据）",
        "2.2 集成深度3 - 菜单隐藏（编辑已显示菜单）",
        "2.2 集成深度4 - 字典管理新增键值",
        "2.4 数据驱动 - 25组新增字典数据校验"
    };

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        System.setProperty("ruoyi.base.url", baseUrl);
    }

    /**
     * 在运行测试前检查 Edge 驱动文件是否存在（多路径搜索）
     * 找到后自动设置系统属性为绝对路径，供 EdgeDriver 使用
     * @return true 表示驱动就绪，false 表示驱动缺失
     */
    public boolean checkEdgeDriver() {
        String[] candidatePaths = {
            "msedgedriver.exe",
            "src/main/resources/msedgedriver.exe",
            "target/msedgedriver.exe"
        };

        for (String path : candidatePaths) {
            File driverFile = new File(path);
            if (driverFile.exists()) {
                String absolutePath = driverFile.getAbsolutePath();
                System.out.println("[INFO] 找到 EdgeDriver: " + absolutePath);
                System.setProperty("webdriver.edge.driver", absolutePath);
                return true;
            }
        }

        JOptionPane.showMessageDialog(
            null,
            "缺少 Edge 驱动文件！\n\n"
                + "请将 msedgedriver.exe 下载并放入以下目录之一：\n"
                + "  1. 项目根目录（与 pom.xml 同级）\n"
                + "  2. src/main/resources 目录下\n"
                + "  3. target 目录下\n\n"
                + "下载地址：https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/",
            "驱动缺失",
            JOptionPane.ERROR_MESSAGE
        );
        return false;
    }

    /**
     * 根据用户输入的测试用例编号或方法名，动态过滤并运行测试
     *
     * @param className   测试类的全限定名
     * @param methodNames 用户指定要运行的方法名列表（为 null 或空列表则运行全部）
     */
    public void runFilteredMethods(String className, List<String> methodNames) {
        if (!checkEdgeDriver()) {
            return;
        }

        System.setProperty("ruoyi.base.url", baseUrl);

        if (methodNames == null || methodNames.isEmpty()) {
            runAllMethods(className);
            return;
        }

        TestNG testng = new TestNG();
        testng.setVerbose(1);

        XmlSuite suite = new XmlSuite();
        suite.setName("RuoYiAutoTest - Filtered");
        suite.setParallel(XmlSuite.ParallelMode.NONE);

        XmlTest test = new XmlTest(suite);
        test.setName("FilteredTest");

        XmlClass xmlClass = new XmlClass(className);
        List<XmlInclude> includedMethods = new ArrayList<>();
        for (String name : methodNames) {
            includedMethods.add(new XmlInclude(name.trim()));
        }
        xmlClass.setIncludedMethods(includedMethods);

        List<XmlClass> classes = new ArrayList<>();
        classes.add(xmlClass);
        test.setXmlClasses(classes);

        testng.setXmlSuites(Collections.singletonList(suite));

        try {
            testng.run();
        } catch (Exception e) {
            System.err.println("[TestRunner] 测试执行异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 运行测试类中的单个指定方法
     */
    public void runSingleMethod(String className, String methodName) {
        if (!checkEdgeDriver()) {
            return;
        }
        runFilteredMethods(className, Collections.singletonList(methodName));
    }

    /**
     * 运行测试类中的所有方法
     */
    public void runAllMethods(String className) {
        System.setProperty("ruoyi.base.url", baseUrl);

        TestNG testng = new TestNG();
        testng.setTestClasses(new Class<?>[]{loadClass(className)});
        testng.setVerbose(2);

        try {
            testng.run();
        } catch (Exception e) {
            System.err.println("[TestRunner] 测试执行异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法加载测试类: " + className, e);
        }
    }
}
