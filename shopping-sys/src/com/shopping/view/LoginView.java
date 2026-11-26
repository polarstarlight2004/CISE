package com.shopping.view;

import com.shopping.constant.Constants;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 登录界面
 * TODO 登录界面后续还需要新增管理员用于管理全局用户和商品
 */
public class LoginView extends JFrame {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole;
    private JButton btnLogin;
    private JButton btnRegister;

    public LoginView() {
        super("Online Shopping System - Login");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        //juzhong 剧中  居中
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 角色选择 (Role)
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Select Role:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        // 下拉框内容
        String[] roles = {
                Constants.ROLE_CUSTOMER,
                Constants.ROLE_MERCHANT,
                Constants.ROLE_ADMIN
        };
        cmbRole = new JComboBox<>(roles);
        add(cmbRole, gbc);

        // 用户名 (Username)
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Username:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        txtUsername = new JTextField(15);
        add(txtUsername, gbc);

        //  密码 (Password)
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        txtPassword = new JPasswordField(15);
        add(txtPassword, gbc);

        // 按钮区域
        JPanel buttonPanel = new JPanel();
        btnLogin = new JButton("Login");
        btnRegister = new JButton("Register");
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2; // 占两列
        add(buttonPanel, gbc);
    }

    
    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

    public String getSelectedRole() {
        return (String) cmbRole.getSelectedItem();
    }


    //进行时间绑定
    public void addLoginListener(ActionListener listener) {
        btnLogin.addActionListener(listener);
    }

    public void addRegisterListener(ActionListener listener) {
        btnRegister.addActionListener(listener);
    }
    
    //弹出提示框
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}