package com.ruoyi.autotest.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * 参数与通知公告模块测试面板
 */
public class ParamNoticePanel extends JPanel {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String TEST_CLASS = "com.ruoyi.autotest.test.ParamNoticeTest";

    private JTextArea logArea;
    private JTextField baseUrlField;
    private String baseUrl = "http://localhost";
    /** 防止同时运行多个测试 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ParamNoticePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        add(createTopInfoPanel(), BorderLayout.NORTH);
        add(createButtonPanel(), BorderLayout.CENTER);
        add(createLogPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel titleLabel = new JLabel("参数与通知公告模块 - 自动化测试（组员4）");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));

        JLabel infoLabel = new JLabel("<html>"
            + "负责模块：参数配置 与 通知公告管理<br>"
            + "单模块测试：参数查询(按名称/键名)、公告查询(列表/标题)<br>"
            + "集成测试：深度3-新增公告、深度4-修改参数<br>"
            + "数据驱动：25组新增公告数据组合测试<br>"
            + "性能测试：130并发通知公告列表接口压力测试"
            + "</html>");
        infoLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        infoLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(infoLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")),
            " 测试操作 "));
        panel.setPreferredSize(new Dimension(0, 310));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        JButton btnSingle = createActionButton("单模块测试 (4用例)",
            "参数查询(名称+键名) + 公告查询(列表+标题)", UIManager.getColor("Actions.Green"));
        btnSingle.addActionListener(e -> runSingleModuleTest());
        panel.add(btnSingle, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        JButton btnDepth3 = createActionButton("集成深度3: 新增公告",
            "登录 → 系统管理 → 通知公告 → 新增并保存", new Color(70, 130, 180));
        btnDepth3.addActionListener(e -> runDepth3Test());
        panel.add(btnDepth3, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        JButton btnDepth4 = createActionButton("集成深度4: 修改参数",
            "登录 → 系统管理 → 参数设置 → 搜索 → 修改保存", new Color(138, 43, 226));
        btnDepth4.addActionListener(e -> runDepth4Test());
        panel.add(btnDepth4, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JButton btnDataDriven = createActionButton("数据驱动: 25组公告数据",
            "通知/提醒/空标题/超长/特殊字符等25组", new Color(220, 130, 50));
        btnDataDriven.addActionListener(e -> runDataDrivenTest());
        panel.add(btnDataDriven, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        JButton btnPerf = createActionButton("性能测试: 130并发压测",
            "Java HttpClient 130线程并发压力测试", new Color(180, 50, 50));
        btnPerf.addActionListener(e -> runPerformanceTest());
        panel.add(btnPerf, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        JButton btnAll = createActionButton("运行全部测试",
            "运行 ParamNoticeTest 全部31个测试方法", new Color(60, 60, 60));
        btnAll.addActionListener(e -> runAllTests());
        panel.add(btnAll, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 3;
        panel.add(createConfigRow(), gbc);

        return panel;
    }

    private JPanel createConfigRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));

        row.add(new JLabel("系统地址:"));
        baseUrlField = new JTextField(baseUrl, 25);
        baseUrlField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        row.add(baseUrlField);

        JButton saveUrlBtn = new JButton("保存地址");
        saveUrlBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        saveUrlBtn.addActionListener(e -> {
            baseUrl = baseUrlField.getText().trim();
            System.setProperty("ruoyi.base.url", baseUrl);
            appendLog("[配置] 系统地址: " + baseUrl);
        });
        row.add(saveUrlBtn);

        row.add(Box.createHorizontalStrut(16));

        JCheckBox keepBrowserCheck = new JCheckBox("测试后保持浏览器打开");
        keepBrowserCheck.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        keepBrowserCheck.addActionListener(e ->
            System.setProperty("ruoyi.keep.browser.open",
                String.valueOf(keepBrowserCheck.isSelected())));
        row.add(keepBrowserCheck);

        JCheckBox headlessCheck = new JCheckBox("无头模式(不显示浏览器)");
        headlessCheck.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        headlessCheck.addActionListener(e ->
            System.setProperty("ruoyi.chrome.headless",
                String.valueOf(headlessCheck.isSelected())));
        row.add(headlessCheck);

        row.add(Box.createHorizontalStrut(12));

        JButton clearLogBtn = new JButton("清空日志");
        clearLogBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        row.add(clearLogBtn);

        return row;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")),
            " 运行日志 "));
        panel.setPreferredSize(new Dimension(0, 250));

        logArea = new JTextArea();
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setEditable(false);
        logArea.setBackground(UIManager.getColor("TextArea.background"));
        logArea.setForeground(UIManager.getColor("TextArea.foreground"));

        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JButton createActionButton(String text, String tooltip, Color color) {
        JButton btn = new JButton("<html><center>" + text.replace("\n", "<br>") + "</center></html>");
        btn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        btn.setToolTipText(tooltip);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 60));
        return btn;
    }

    // ======================== 测试执行（带并发锁） ========================

    private boolean tryStartTest(String name) {
        if (!running.compareAndSet(false, true)) {
            appendLog("[提示] 正在执行测试中，请等待当前测试完成后再试。");
            return false;
        }
        appendLog("=== 开始：" + name + " ===");
        return true;
    }

    private void endTest(String name) {
        appendLog("=== " + name + " 完成 ===");
        running.set(false);
    }

    private void runSingleModuleTest() {
        saveBaseUrl();
        if (!tryStartTest("单模块测试 (4用例)")) return;
        new Thread(() -> {
            try {
                runTestNGMethods(new String[]{
                    "testSingleModule_ConfigQueryByName",
                    "testSingleModule_ConfigQueryByKey",
                    "testSingleModule_NoticeQueryList",
                    "testSingleModule_NoticeQueryByTitle"
                });
            } finally {
                endTest("单模块测试");
            }
        }).start();
    }

    private void runDepth3Test() {
        saveBaseUrl();
        if (!tryStartTest("集成深度3 - 新增公告")) return;
        new Thread(() -> {
            try {
                runTestNGMethods(new String[]{"testIntegration_AddNoticeDepth3"});
            } finally {
                endTest("集成深度3");
            }
        }).start();
    }

    private void runDepth4Test() {
        saveBaseUrl();
        if (!tryStartTest("集成深度4 - 修改参数")) return;
        new Thread(() -> {
            try {
                runTestNGMethods(new String[]{"testIntegration_ModifyConfigDepth4"});
            } finally {
                endTest("集成深度4");
            }
        }).start();
    }

    private void runDataDrivenTest() {
        saveBaseUrl();
        if (!tryStartTest("数据驱动测试 (25组)")) return;
        new Thread(() -> {
            try {
                runTestNGMethods(new String[]{"testDataDriven_AddNotice25Cases"});
            } finally {
                endTest("数据驱动测试");
            }
        }).start();
    }

    private void runAllTests() {
        saveBaseUrl();
        if (!tryStartTest("全部测试 (31用例)")) return;
        new Thread(() -> {
            try {
                runTestNGMethods(new String[]{
                    "testSingleModule_ConfigQueryByName",
                    "testSingleModule_ConfigQueryByKey",
                    "testSingleModule_NoticeQueryList",
                    "testSingleModule_NoticeQueryByTitle",
                    "testIntegration_AddNoticeDepth3",
                    "testIntegration_ModifyConfigDepth4",
                    "testDataDriven_AddNotice25Cases"
                });
            } finally {
                endTest("全部测试");
            }
        }).start();
    }

    private void runPerformanceTest() {
        saveBaseUrl();
        if (!tryStartTest("性能测试 (130并发)")) return;
        new Thread(() -> {
            try {
                runConcurrentPerformanceTest();
            } catch (Exception e) {
                appendLog("[错误] 性能测试异常: " + e.getMessage());
            } finally {
                endTest("性能测试");
            }
        }).start();
    }

    private void runTestNGMethods(String[] methodNames) {
        try {
            org.testng.TestNG testNG = new org.testng.TestNG();
            testNG.setUseDefaultListeners(false);

            org.testng.xml.XmlSuite suite = new org.testng.xml.XmlSuite();
            suite.setName("参数与公告测试套件");
            suite.setVerbose(1);

            org.testng.xml.XmlTest test = new org.testng.xml.XmlTest(suite);
            test.setName("参数与公告测试");

            org.testng.xml.XmlClass xmlClass = new org.testng.xml.XmlClass(TEST_CLASS);
            List<org.testng.xml.XmlInclude> includes = new ArrayList<>();
            for (String method : methodNames) {
                includes.add(new org.testng.xml.XmlInclude(method));
            }
            xmlClass.setIncludedMethods(includes);
            test.setXmlClasses(Collections.singletonList(xmlClass));

            testNG.setXmlSuites(Collections.singletonList(suite));

            testNG.addListener(new org.testng.ITestListener() {
                public void onTestSuccess(org.testng.ITestResult result) {
                    javax.swing.SwingUtilities.invokeLater(() ->
                        appendLog("[通过] " + result.getMethod().getMethodName()));
                }
                public void onTestFailure(org.testng.ITestResult result) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        Throwable t = result.getThrowable();
                        appendLog("[失败] " + result.getMethod().getMethodName()
                            + " - " + (t != null ? t.getMessage() : ""));
                    });
                }
                public void onTestSkipped(org.testng.ITestResult result) {
                    javax.swing.SwingUtilities.invokeLater(() ->
                        appendLog("[跳过] " + result.getMethod().getMethodName()));
                }
                public void onConfigurationFailure(org.testng.ITestResult result) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        appendLog("[配置失败] " + result.getMethod().getMethodName());
                        Throwable t = result.getThrowable();
                        if (t != null) {
                            appendLog("  原因: " + t.toString());
                            t.printStackTrace();
                        }
                    });
                }
            });

            testNG.run();
        } catch (Exception e) {
            appendLog("[错误] TestNG运行异常: " + e.getMessage());
        }
    }

    // ======================== 性能测试 ========================

    private void runConcurrentPerformanceTest() {
        String url = baseUrlField.getText().trim();
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        final String base = url;
        final int CONCURRENT = 130;
        final int RAMP_UP_SECONDS = 20;

        appendLog("目标接口: " + base + "/system/notice/list");
        appendLog("并发数: " + CONCURRENT + "  |  预热期: " + RAMP_UP_SECONDS + "秒");

        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failure = new AtomicInteger(0);
        AtomicLong totalBytes = new AtomicLong(0);

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT);

        long startMs = System.currentTimeMillis();

        for (int i = 0; i < CONCURRENT; i++) {
            final int uid = i + 1;
            executor.submit(() -> {
                try {
                    long delay = (long)(((double)uid / CONCURRENT) * RAMP_UP_SECONDS * 1000);
                    Thread.sleep(delay);
                    startLatch.await();

                    CookieManager cm = new CookieManager();
                    cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                    HttpClient client = HttpClient.newBuilder()
                        .cookieHandler(cm)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                    String enc = StandardCharsets.UTF_8.name();
                    String body = "username=" + URLEncoder.encode("admin", enc)
                        + "&password=" + URLEncoder.encode("admin123", enc)
                        + "&rememberMe=false";

                    HttpRequest loginReq = HttpRequest.newBuilder()
                        .uri(URI.create(base + "/login"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                    HttpResponse<String> loginResp = client.send(loginReq,
                        HttpResponse.BodyHandlers.ofString());

                    if (loginResp.body().contains("\"code\":500")) {
                        failure.incrementAndGet();
                        return;
                    }

                    String listParams = "pageSize=10&pageNum=1&orderByColumn=createTime&isAsc=desc";
                    Instant reqStart = Instant.now();

                    HttpRequest listReq = HttpRequest.newBuilder()
                        .uri(URI.create(base + "/system/notice/list"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .POST(HttpRequest.BodyPublishers.ofString(listParams))
                        .build();
                    HttpResponse<String> listResp = client.send(listReq,
                        HttpResponse.BodyHandlers.ofString());

                    long elapsed = Duration.between(reqStart, Instant.now()).toMillis();
                    responseTimes.add(elapsed);
                    totalBytes.addAndGet(listResp.body().length());

                    if (listResp.statusCode() == 200
                        && listResp.body().contains("\"code\":0")) {
                        success.incrementAndGet();
                    } else {
                        failure.incrementAndGet();
                    }
                } catch (Exception e) {
                    failure.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endMs = System.currentTimeMillis();
        executor.shutdown();

        List<Long> sorted = new ArrayList<>(responseTimes);
        Collections.sort(sorted);

        int total = success.get() + failure.get();
        long avg = sorted.isEmpty() ? 0
            : (long) sorted.stream().mapToLong(Long::longValue).average().orElse(0);
        double tps = (endMs - startMs) > 0
            ? (total * 1000.0 / (endMs - startMs)) : 0;
        double errRate = total > 0 ? (failure.get() * 100.0 / total) : 0;

        appendLog("========================================");
        appendLog("  性能测试结果");
        appendLog("========================================");
        appendLog("  总请求: " + total + "  成功: " + success.get()
            + "  失败: " + failure.get()
            + "  错误率: " + String.format("%.2f%%", errRate));
        appendLog("  总耗时: " + (endMs - startMs) + " ms"
            + "  吞吐量: " + String.format("%.2f", tps) + " req/s");
        appendLog("  传输量: " + String.format("%.2f", totalBytes.get() / 1024.0) + " KB");

        if (!sorted.isEmpty()) {
            int size = sorted.size();
            long p90 = sorted.get((int)(size * 0.9));
            long p95 = sorted.get((int)(size * 0.95));
            long p99 = sorted.get((int)(size * 0.99));
            appendLog("  平均: " + avg + " ms"
                + "  最小: " + sorted.get(0) + " ms"
                + "  最大: " + sorted.get(size - 1) + " ms");
            appendLog("  中位数: " + sorted.get(size / 2) + " ms"
                + "  P90: " + p90 + " ms"
                + "  P95: " + p95 + " ms"
                + "  P99: " + p99 + " ms");
        }
        appendLog("========================================");
    }

    private void saveBaseUrl() {
        baseUrl = baseUrlField.getText().trim();
        System.setProperty("ruoyi.base.url", baseUrl);
    }

    private void appendLog(String msg) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            String timestamp = LocalDateTime.now().format(DT_FORMAT);
            logArea.append("[" + timestamp + "] " + msg + "\n");
        });
    }
}
