package com.shopping.controller;

import com.shopping.dao.ProductDAO;
import com.shopping.model.Product;
import com.shopping.model.User;
import com.shopping.view.LoginView;
import com.shopping.view.MerchantView;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class MerchantController {

    private MerchantView view;
    private ProductDAO productDAO;
    private User currentUser; // 当前登录的商家

    public MerchantController(MerchantView view, User user) {
        this.view = view;
        this.currentUser = user;
        this.productDAO = new ProductDAO();

        // 设置欢迎语
        view.setWelcomeMessage("Merchant Dashboard - Logged in as: " + user.getUsername());

        // 绑定按钮事件
        view.addAddListener(new AddAction());
        view.addEditListener(new EditAction());
        view.addDeleteListener(new DeleteAction());
        view.addRefreshListener(e -> loadProductData());
        view.addLogoutListener(new LogoutAction()); //退出功能

        // 初始化显示界面并加载数据
        view.setVisible(true);
        loadProductData();
    }

    // 加载表格数据
    private void loadProductData() {
        //只加载当前 merchantId 的数据 用户区分不同商家各自的商品
        Vector<Vector<Object>> data = productDAO.getProductsByMerchant(currentUser.getUserId());
        view.setTableData(data);
    }

    // 内部类：处理添加商品
    class AddAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 弹出简单的输入框 (为了简化，未使用复杂的新弹窗类，建议作业中可以使用 JDialog 封装)
            JTextField nameField = new JTextField();
            JTextField descField = new JTextField();
            JTextField priceField = new JTextField();
            JTextField stockField = new JTextField();

            Object[] message = {
                    "Name:", nameField,
                    "Description:", descField,
                    "Price:", priceField,
                    "Stock:", stockField
            };

            int option = JOptionPane.showConfirmDialog(view, message, "Add New Product", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText();
                    String desc = descField.getText();
                    double price = Double.parseDouble(priceField.getText());
                    int stock = Integer.parseInt(stockField.getText());

                    // 创建对象，将商户id传进去
                    Product p = new Product(0, name, desc, price, stock, currentUser.getUserId());

                    if (productDAO.addProduct(p)) {
                        JOptionPane.showMessageDialog(view, "Product Added Successfully!");
                        loadProductData(); // 刷新表格
                    } else {
                        JOptionPane.showMessageDialog(view, "Failed to add product.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(view, "Invalid Price or Stock format!");
                }
            }
        }
    }

    //处理编辑商品
    class EditAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = view.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Please select a product to edit.");
                return;
            }

            // 获取选中行的当前值
            int id = (int) view.getValueAt(selectedRow, 0);
            String currentName = (String) view.getValueAt(selectedRow, 1);
            String currentDesc = (String) view.getValueAt(selectedRow, 2);
            double currentPrice = (double) view.getValueAt(selectedRow, 3);
            int currentStock = (int) view.getValueAt(selectedRow, 4);

            // 预填充输入框
            JTextField nameField = new JTextField(currentName);
            JTextField descField = new JTextField(currentDesc);
            JTextField priceField = new JTextField(String.valueOf(currentPrice));
            JTextField stockField = new JTextField(String.valueOf(currentStock));

            Object[] message = {
                    "Name:", nameField, "Description:", descField,
                    "Price:", priceField, "Stock:", stockField
            };

            int option = JOptionPane.showConfirmDialog(view, message, "Edit Product", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                try {
                    Product p = new Product(id, nameField.getText(), descField.getText(),
                            Double.parseDouble(priceField.getText()),
                            Integer.parseInt(stockField.getText()),
                            currentUser.getUserId()); // 保持 merchantId 不变

                    if (productDAO.updateProduct(p)) {
                        JOptionPane.showMessageDialog(view, "Product Updated!");
                        loadProductData();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Update Failed: " + ex.getMessage());
                }
            }
        }
    }

    //处理删除商品
    class DeleteAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = view.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "Please select a product to delete.");
                return;
            }

            int id = (int) view.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(view, "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // 调用 DAO，同时传入 merchantId 进行双重验证
                if (productDAO.deleteProduct(id, currentUser.getUserId())) {
                    loadProductData();
                } else {
                    JOptionPane.showMessageDialog(view, "Delete Failed.");
                }
            }
        }
    }

    // 处理退出登录
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
                // 2. 关闭当前商家窗口
                view.dispose();

                // 3. 重新打开登录界面
                LoginView loginView = new LoginView();
                new LoginController(loginView);
                loginView.setVisible(true);
            }
        }
    }
}