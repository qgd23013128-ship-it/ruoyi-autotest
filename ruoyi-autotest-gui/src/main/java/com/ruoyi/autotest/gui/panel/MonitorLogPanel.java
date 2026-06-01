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
        appendLogSeparator("执行性能测试（JMeter命令行）");
        new Thread(() -> {
            appendLog("[性能测试] 正在自动搜索本机 JMeter 安装...");
            String jmeterHome = findJmeterHome();
            if (jmeterHome == null) {
                appendLog("[性能测试] 未找到 JMeter，请下载解压到 C:\\apache-jmeter 目录");
                return;
            }
            appendLog("[性能测试] 已找到 JMeter: " + jmeterHome);

            String scriptPath = jmeterHome + "\\bin\\RuoYi_MonitorLog_Test.jmx";
            String jmeterCmd = jmeterHome + "\\bin\\jmeter.bat";

            java.io.File scriptFile = new java.io.File(scriptPath);
            if (!scriptFile.exists()) {
                appendLog("[性能测试] JMX脚本文件不存在: " + scriptPath);
                appendLog("[性能测试] 请将 JMX 测试脚本放置于: " + scriptPath);
                return;
            }

            appendLog("[性能测试] 正在启动 JMeter...");
            try {
                String resultFile = "jmeter_monitorlog_result.jtl";
                ProcessBuilder pb = new ProcessBuilder(
                    jmeterCmd, "-n", "-t", scriptPath,
                    "-l", resultFile
                );
                pb.inheritIO();
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    appendLog("[性能测试] 性能测试执行完毕，结果文件: " + new java.io.File(resultFile).getAbsolutePath());
                } else {
                    appendLog("[性能测试] JMeter 执行异常，退出码: " + exitCode);
                }
            } catch (Exception ex) {
                appendLog("[性能测试] 执行异常: " + ex.getMessage());
            }
        }).start();
    }

    private String findJmeterHome() {
        String envHome = System.getenv("JMETER_HOME");
        if (envHome != null && !envHome.isEmpty()) {
            java.io.File f = new java.io.File(envHome, "bin\\jmeter.bat");
            if (f.exists()) return envHome;
        }
        String[] searchDirs = {
            "C:\\apache-jmeter\\apache-jmeter-5.6.3",
            "C:\\apache-jmeter\\apache-jmeter-5.6.2",
            "C:\\apache-jmeter\\apache-jmeter-5.5",
            "D:\\apache-jmeter\\apache-jmeter-5.6.3",
            "D:\\apache-jmeter\\apache-jmeter-5.6.2",
        };
        for (String dir : searchDirs) {
            java.io.File f = new java.io.File(dir, "bin\\jmeter.bat");
            if (f.exists()) return dir;
        }
        java.io.File cRoot = new java.io.File("C:\\apache-jmeter");
        if (cRoot.isDirectory()) {
            java.io.File[] subs = cRoot.listFiles();
            if (subs != null) {
                for (java.io.File sub : subs) {
                    if (sub.isDirectory()) {
                        java.io.File f = new java.io.File(sub, "bin\\jmeter.bat");
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
