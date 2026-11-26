package com.shopping.controller;

import com.shopping.constant.Constants;
import com.shopping.dao.UserDAO;
import com.shopping.model.User;
import com.shopping.view.CustomerView;
import com.shopping.view.LoginView;
import com.shopping.view.MerchantView;
import com.shopping.view.AdminView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginController {

    private LoginView view;
    private UserDAO userDAO;

    public LoginController(LoginView view) {
        this.view = view;
        this.userDAO = new UserDAO();

        this.view.addLoginListener(new LoginListener());
        this.view.addRegisterListener(new RegisterListener());
    }

    class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = view.getUsername();
            String password = view.getPassword();
            String role = view.getSelectedRole();

            if (username.isEmpty() || password.isEmpty()) {
                view.showMessage("Username and Password cannot be empty!");
                return;
            }

            User user = userDAO.login(username, password, role);

            if (user != null) {
                view.showMessage("Login Successful!");
                view.dispose();

                if (Constants.ROLE_MERCHANT.equals(user.getRole())) {
                    new MerchantController(new MerchantView(), user);
                } else if (Constants.ROLE_ADMIN.equals(user.getRole())) {
                    new AdminController(new AdminView(), user);
                } else {
                    new CustomerController(new CustomerView(), user);
                }
            } else {
                view.showMessage("Login Failed: Invalid credentials or wrong role.");
            }
        }
    }

    class RegisterListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = view.getUsername();
            String password = view.getPassword();
            String role = view.getSelectedRole();

            //  基础非空验证
            if (username.isEmpty() || password.isEmpty()) {
                view.showMessage("Please enter all fields to register.");
                return;
            }

            // 禁止注册管理员
            if (Constants.ROLE_ADMIN.equals(role)) {
                view.showMessage("Security Alert: Administrator registration is NOT allowed via this interface.\nPlease contact system support.");
                return; // 直接结束，不执行后续数据库操作
            }

            //  执行普通用户或商家的注册
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRole(role);
            newUser.setBalance(0.0);

            boolean success = userDAO.register(newUser);

            if (success) {
                view.showMessage("Registration Successful! Please login.");
            } else {
                view.showMessage("Registration Failed: Username might already exist.");
            }
        }
    }
}