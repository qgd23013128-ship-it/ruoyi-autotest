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

import com.ruoyi.autotest.gui.runner.JmeterPerformanceRunner;
import com.ruoyi.autotest.gui.runner.TestRunner;

public class MenuDictPanel extends JPanel {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private JTextArea logArea;
    private final TestRunner testRunner;

    private static final String TEST_CLASS = "com.ruoyi.autotest.test.DictionaryTest";

    public MenuDictPanel() {
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

        JLabel titleLabel = new JLabel("菜单与字典模块 - 自动化测试");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));

        JLabel descLabel = new JLabel(
            "2.1 单模块(2) | 2.2 集成深度3/4 | 2.3 JMeter 110并发 | 2.4 数据驱动25组");
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
            " 测试操作（2.1 ~ 2.4） "));
        panel.setPreferredSize(new Dimension(0, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JButton btnDictQuery = createActionButton(
            "2.1 单模块测试",
            "字典类型查询（dictType）+ 字典数据停用（编辑）",
            UIManager.getColor("Actions.Green"));
        btnDictQuery.addActionListener(e -> runSingleModuleTest());
        panel.add(btnDictQuery, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        JButton btnDepth3 = createActionButton(
            "2.2 集成-深度3",
            "登录→菜单管理→编辑已显示菜单→设为隐藏",
            new Color(70, 130, 180));
        btnDepth3.addActionListener(e -> runDepth3Test());
        panel.add(btnDepth3, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        JButton btnDepth4 = createActionButton(
            "2.2 集成-深度4",
            "登录→字典管理→字典数据页→新增键值",
            new Color(138, 43, 226));
        btnDepth4.addActionListener(e -> runDepth4Test());
        panel.add(btnDepth4, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JButton btnDataDriven = createActionButton(
            "2.4 数据组合测试",
            "新增字典数据表单 25 组合法性校验",
            new Color(220, 130, 50));
        btnDataDriven.addActionListener(e -> runDataDrivenTest());
        panel.add(btnDataDriven, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        JButton btnPerformance = createActionButton(
            "2.3 性能测试",
            "JMeter 110并发 POST /system/dict/list",
            new Color(180, 50, 50));
        btnPerformance.addActionListener(e -> runPerformanceTest());
        panel.add(btnPerformance, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        JButton btnRunAll = createActionButton(
            "运行全部测试",
            "依次运行以上所有测试项",
            new Color(60, 60, 60));
        btnRunAll.addActionListener(e -> runAllTests());
        panel.add(btnRunAll, gbc);

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
        keepBrowserCheck.setSelected(true);
        System.setProperty("ruoyi.keep.browser.open", "true");
        keepBrowserCheck.addActionListener(e -> {
            boolean keep = keepBrowserCheck.isSelected();
            System.setProperty("ruoyi.keep.browser.open", String.valueOf(keep));
            appendLog("[配置] 测试后保持浏览器打开: " + (keep ? "是" : "否"));
        });
        row.add(keepBrowserCheck);

        row.add(Box.createHorizontalStrut(12));

        JCheckBox slowDemoCheck = new JCheckBox("慢速演示（便于观察查询过程）");
        slowDemoCheck.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        slowDemoCheck.setSelected(true);
        applySlowDemoSettings(true);
        slowDemoCheck.addActionListener(e -> {
            boolean enabled = slowDemoCheck.isSelected();
            applySlowDemoSettings(enabled);
            if (enabled) {
                appendLog("[配置] 慢速演示: 开启（每步操作间隔 1 秒，结束停留 3 秒）");
            } else {
                appendLog("[配置] 慢速演示: 关闭（每步操作间隔 1 秒，结束停留 3 秒）");
            }
        });
        row.add(slowDemoCheck);

        row.add(Box.createHorizontalStrut(12));

        JButton clearLogBtn = new JButton("清空日志");
        clearLogBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        clearLogBtn.addActionListener(e -> logArea.setText(""));
        row.add(clearLogBtn);

        return row;
    }

    private void applySlowDemoSettings(boolean slowDemoEnabled) {
        System.setProperty("ruoyi.test.step.pause.ms", "1000");
        System.setProperty("ruoyi.test.result.pause.ms", "1000");
        System.setProperty("ruoyi.test.finish.pause.ms", "3000");
        System.setProperty("ruoyi.test.action.delay.ms", "0");
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

    private List<String> promptUserForTestCases() {
        return promptUserForTestCases(0, 1, 2, 3, 4);
    }

    private List<String> promptUserForTestCases(int... allowedIndices) {
        StringBuilder sb = new StringBuilder();
        sb.append("请输入需要执行的测试用例编号（例如：1,3，或留空执行全部）：\n\n");
        sb.append("可用的测试用例：\n");
        for (int idx : allowedIndices) {
            int displayNum = idx + 1;
            sb.append("  ").append(displayNum).append(". ").append(TestRunner.DICT_TEST_DESCRIPTIONS[idx]).append("\n");
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
            if (part.isEmpty()) {
                continue;
            }

            try {
                int displayNum = Integer.parseInt(part);
                int index = displayNum - 1;
                boolean allowed = false;
                for (int ai : allowedIndices) {
                    if (ai == index) { allowed = true; break; }
                }
                if (allowed) {
                    selectedMethods.add(TestRunner.DICT_TEST_METHODS[index]);
                    continue;
                }
            } catch (NumberFormatException ignored) {
            }

            for (int idx : allowedIndices) {
                String method = TestRunner.DICT_TEST_METHODS[idx];
                if (method.equalsIgnoreCase(part) || method.contains(part)) {
                    selectedMethods.add(method);
                    break;
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
        appendLogSeparator("2.1 单模块测试（字典类型查询 + 字典数据停用）");
        List<String> selected = promptUserForTestCases(0, 1);
        if (selected == null) { return; }
        List<String> targets = selected.isEmpty()
            ? List.of("testSingleModule_DictTypeQuery", "testSingleModule_DictDataDeactivation")
            : selected;
        new Thread(() -> testRunner.runFilteredMethods(TEST_CLASS, targets)).start();
    }

    private void runDepth3Test() {
        appendLogSeparator("2.2 集成测试-深度3（菜单显示/隐藏切换）");
        appendLog("[测试用例] testIntegration_Depth3_MenuDisplayToggle");
        new Thread(() -> testRunner.runFilteredMethods(TEST_CLASS,
            List.of("testIntegration_Depth3_MenuDisplayToggle"))).start();
    }

    private void runDepth4Test() {
        appendLogSeparator("2.2 集成测试-深度4（字典管理新增键值）");
        appendLog("[测试用例] testIntegration_Depth4_AddDictData");
        new Thread(() -> testRunner.runFilteredMethods(TEST_CLASS,
            List.of("testIntegration_Depth4_AddDictData"))).start();
    }

    private void runDataDrivenTest() {
        appendLogSeparator("2.4 数据组合测试（25组新增字典数据校验）");
        appendLog("[测试用例] testDataDriven_DictDataValidation");
        new Thread(() -> testRunner.runFilteredMethods(TEST_CLASS,
            List.of("testDataDriven_DictDataValidation"))).start();
    }

    private void runPerformanceTest() {
        appendLogSeparator("2.3 性能测试（JMeter 110并发 /system/dict/list）");
        new Thread(() -> JmeterPerformanceRunner.runDictTypeListTest(
            testRunner.getBaseUrl(), this::appendLog)).start();
    }

    private void runAllTests() {
        appendLogSeparator("运行全部 Selenium 测试（2.1/2.2/2.4，不含 JMeter）");
        List<String> selected = promptUserForTestCases();
        if (selected == null) { return; }
        List<String> targets = selected.isEmpty()
            ? List.of("testSingleModule_DictTypeQuery", "testSingleModule_DictDataDeactivation",
                       "testIntegration_Depth3_MenuDisplayToggle", "testIntegration_Depth4_AddDictData",
                       "testDataDriven_DictDataValidation")
            : selected;
        new Thread(() -> testRunner.runFilteredMethods(TEST_CLASS, targets)).start();
    }

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