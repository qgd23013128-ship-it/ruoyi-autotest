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
import com.ruoyi.autotest.gui.util.CsvCaseEditor;

/**
 * 部门与岗位模块测试面板。
 */
public class DeptPostPanel extends JPanel {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String TEST_CLASS = "com.ruoyi.autotest.test.DeptPostTest";
    private static final String LOG_PREFIX = "[高浩珈-部门岗位模块]";

    private JTextArea logArea;
    private final TestRunner testRunner;

    public DeptPostPanel() {
        testRunner = new TestRunner();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        add(createTopInfoPanel(), BorderLayout.NORTH);
        add(createButtonPanel(), BorderLayout.CENTER);
        add(createLogPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel titleLabel = new JLabel("部门与岗位模块 - 自动化测试（高浩珈 23013100）");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));

        JLabel moduleInfoLabel = new JLabel("<html>"
            + "负责模块：部门管理与岗位管理<br>"
            + "单模块测试：部门查询、岗位新增、岗位状态停用<br>"
            + "集成测试：岗位新增流程、部门修改表单回显流程<br>"
            + "数据组合测试：25组岗位新增表单数据<br>"
            + "Base URL：使用界面输入框中的地址"
            + "</html>");
        moduleInfoLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        moduleInfoLabel.setForeground(UIManager.getColor("Label.foreground"));
        moduleInfoLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel tipLabel = new JLabel("建议测试顺序：单模块测试 → 集成深度3 → 集成深度4 → 数据组合测试");
        tipLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        tipLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel infoPanel = new JPanel(new BorderLayout(0, 6));
        infoPanel.add(moduleInfoLabel, BorderLayout.CENTER);
        infoPanel.add(tipLabel, BorderLayout.SOUTH);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        JButton btnSingleModule = createActionButton(
            "单模块：部门查询/岗位新增/岗位停用",
            "部门查询 + 岗位新增 + 岗位状态停用",
            UIManager.getColor("Actions.Green"));
        btnSingleModule.addActionListener(e -> runSingleModuleTest());
        panel.add(btnSingleModule, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        JButton btnDepth3 = createActionButton(
            "集成深度3：新增岗位流程",
            "登录 -> 岗位管理 -> 新增岗位保存",
            new Color(70, 130, 180));
        btnDepth3.addActionListener(e -> runDepth3Test());
        panel.add(btnDepth3, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        JButton btnDepth4 = createActionButton(
            "集成深度4：部门修改回显流程",
            "登录 -> 部门管理 -> 展开部门树 -> 修改表单回显 -> 取消",
            new Color(138, 43, 226));
        btnDepth4.addActionListener(e -> runDepth4Test());
        panel.add(btnDepth4, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JButton btnDataDriven = createActionButton(
            "数据组合：25组岗位数据",
            "新增岗位25组数据组合测试",
            new Color(220, 130, 50));
        btnDataDriven.addActionListener(e -> runDataDrivenTest());
        panel.add(btnDataDriven, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        JButton btnPerformance = createActionButton(
            "性能测试：部门列表接口",
            "部门与岗位模块性能测试入口",
            new Color(180, 50, 50));
        btnPerformance.addActionListener(e -> runPerformanceTest());
        panel.add(btnPerformance, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        JButton btnRunAll = createActionButton(
            "运行全部测试",
            "运行 DeptPostTest 全部测试方法",
            new Color(60, 60, 60));
        btnRunAll.addActionListener(e -> runAllTests());
        panel.add(btnRunAll, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JButton btnEditCases = createActionButton(
            "编辑数据组合用例",
            "前端编辑 testdata/deptpost/post_add_cases.csv",
            new Color(120, 110, 70));
        btnEditCases.addActionListener(e -> openCaseEditor());
        panel.add(btnEditCases, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
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
        btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setMinimumSize(new Dimension(220, 50));
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

    private void runSingleModuleTest() {
        runMethods(
            "单模块测试（部门查询 + 岗位新增 + 岗位状态停用）",
            List.of("testSingleModule_DeptQuery", "testSingleModule_PostAdd", "testSingleModule_PostDisableStatus"));
    }

    private void runDepth3Test() {
        runMethods(
            "集成深度3（登录 -> 岗位管理 -> 新增岗位保存）",
            List.of("testIntegration_AddPost"));
    }

    private void runDepth4Test() {
        runMethods(
            "集成深度4（登录 -> 部门管理 -> 修改表单回显 -> 取消返回）",
            List.of("testIntegration_EditDeptLeaderForm"));
    }

    private void runDataDrivenTest() {
        runMethods(
            "数据组合测试（新增岗位25组数据组合）",
            List.of("testDataDriven_AddPost25Cases"));
    }

    private void openCaseEditor() {
        appendLogSeparator("编辑数据组合用例");
        SwingUtilities.invokeLater(() -> {
            try {
                CsvCaseEditor.showDialog(SwingUtilities.getWindowAncestor(this), this::appendLog);
            } catch (Exception ex) {
                appendLog("[ERROR] 打开数据组合用例编辑窗口失败: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                    "打开数据组合用例编辑窗口失败：" + ex.getMessage(),
                    "打开失败",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void runAllTests() {
        appendLogSeparator("运行全部测试（DeptPostTest 全部测试方法）");
        new Thread(() -> testRunner.runAllMethods(TEST_CLASS, this::appendLog)).start();
    }

    private void runPerformanceTest() {
        appendLogSeparator("性能测试：部门列表接口");
        new Thread(() -> {
            appendLog("[性能测试] 正在自动搜索本机 JMeter 安装...");
            String jmeterHome = findJmeterHome();
            if (jmeterHome == null) {
                appendLog("[性能测试] 未找到 JMeter，请将 JMeter 解压到 C:\\apache-jmeter 或 D:\\apache-jmeter 目录。");
                return;
            }

            String scriptPath = jmeterHome + "\\bin\\RuoYi_DeptPost_Test.jmx";
            String jmeterCmd = jmeterHome + "\\bin\\jmeter.bat";
            File scriptFile = new File(scriptPath);
            if (!scriptFile.exists()) {
                appendLog("[性能测试] 未找到 JMX 脚本: " + scriptPath);
                appendLog("[性能测试] 请将部门与岗位模块性能测试脚本命名为 RuoYi_DeptPost_Test.jmx 并放入 JMeter bin 目录。");
                return;
            }

            appendLog("[性能测试] 启动 JMeter: " + scriptPath);
            try {
                String resultFile = "jmeter_dept_post_result.jtl";
                ProcessBuilder pb = new ProcessBuilder(jmeterCmd, "-n", "-t", scriptPath, "-l", resultFile);
                pb.inheritIO();
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    appendLog("[性能测试] 执行完成，结果文件: " + new File(resultFile).getAbsolutePath());
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
        if (hasJmeter(envHome)) {
            return envHome;
        }

        String[] searchDirs = {
            "C:\\apache-jmeter\\apache-jmeter-5.6.3",
            "C:\\apache-jmeter\\apache-jmeter-5.6.2",
            "C:\\apache-jmeter\\apache-jmeter-5.5",
            "D:\\apache-jmeter\\apache-jmeter-5.6.3",
            "D:\\apache-jmeter\\apache-jmeter-5.6.2",
            "D:\\apache-jmeter\\apache-jmeter-5.5"
        };
        for (String dir : searchDirs) {
            if (hasJmeter(dir)) {
                return dir;
            }
        }

        String found = findJmeterUnderRoot("C:\\apache-jmeter");
        if (found != null) {
            return found;
        }
        return findJmeterUnderRoot("D:\\apache-jmeter");
    }

    private boolean hasJmeter(String dir) {
        return dir != null && !dir.isEmpty() && new File(dir, "bin\\jmeter.bat").exists();
    }

    private String findJmeterUnderRoot(String rootPath) {
        File root = new File(rootPath);
        File[] subs = root.isDirectory() ? root.listFiles() : null;
        if (subs == null) {
            return null;
        }
        for (File sub : subs) {
            if (sub.isDirectory() && hasJmeter(sub.getAbsolutePath())) {
                return sub.getAbsolutePath();
            }
        }
        return null;
    }

    private void runMethods(String title, List<String> methodNames) {
        appendLogSeparator(title);
        appendLog("[测试类] " + TEST_CLASS);
        appendLog("[测试方法] " + String.join(", ", methodNames));
        new Thread(() -> testRunner.runFilteredMethods(TEST_CLASS, methodNames, this::appendLog)).start();
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalDateTime.now().format(DT_FORMAT);
            logArea.append("[" + timestamp + "] " + LOG_PREFIX + " " + message + "\n");
        });
    }

    private void appendLogSeparator(String title) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("\n" + "=".repeat(70) + "\n");
            logArea.append(LOG_PREFIX + " " + title + "\n");
            logArea.append("=".repeat(70) + "\n");
        });
    }
}
