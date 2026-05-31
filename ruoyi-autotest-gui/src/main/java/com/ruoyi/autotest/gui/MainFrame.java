package com.ruoyi.autotest.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.ruoyi.autotest.gui.panel.DeptPostPanel;
import com.ruoyi.autotest.gui.panel.MenuDictPanel;
import com.ruoyi.autotest.gui.panel.MonitorLogPanel;
import com.ruoyi.autotest.gui.panel.ParamNoticePanel;
import com.ruoyi.autotest.gui.panel.UserPermissionPanel;

/**
 * 自动化测试工具主窗口
 * 采用左右分栏布局：左侧为导航按钮区，右侧为内容工作区
 */
public class MainFrame extends JFrame {

    private static final String APP_TITLE = "若依后台管理系统 - 自动化测试集成工具 v1.0";

    /** 当前激活的导航按钮 */
    private JButton activeNavButton;

    /** 右侧内容卡片布局面板 */
    private JPanel contentPanel;
    private CardLayout cardLayout;

    /** 五个模块面板 */
    private UserPermissionPanel userPermissionPanel;
    private DeptPostPanel deptPostPanel;
    private MenuDictPanel menuDictPanel;
    private ParamNoticePanel paramNoticePanel;
    private MonitorLogPanel monitorLogPanel;

    public MainFrame() {
        initLookAndFeel();
        initUI();
    }

    private void initLookAndFeel() {
        try {
            com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            System.setProperty("flatlaf.menuBarEmbedded", "false");
        } catch (Exception e) {
            try {
                com.formdev.flatlaf.FlatDarkLaf.setup();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 安全设置应用窗口图标
     * 使用类加载器从 JAR 包内 resources 根目录读取图标。
     * 若图标文件不存在，打印告警并继续启动，绝不崩溃。
     */
    private void setAppIcon() {
        java.net.URL iconUrl = getClass().getResource("/icon.png");
        if (iconUrl == null) {
            System.err.println("[WARN] 警告：未能在 resources 目录中找到图标资源 /icon.png，"
                + "程序将使用默认窗口图标继续运行。");
            return;
        }
        try {
            Image iconImage = Toolkit.getDefaultToolkit().createImage(iconUrl);
            if (iconImage != null) {
                setIconImage(iconImage);
            }
        } catch (Exception e) {
            System.err.println("[WARN] 警告：图标加载失败（" + e.getMessage() + "），"
                + "程序将使用默认窗口图标继续运行。");
        }
    }

    private void initUI() {
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);

        setAppIcon();

        setLayout(new BorderLayout());

        add(createTopBanner(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(1);
        splitPane.setDividerLocation(200);
        splitPane.setEnabled(false);

        splitPane.setLeftComponent(createNavPanel());
        splitPane.setRightComponent(createContentPanel());

        add(splitPane, BorderLayout.CENTER);

        add(createStatusBar(), BorderLayout.SOUTH);
    }

    /**
     * 顶部横幅区域
     */
    private JPanel createTopBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(UIManager.getColor("Panel.background"));
        banner.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
            UIManager.getColor("Separator.foreground")));

        JLabel titleLabel = new JLabel("  若依后台管理系统 - 自动化测试集成工具");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel versionLabel = new JLabel("v1.0  ");
        versionLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        versionLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        banner.add(titleLabel, BorderLayout.WEST);
        banner.add(versionLabel, BorderLayout.EAST);

        return banner;
    }

    /**
     * 左侧导航按钮区域
     */
    private JPanel createNavPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        navPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel navTitle = new JLabel("  测试模块导航");
        navTitle.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        navTitle.setForeground(UIManager.getColor("Label.foreground"));
        navTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        navTitle.setBorder(BorderFactory.createEmptyBorder(8, 4, 16, 4));

        navPanel.add(navTitle);

        String[] navItems = {
            "用户与权限",
            "部门与岗位",
            "菜单与字典",
            "参数与通知公告",
            "系统监控与日志"
        };

        for (int i = 0; i < navItems.length; i++) {
            JButton navBtn = createNavButton(navItems[i], i);
            navPanel.add(navBtn);
            navPanel.add(Box.createVerticalStrut(4));
        }

        navPanel.add(Box.createVerticalGlue());

        JLabel tipLabel = new JLabel("  Java 11 + Selenium 4 + TestNG");
        tipLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
        tipLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        tipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        navPanel.add(tipLabel);

        return navPanel;
    }

    private JButton createNavButton(String text, int index) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(180, 44));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (activeNavButton != null) {
                activeNavButton.putClientProperty("JButton.selected", false);
            }
            activeNavButton = btn;
            btn.putClientProperty("JButton.selected", true);

            String cardName = "card_" + index;
            cardLayout.show(contentPanel, cardName);
        });

        return btn;
    }

    /**
     * 右侧内容工作区（使用CardLayout切换不同模块面板）
     */
    private JPanel createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        userPermissionPanel = new UserPermissionPanel();
        deptPostPanel = new DeptPostPanel();
        menuDictPanel = new MenuDictPanel();
        paramNoticePanel = new ParamNoticePanel();
        monitorLogPanel = new MonitorLogPanel();

        contentPanel.add(userPermissionPanel, "card_0");
        contentPanel.add(deptPostPanel, "card_1");
        contentPanel.add(menuDictPanel, "card_2");
        contentPanel.add(paramNoticePanel, "card_3");
        contentPanel.add(monitorLogPanel, "card_4");

        cardLayout.show(contentPanel, "card_0");

        return contentPanel;
    }

    /**
     * 底部状态栏
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0,
                UIManager.getColor("Separator.foreground")),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));

        JLabel statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));

        JLabel runtimeLabel = new JLabel("Java: " + System.getProperty("java.version")
            + "  |  Selenium 4.x  |  TestNG");
        runtimeLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        runtimeLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(runtimeLabel, BorderLayout.EAST);

        return statusBar;
    }
}
