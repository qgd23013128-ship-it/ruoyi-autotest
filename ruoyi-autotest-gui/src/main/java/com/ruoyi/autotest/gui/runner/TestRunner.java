package com.ruoyi.autotest.gui.runner;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JOptionPane;

import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Dynamic TestNG runner used by the Swing GUI.
 */
public class TestRunner {

    private static final String DEPT_POST_TEST_CLASS = "com.ruoyi.autotest.test.DeptPostTest";

    private String baseUrl = "http://localhost";

    public static final String[] ALL_TEST_METHODS = {
        "testSingleModule_Login",
        "testSingleModule_UserQuery",
        "testIntegration_Depth3_AddRole",
        "testIntegration_Depth4_AddUserAndAssign",
        "testDataDriven_Login"
    };

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        System.setProperty("ruoyi.base.url", baseUrl);
    }

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
                System.out.println("[INFO] Found EdgeDriver: " + absolutePath);
                System.setProperty("webdriver.edge.driver", absolutePath);
                return true;
            }
        }

        JOptionPane.showMessageDialog(
            null,
            "缺少 Edge 驱动文件：msedgedriver.exe\n\n"
                + "请将 msedgedriver.exe 放到项目根目录、src/main/resources 或 target 目录。",
            "驱动缺失",
            JOptionPane.ERROR_MESSAGE
        );
        return false;
    }

    public void runFilteredMethods(String className, List<String> methodNames) {
        runFilteredMethods(className, methodNames, null);
    }

    public void runFilteredMethods(String className, List<String> methodNames, Consumer<String> logConsumer) {
        Consumer<String> logger = logger(logConsumer);
        if (!prepareRun(className, logger)) {
            return;
        }

        if (methodNames == null || methodNames.isEmpty()) {
            runAllMethods(className, logger);
            return;
        }

        logger.accept("开始执行 TestNG 指定方法");
        logger.accept("实际执行测试类: " + className);
        logger.accept("实际执行测试方法: " + String.join(", ", methodNames));

        TestNG testng = createTestNG(logger, 1);
        XmlSuite suite = new XmlSuite();
        suite.setName("RuoYiAutoTest-GUI-Filtered");
        suite.setParallel(XmlSuite.ParallelMode.NONE);

        XmlTest test = new XmlTest(suite);
        test.setName("GUI-Filtered-Test");

        XmlClass xmlClass = new XmlClass(className);
        List<XmlInclude> includedMethods = new ArrayList<>();
        for (String name : methodNames) {
            if (name != null && !name.trim().isEmpty()) {
                includedMethods.add(new XmlInclude(name.trim()));
            }
        }
        xmlClass.setIncludedMethods(includedMethods);
        test.setXmlClasses(Collections.singletonList(xmlClass));
        testng.setXmlSuites(Collections.singletonList(suite));

        runTestNG(testng, logger);
    }

    public void runSingleMethod(String className, String methodName) {
        runFilteredMethods(className, Collections.singletonList(methodName), null);
    }

    public void runAllMethods(String className) {
        runAllMethods(className, null);
    }

    public void runAllMethods(String className, Consumer<String> logConsumer) {
        Consumer<String> logger = logger(logConsumer);
        if (!prepareRun(className, logger)) {
            return;
        }

        logger.accept("开始执行 TestNG 全部方法");
        logger.accept("实际执行测试类: " + className);
        logger.accept("实际执行测试方法: 全部 @Test 方法");

        TestNG testng = createTestNG(logger, 2);
        try {
            testng.setTestClasses(new Class<?>[]{loadClass(className)});
        } catch (RuntimeException e) {
            logger.accept("FAILED: " + e.getMessage());
            logger.accept(stackTrace(e));
            return;
        }
        runTestNG(testng, logger);
    }

    private boolean prepareRun(String className, Consumer<String> logger) {
        if (requiresEdgeDriver(className) && !checkEdgeDriver()) {
            logger.accept("FAILED: EdgeDriver 未就绪，已取消执行");
            return false;
        }
        System.setProperty("ruoyi.base.url", baseUrl);
        logger.accept("Base URL: " + baseUrl);
        return true;
    }

    private TestNG createTestNG(Consumer<String> logger, int verbose) {
        TestNG testng = new TestNG();
        testng.setVerbose(verbose);
        testng.setOutputDirectory("target/testng-gui-results");
        testng.addListener(new GuiTestListener(logger));
        return testng;
    }

    private void runTestNG(TestNG testng, Consumer<String> logger) {
        try {
            testng.run();
            logger.accept("TestNG 执行结束");
        } catch (Exception e) {
            logger.accept("FAILED: TestNG 执行异常 - " + e.getMessage());
            logger.accept(stackTrace(e));
        }
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法加载测试类: " + className, e);
        }
    }

    private boolean requiresEdgeDriver(String className) {
        return !DEPT_POST_TEST_CLASS.equals(className);
    }

    private Consumer<String> logger(Consumer<String> logConsumer) {
        return logConsumer == null ? System.out::println : logConsumer;
    }

    private static String stackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private static final class GuiTestListener implements ITestListener, IConfigurationListener {
        private final Consumer<String> logger;

        private GuiTestListener(Consumer<String> logger) {
            this.logger = logger;
        }

        @Override
        public void onTestStart(ITestResult result) {
            logger.accept("STARTED: " + resultName(result));
        }

        @Override
        public void onTestSuccess(ITestResult result) {
            logger.accept("PASSED: " + resultName(result));
        }

        @Override
        public void onTestFailure(ITestResult result) {
            logger.accept("FAILED: " + resultName(result));
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                logger.accept("异常信息: " + throwable);
                logger.accept(stackTrace(throwable));
            }
        }

        @Override
        public void onTestSkipped(ITestResult result) {
            logger.accept("SKIPPED: " + resultName(result));
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                logger.accept("跳过原因: " + throwable);
            }
        }

        @Override
        public void onConfigurationFailure(ITestResult result) {
            logger.accept("FAILED: 配置方法失败 - " + resultName(result));
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                logger.accept("异常信息: " + throwable);
                logger.accept(stackTrace(throwable));
            }
        }

        @Override
        public void onConfigurationSkip(ITestResult result) {
            logger.accept("SKIPPED: 配置方法跳过 - " + resultName(result));
        }

        @Override
        public void onFinish(ITestContext context) {
            logger.accept("结果汇总: PASSED=" + context.getPassedTests().size()
                + ", FAILED=" + context.getFailedTests().size()
                + ", SKIPPED=" + context.getSkippedTests().size());
        }

        private String resultName(ITestResult result) {
            return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        }
    }
}
