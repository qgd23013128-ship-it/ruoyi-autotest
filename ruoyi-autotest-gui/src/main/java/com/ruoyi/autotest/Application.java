package com.ruoyi.autotest;

import com.ruoyi.autotest.gui.MainFrame;

import javax.swing.*;

/**
 * 若依后台管理系统自动化测试集成GUI工具 - 应用入口
 */
public class Application {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
