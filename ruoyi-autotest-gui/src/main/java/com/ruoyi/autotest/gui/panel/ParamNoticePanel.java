package com.ruoyi.autotest.gui.panel;

import javax.swing.*;
import java.awt.*;

/**
 * 参数与通知公告模块测试面板（占位）
 */
public class ParamNoticePanel extends JPanel {

    public ParamNoticePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("参数与通知公告模块");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));

        JLabel descLabel = new JLabel("<html><br>该模块包含以下功能测试：<br><br>"
            + "• 参数配置 - 参数查询、新增参数、编辑参数<br>"
            + "• 通知公告 - 公告列表、新增公告、编辑公告<br>"
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
