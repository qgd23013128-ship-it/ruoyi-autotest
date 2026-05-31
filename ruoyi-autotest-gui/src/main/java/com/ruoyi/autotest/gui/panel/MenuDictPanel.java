package com.ruoyi.autotest.gui.panel;

import javax.swing.*;
import java.awt.*;

/**
 * 菜单与字典模块测试面板（占位）
 */
public class MenuDictPanel extends JPanel {

    public MenuDictPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("菜单与字典模块");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));

        JLabel descLabel = new JLabel("<html><br>该模块包含以下功能测试：<br><br>"
            + "• 菜单管理 - 菜单树查询、新增菜单、编辑菜单<br>"
            + "• 字典管理 - 字典类型管理、字典数据管理<br>"
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
