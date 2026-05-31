package com.ruoyi.autotest.gui.panel;

import javax.swing.*;
import java.awt.*;

/**
 * 系统监控与日志模块测试面板（占位）
 */
public class MonitorLogPanel extends JPanel {

    public MonitorLogPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("系统监控与日志模块");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));

        JLabel descLabel = new JLabel("<html><br>该模块包含以下功能测试：<br><br>"
            + "• 在线用户 - 在线用户监控、强制下线<br>"
            + "• 定时任务 - 任务调度管理<br>"
            + "• 数据监控 - Druid数据源监控<br>"
            + "• 服务监控 - 服务器状态监控<br>"
            + "• 日志管理 - 操作日志、登录日志查询<br>"
            + "<br>（敬请期待...）</html>");
        descLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(titleLabel, gbc);
        gbc.gridy = 1;
        centerPanel.add(descLabel, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }
}
