package com.ruoyi.autotest.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

import com.ruoyi.autotest.gui.runner.TestRunner;

/**
 * 系统监控与日志模块测试面板
 */
public class MonitorLogPanel extends JPanel {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private JTextArea logArea;
    private final TestRunner testRunner;

    private static final String TEST_CLASS = "com.ruoyi.autotest.test.MonitorLogTest";

    public MonitorLogPanel() {
        testRunner = new TestRunner();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        add(createTopInfoPanel(), BorderLayout.NORTH);
        add(createButtonPanel(), BorderLayout.CENTER);
        add(createLogPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel titleLabel = new JLabel("系统监控与日志模块 - 自动化测试");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));

        JLabel descLabel = new JLabel("包含：操作日志查询、登录日志查询、清空日志集成测试、"
            + "在线用户强退集成测试、数据驱动操作日志搜索测试（25组数据）、性能测试");
        descLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        descLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(descLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")),
            " 测试操作 "));
        panel.setPreferredSize(new Dimension(0, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 按钮1: 运行单模块测试
        gbc.gridx = 0;
        gbc.gridy = 0;
        JButton btnSingleModule = createActionButton(
            "运行单模块测试",
            "操作日志查询 + 登录日志查询",
            UIManager.getColor("Actions.Green"));
        btnSingleModule.addActionListener(e -> runSingleModuleTest());
        panel.add(btnSingleModule, gbc);

        // 按钮2: 运行集成测试-深度3
        gbc.gridx = 1;
        gbc.gridy = 0;
        JButton btnDepth3 = createActionButton(
            "运行集成测试-深度3",
            "登录 → 操作日志 → 清空日志",
            new Color(70, 130, 180));
        btnDepth3.addActionListener(e -> runDepth3Test());
        panel.add(btnDepth3, gbc);

        // 按钮3: 运行集成测试-深度4
        gbc.gridx = 2;
        gbc.gridy = 0;
        JButton btnDepth4 = createActionButton(
            "运行集成测试-深度4",
            "登录 → 在线用户 → IP搜索 → 强退确认",
            new Color(138, 43, 226));
        btnDepth4.addActionListener(e -> runDepth4Test());
        panel.add(btnDepth4, gbc);

        // 按钮4: 运行数据组合测试
        gbc.gridx = 0;
        gbc.gridy = 1;
        JButton btnDataDriven = createActionButton(
            "运行数据组合测试",
            "操作日志高级搜索25组条件组合测试",
            new Color(220, 130, 50));
        btnDataDriven.addActionListener(e -> runDataDrivenTest());
        panel.add(btnDataDriven, gbc);

        // 按钮5: 执行性能测试
        gbc.gridx = 1;
        gbc.gridy = 1;
        JButton btnPerformance = createActionButton(
            "执行性能测试",
            "调用本地JMeter执行 /monitor/operlog/list 并发测试",
            new Color(180, 50, 50));
        btnPerformance.addActionListener(e -> runPerformanceTest());
        panel.add(btnPerformance, gbc);

        // 全部运行按钮
        gbc.gridx = 2;
        gbc.gridy = 1;
        JButton btnRunAll = createActionButton(
            "运行全部测试",
            "依次运行以上所有测试项",
            new Color(60, 60, 60));
        btnRunAll.addActionListener(e -> runAllTests());
        panel.add(btnRunAll, gbc);

        // 配置区域
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        panel.add(createConfigRow(), gbc);

        return panel;
    }

    private JPanel createConfigRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));

        row.add(new JLabel("Base URL:"));
        JTextField baseUrlField = new JTextField(testRunner.getBaseUrl(), 25);
        baseUrlField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        baseUrlField.addActionListener(e -> testRunner.setBaseUrl(baseUrlField.getText().trim()));
        row.add(baseUrlField);

        JButton saveUrlBtn = new JButton("保存URL");
        saveUrlBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        saveUrlBtn.addActionListener(e -> {
            testRunner.setBaseUrl(baseUrlField.getText().trim());
            appendLog("[配置] Base URL 已更新为: " + testRunner.getBaseUrl());
        });
        row.add(saveUrlBtn);

        row.add(Box.createHorizontalStrut(16));

        JCheckBox keepBrowserCheck = new JCheckBox("测试后保持浏览器打开");
        keepBrowserCheck.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        keepBrowserCheck.setSelected(false);
        keepBrowserCheck.addActionListener(e -> {
            boolean keep = keepBrowserCheck.isSelected();
            System.setProperty("ruoyi.keep.browser.open", String.valueOf(keep));
            appendLog("[配置] 测试后保持浏览器打开: " + (keep ? "是" : "否"));
        });
        row.add(keepBrowserCheck);

        row.add(Box.createHorizontalStrut(12));

        JButton clearLogBtn = new JButton("清空日志");
        clearLogBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        row.add(clearLogBtn);

        return row;
    }

    private JButton createActionButton(String text, String tooltip, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(220, 50));
        btn.setMinimumSize(new Dimension(180, 50));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (bgColor != null) {
            btn.setBackground(bgColor);
            btn.setForeground(Color.WHITE);
        }

        return btn;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")),
            " 测试执行日志 "));
        panel.setPreferredSize(new Dimension(0, 350));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ======================== 事件处理 ========================

    private List<String> promptUserForTestCases(int... allowedIndices) {
        String[] allMethods = TestRunner.ALL_TEST_METHODS;
        // 偏移量：前5个是组长的方法，后5个是本模块的方法
        int offset = 5;
        StringBuilder sb = new StringBuilder();
        sb.append("请输入需要执行的测试用例编号（例如：1,3，或留空执行全部）：\n\n");
        sb.append("可用的测试用例：\n");
        for (int idx : allowedIndices) {
            int displayNum = idx + 1;
            int actualIdx = offset + idx;
            if (actualIdx < allMethods.length) {
                sb.append("  ").append(displayNum).append(". ").append(allMethods[actualIdx]).append("\n");
            }
        }

        String input = (String) JOptionPane.showInputDialog(
            SwingUtilities.getWindowAncestor(this),
            sb.toString(),
            "选择测试用例",
            JOptionPane.QUESTION_MESSAGE,
            null, null, ""
        );

        if (input == null) {
            appendLog("[用户操作] 已取消测试执行");
            return null;
        }

        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            appendLog("[用户操作] 留空，将执行当前模块全部测试用例");
            return new ArrayList<>();
        }

        List<String> selectedMethods = new ArrayList<>();
        String[] parts = trimmed.split("[,\\s]+");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            try {
                int displayNum = Integer.parseInt(part);
                int index = displayNum - 1;
                boolean allowed = false;
                for (int ai : allowedIndices) {
                    if (ai == index) { allowed = true; break; }
                }
                if (allowed) {
                    int actualIdx = offset + index;
                    if (actualIdx < allMethods.length) {
                        selectedMethods.add(allMethods[actualIdx]);
                    }
                    continue;
                }
            } catch (NumberFormatException ignored) { }
            for (int idx : allowedIndices) {
                int actualIdx = offset + idx;
                if (actualIdx < allMethods.length) {
                    String method = allMethods[actualIdx];
                    if (method.equalsIgnoreCase(part) || method.contains(part)) {
                        selectedMethods.add(method);
                        break;
                    }
                }
            }
        }

        if (selectedMethods.isEmpty()) {
            appendLog("[WARN] 没有匹配到有效测试用例，将执行当前模块全部");
            return new ArrayList<>();
        }

        appendLog("[用户操作] 已选择测试用例: " + String.join(", ", selectedMethods));
        return selectedMethods;
    }

    private void runSingleModuleTest() {
        appendLogSeparator("运行单模块测试（操作日志查询 + 登录日志查询）");
        List<String> selected = promptUserForTestCases(0, 1);
        if (selected == null) return;
        List<String> targets = selected.isEmpty()
            ? List.of("testSingleModule_OperLogQuery", "testSingleModule_LoginLogQuery")
            : selected;
        new Thread(() -> testRunner.runFilteredMethods(TEST_CLASS, targets)).start();
    }

    private void runDepth3Test() {
        appendLogSeparator("运行集成测试-深度3（登录 → 操作日志 → 清空日志）");
        appendLog("[测试用例] testIntegration_Depth3_CleanLog");
        new Thread(() -> testRunner.runFilteredMethods(TEST_CLASS,
            List.of("testIntegration_Depth3_CleanLog"))).start();
    }

    private void runDepth4Test() {
        appendLogSeparator("运行集成测试-深度4（登录 → 在线用户 → IP搜索 → 强退确认）");
        appendLog("[测试用例] testIntegration_Depth4_ForceLogout");
        new Thread(() -> testRunner.runFilteredMethods(TEST_CLASS,
            List.of("testIntegration_Depth4_ForceLogout"))).start();
    }

    private void runDataDrivenTest() {
        appendLogSeparator("运行数据组合测试（25组操作日志搜索条件）");
        appendLog("[测试用例] testDataDriven_OperLogSearch");
        new Thread(() -> testRunner.runFilteredMethods(TEST_CLASS,
            List.of("testDataDriven_OperLogSearch"))).start();
    }

    private void runPerformanceTest() {
        appendLogSeparator("执行性能测试（JMeter命令模式）");
        new Thread(() -> {
            // 1. 查找 JMeter
            appendLog("[性能测试] 正在搜索本机 JMeter 安装...");
            String jmeterHome = findJmeterHome();
            if (jmeterHome == null) {
                appendLog("[性能测试] 未找到 JMeter，请检查 E:\\apache-jmeter-5.6.3 是否存在");
                return;
            }
            appendLog("[性能测试] 已找到 JMeter: " + jmeterHome);

            // 2. 定位 JMX 脚本（优先找当前项目目录下的 jmeter 子目录）
            String jmxPath = findJmxScript();
            if (jmxPath == null) {
                return;
            }
            appendLog("[性能测试] JMX脚本: " + jmxPath);

            // 3. 准备输出路径
            String resultDir = new java.io.File(jmxPath).getParent();
            String jtlFile = resultDir + File.separator + "result.jtl";
            String reportDir = resultDir + File.separator + "report";

            // 清理旧结果
            new java.io.File(jtlFile).delete();
            deleteDirectory(new java.io.File(reportDir));
            new java.io.File(reportDir).mkdirs();

            // 4. 执行 JMeter（用 java -jar ApacheJMeter.jar，避免依赖 .bat）
            String javaBin = findJava17();
            String jmeterJar = jmeterHome + File.separator + "bin" + File.separator + "ApacheJMeter.jar";

            appendLog("[性能测试] 正在启动 JMeter 150 并发压测，请等待...");
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    javaBin, "-jar", jmeterJar,
                    "-n", "-t", jmxPath,
                    "-l", jtlFile,
                    "-e", "-o", reportDir
                );
                pb.directory(new java.io.File(resultDir));
                pb.redirectErrorStream(true);
                Process process = pb.start();

                // 读取 JMeter 实时输出
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String logLine = line;
                        SwingUtilities.invokeLater(() -> {
                            if (logLine.contains("Err:")) {
                                // 只显示关键摘要行
                                String summary = logLine.replaceAll("summary \\+", "");
                                appendLog("[JMeter] " + summary.trim());
                            }
                        });
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    appendLog("[性能测试] 测试执行完毕！");
                    showPerformanceSummary(jtlFile);
                    // 尝试打开 HTML 报告
                    openReportInBrowser(reportDir + File.separator + "index.html");
                } else {
                    appendLog("[性能测试] JMeter 执行异常，退出码: " + exitCode);
                }
            } catch (Exception ex) {
                appendLog("[性能测试] 执行异常: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    /**
     * 显示性能测试汇总结果（从 JTL 统计）
     */
    private void showPerformanceSummary(String jtlFile) {
        java.io.File f = new java.io.File(jtlFile);
        if (!f.exists()) {
            appendLog("[性能测试] 未找到结果文件，无法统计");
            return;
        }
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.FileReader(jtlFile))) {
            String header = br.readLine(); // 跳过 CSV 头
            if (header == null) return;

            int totalSamples = 0;
            int totalErrors = 0;
            long totalTime = 0;
            long minTime = Long.MAX_VALUE;
            long maxTime = Long.MIN_VALUE;

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 9) continue;
                try {
                    totalSamples++;
                    long elapsed = Long.parseLong(parts[1]);
                    totalTime += elapsed;
                    minTime = Math.min(minTime, elapsed);
                    maxTime = Math.max(maxTime, elapsed);
                    if (!"true".equals(parts[8])) {
                        totalErrors++;
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            double errorPct = totalSamples > 0 ? (totalErrors * 100.0 / totalSamples) : 0;
            double avgTime = totalSamples > 0 ? (totalTime / (double) totalSamples) : 0;

            appendLog("");
            appendLog("========== 性能测试结果汇总 ==========");
            appendLog(String.format("  总样本数: %d", totalSamples));
            appendLog(String.format("  错误数:   %d (%.2f%%)", totalErrors, errorPct));
            appendLog(String.format("  平均响应: %.2f ms", avgTime));
            appendLog(String.format("  最小响应: %d ms", minTime));
            appendLog(String.format("  最大响应: %d ms", maxTime));
            appendLog("======================================");
            appendLog("");

            if (errorPct == 0 && totalSamples >= 100) {
                appendLog("[PASS] 满足实验要求：Samples >= 100 且 Error%% = 0.00%%");
            } else {
                appendLog("[WARN] 未达到实验要求指标，请检查测试配置");
            }
        } catch (Exception e) {
            appendLog("[性能测试] 读取结果异常: " + e.getMessage());
        }
    }

    private String findJmxScript() {
        // 搜索路径：项目 jmeter 目录、当前目录、类路径
        String[] searchPaths = {
            "jmeter" + File.separator + "monitor_perf_test.jmx",
            "ruoyi-autotest-gui" + File.separator + "jmeter" + File.separator + "monitor_perf_test.jmx",
            ".." + File.separator + "ruoyi-autotest-gui" + File.separator + "jmeter" + File.separator + "monitor_perf_test.jmx",
            System.getProperty("user.dir") + File.separator + "jmeter" + File.separator + "monitor_perf_test.jmx",
        };
        for (String path : searchPaths) {
            java.io.File f = new java.io.File(path);
            if (f.exists()) {
                return f.getAbsolutePath();
            }
        }
        appendLog("[性能测试] 未找到 JMX 脚本文件 monitor_perf_test.jmx");
        appendLog("[性能测试] 请将脚本放置于 jmeter/ 目录下");
        return null;
    }

    private String findJava17() {
        // 优先使用 JAVA_HOME 指向的 java
        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome != null && !javaHome.isEmpty()) {
            String java = javaHome + File.separator + "bin" + File.separator + "java";
            if (new java.io.File(java).exists()) return java;
        }
        // 回退到 PATH 中的 java
        return "java";
    }

    private void openReportInBrowser(String htmlPath) {
        java.io.File reportFile = new java.io.File(htmlPath);
        if (!reportFile.exists()) return;
        try {
            java.awt.Desktop.getDesktop().browse(reportFile.toURI());
            appendLog("[性能测试] 已打开 HTML 报告");
        } catch (Exception e) {
            appendLog("[性能测试] 报告路径: " + reportFile.getAbsolutePath());
            appendLog("[性能测试] 请手动在浏览器中打开以上路径查看完整报告");
        }
    }

    private void deleteDirectory(java.io.File dir) {
        if (dir == null || !dir.exists()) return;
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            for (java.io.File f : files) {
                if (f.isDirectory()) deleteDirectory(f);
                else f.delete();
            }
        }
        dir.delete();
    }

    private String findJmeterHome() {
        // 优先检测本机已安装的路径
        String[] searchDirs = {
            "E:\\apache-jmeter-5.6.3",
            "E:\\apache-jmeter-5.6.2",
            "C:\\apache-jmeter\\apache-jmeter-5.6.3",
            "C:\\apache-jmeter\\apache-jmeter-5.6.2",
        };
        String envHome = System.getenv("JMETER_HOME");
        if (envHome != null && !envHome.isEmpty()) {
            java.io.File f = new java.io.File(envHome, "bin" + File.separator + "ApacheJMeter.jar");
            if (f.exists()) return envHome;
        }
        for (String dir : searchDirs) {
            java.io.File f = new java.io.File(dir, "bin" + File.separator + "ApacheJMeter.jar");
            if (f.exists()) return dir;
        }
        // 通用搜索 C:\apache-jmeter\ 下的子目录
        java.io.File cRoot = new java.io.File("C:\\apache-jmeter");
        if (cRoot.isDirectory()) {
            java.io.File[] subs = cRoot.listFiles();
            if (subs != null) {
                for (java.io.File sub : subs) {
                    if (sub.isDirectory()) {
                        java.io.File f = new java.io.File(sub, "bin" + File.separator + "ApacheJMeter.jar");
                        if (f.exists()) return sub.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    private void runAllTests() {
        appendLogSeparator("运行全部测试（依次执行所有测试方法）");
        List<String> selected = promptUserForTestCases(0, 1, 2, 3, 4);
        if (selected == null) return;
        List<String> targets = selected.isEmpty()
            ? List.of("testSingleModule_OperLogQuery", "testSingleModule_LoginLogQuery",
                       "testIntegration_Depth3_CleanLog", "testIntegration_Depth4_ForceLogout",
                       "testDataDriven_OperLogSearch")
            : selected;
        new Thread(() -> testRunner.runFilteredMethods(TEST_CLASS, targets)).start();
    }

    // ======================== 日志输出 ========================

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalDateTime.now().format(DT_FORMAT);
            logArea.append("[" + timestamp + "] " + message + "\n");
        });
    }

    private void appendLogSeparator(String title) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("\n" + "=".repeat(70) + "\n");
            logArea.append("  " + title + "\n");
            logArea.append("=".repeat(70) + "\n");
        });
    }
}
