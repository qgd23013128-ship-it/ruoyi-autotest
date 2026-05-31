package com.ruoyi.autotest.gui.panel;

import javax.swing.*;
import java.awt.*;

/**
 * 部门与岗位模块测试面板（占位）
 */
public class DeptPostPanel extends JPanel {

    public DeptPostPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("部门与岗位模块");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));

        JLabel descLabel = new JLabel("<html><br>该模块包含以下功能测试：<br><br>"
            + "• 部门管理 - 部门树查询、新增部门、编辑部门<br>"
            + "• 岗位管理 - 岗位查询、新增岗位、编辑岗位<br>"
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
