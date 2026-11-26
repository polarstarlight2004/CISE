package com.shopping.controller;

import com.shopping.dao.ProductDAO;
import com.shopping.dao.UserDAO;
import com.shopping.model.User;
import com.shopping.view.AdminView;
import com.shopping.view.LoginView;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class AdminController {

    private AdminView view;
    private UserDAO userDAO;
    private ProductDAO productDAO;

    public AdminController(AdminView view, User adminUser) {
        this.view = view;
        this.userDAO = new UserDAO();
        this.productDAO = new ProductDAO();

        // 初始加载数据
        loadUsers();
        loadProducts();

        // 绑定事件
        view.addLogoutListener(e -> logout());
        
        // 绑定用户管理按钮
        view.addUserActionsListener(
            e -> loadUsers(), // Refresh
            e -> deleteUser() // Delete
        );

        // 绑定商品管理按钮
        view.addProdActionsListener(
            e -> loadProducts(), // Refresh
            e -> deleteProduct() // Delete
        );

        view.addLogoutListener(new AdminController.LogoutAction()); //退出功能

        view.setVisible(true);
    }

    private void loadUsers() {
        Vector<Vector<Object>> data = userDAO.getAllUsers();
        view.getUserModel().setRowCount(0);
        for (Vector<Object> row : data) view.getUserModel().addRow(row);
    }

    // 加载商品数据
    private void loadProducts() {
        Vector<Vector<Object>> data = productDAO.getAllProductsForAdmin();
        view.getProdModel().setRowCount(0);
        for (Vector<Object> row : data) view.getProdModel().addRow(row);
    }

    private void deleteUser() {
        int row = view.getUserTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view, "Please select a user to delete.");
            return;
        }
        int userId = (int) view.getUserTable().getValueAt(row, 0);
        String username = (String) view.getUserTable().getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(view, 
            "Delete user '" + username + "'? \nWarning: Users with active orders cannot be deleted.", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(userId)) {
                JOptionPane.showMessageDialog(view, "User deleted successfully.");
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(view, "Failed to delete user.\n(They likely have related data in DB)");
            }
        }
    }

    private void deleteProduct() {
        int row = view.getProdTable().getSelectedRow();
        if (row == -1) return;
        int prodId = (int) view.getProdTable().getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(view, "Force delete this product?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (productDAO.adminDeleteProduct(prodId)) {
                JOptionPane.showMessageDialog(view, "Product deleted.");
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(view, "Delete failed (Item might be in orders).");
            }
        }
    }

    private void logout() {
        view.dispose();
        new LoginController(new LoginView());
    }

    // 退出功能
    class LogoutAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 1. 弹出确认框，提升用户体验
            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Are you sure you want to logout?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // 2. 关闭当前窗口
                view.dispose();

                // 3. 重新打开登录界面
                LoginView loginView = new LoginView();
                new LoginController(loginView);
                loginView.setVisible(true);
            }
        }
    }
}