package com.shopping;

import com.shopping.controller.LoginController;
import com.shopping.util.DatabaseHelper;
import com.shopping.view.LoginView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        //初始化数据库与建表
        DatabaseHelper.getInstance();

        // 拉取界面 用户或者商家登录
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            // 登录界面
            new LoginController(loginView);

            // 显示界面
            loginView.setVisible(true);
        });
    }
}
