package com.shopping.controller;

import com.shopping.dao.*;
import com.shopping.model.*;
import com.shopping.view.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Map;
import java.util.Vector;

public class CustomerController {
    
    private CustomerView view;
    private User currentUser;
    private ShoppingCart cart;
    
    // DAO
    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private UserDAO userDAO;
    private CartDAO cartDAO;

    public CustomerController(CustomerView view, User user) {
        this.view = view;
        this.currentUser = user;
        this.cart = new ShoppingCart(); // 初始化内存购物车
        
        // 初始化 DAO
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        this.userDAO = new UserDAO();
        this.cartDAO = new CartDAO();

        // 1. 初始化数据展示
        reloadUserData(); // 刷新余额
        loadCartFromDB(); // 加载购物车数据
        loadShopData();   // 刷新商品
        loadHistoryData(); // 刷新历史

        // 2. 绑定所有事件
        initController();
        
        view.setVisible(true);
    }

    private void loadCartFromDB() {
        Map<Product, Integer> dbItems = cartDAO.loadCart(currentUser.getUserId());
        for (Map.Entry<Product, Integer> entry : dbItems.entrySet()) {
            // 将数据库查出来的数据，塞回内存的 ShoppingCart 对象中
            cart.addProduct(entry.getKey(), entry.getValue());
        }
        refreshCartView(); // 刷新界面
    }
    private void initController() {
        view.addLogoutListener(e -> logout());
        view.addRefreshShopListener(e -> loadShopData());
        view.addRefreshHistoryListener(e -> loadHistoryData());
        
        view.addAddToCartListener(new AddToCartAction());
        view.addRemoveCartListener(new RemoveCartAction());
        view.addCheckoutListener(new CheckoutAction());
        view.addRechargeListener(new RechargeAction());
        view.addUpdateInfoListener(new UpdateInfoAction());
        view.addViewDetailsListener(new ViewDetailsAction());
        view.addLogoutListener(new CustomerController.LogoutAction()); //退出功能
    }

    // --- 数据加载方法 ---

    // 重新从数据库拉取用户信息 (用于充值或消费后更新余额显示)
    private void reloadUserData() {
        User freshUser = userDAO.getUserById(currentUser.getUserId());
        if (freshUser != null) {
            this.currentUser = freshUser;
            view.setBalanceText(currentUser.getBalance());
        }
    }

    private void loadShopData() {
        Vector<Vector<Object>> data = productDAO.getAllAvailableProducts();
        view.getShopModel().setRowCount(0);
        for (Vector<Object> row : data) {
            view.getShopModel().addRow(row);
        }
    }

    private void refreshCartView() {
        view.getCartModel().setRowCount(0);
        for (Map.Entry<Product, Integer> entry : cart.getItems().entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            double subtotal = p.getPrice() * qty;
            
            view.getCartModel().addRow(new Object[]{
                p.getProductId(), p.getName(), p.getPrice(), qty, subtotal
            });
        }
        view.setCartTotalText(cart.getTotalPrice());
    }
    
    private void loadHistoryData() {
        Vector<Vector<Object>> data = orderDAO.getOrdersByUser(currentUser.getUserId());
        view.getHistoryModel().setRowCount(0);
        for(Vector<Object> row : data) {
            view.getHistoryModel().addRow(row);
        }
    }

    private void logout() {
        view.dispose();
        new LoginController(new LoginView());
    }

    // --- 内部类：具体逻辑实现 ---

    // 功能 b: 加入购物车
    class AddToCartAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = view.getShopTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Please select a product first.");
                return;
            }

            // 从表格获取数据还原 Product 对象
            int id = (int) view.getShopTable().getValueAt(row, 0);
            String name = (String) view.getShopTable().getValueAt(row, 1);
            String desc = (String) view.getShopTable().getValueAt(row, 2);
            double price = (double) view.getShopTable().getValueAt(row, 3);
            int stock = (int) view.getShopTable().getValueAt(row, 4);
            int merchantId = (int) view.getShopTable().getValueAt(row, 5);

            String input = JOptionPane.showInputDialog(view, "Enter Quantity (Max " + stock + "):");
            if (input == null || input.isEmpty()) return;

            try {
                int qty = Integer.parseInt(input);
                if (qty <= 0 || qty > stock) {
                    JOptionPane.showMessageDialog(view, "Invalid quantity!");
                    return;
                }

                //内存gengxin
                Product p = new Product(id, name, desc, price, stock, merchantId);
                cart.addProduct(p, qty);

                //数据库更新
                cartDAO.addOrUpdateItem(currentUser.getUserId(), id, qty);

                refreshCartView();
                JOptionPane.showMessageDialog(view, "Added to Cart!");
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Please enter a valid number.");
            }
        }
    }
    
    // 功能 b: 移除购物车商品
    class RemoveCartAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = view.getCartTable().getSelectedRow();
            if (row == -1) return;
            
            int productId = (int) view.getCartTable().getValueAt(row, 0);
            
            // 找到对应的 Product 对象进行移除
            Product target = null;
            for(Product p : cart.getItems().keySet()) {
                if(p.getProductId() == productId) { target = p; break; }
            }
            if(target != null) {
                cart.removeProduct(target);

                //数据库同步：从 DB 删除
                cartDAO.removeItem(currentUser.getUserId(), productId);

                refreshCartView();
            }
        }
    }

    // 功能 g: 支付结算 (最重要的功能)
    class CheckoutAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (cart.getItemCount() == 0) {
                JOptionPane.showMessageDialog(view, "Cart is empty!");
                return;
            }

            double total = cart.getTotalPrice();
            
            // 1. 本地余额校验
            if (currentUser.getBalance() < total) {
                JOptionPane.showMessageDialog(view, "Insufficient Balance! Please Recharge.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(view, 
                "Total: $" + total + "\nConfirm Payment?", "Checkout", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // 调用 Transactional DAO
                    boolean success = orderDAO.checkout(currentUser, cart.getItems(), total);
                    if (success) {
                        JOptionPane.showMessageDialog(view, "Payment Successful! Order Created.");

                        // 内存清空
                        cart.clear();

                        //数据库同步：清空 DB 购物车
                        cartDAO.clearCart(currentUser.getUserId());

                        // 支付成功后的联动：
                        refreshCartView();  // 2. 清空界面购物车
                        reloadUserData();   // 3. 刷新余额 (钱扣了)
                        loadShopData();     // 4. 刷新商城 (库存减了)
                        loadHistoryData();  // 5. 刷新订单历史 (新订单有了)
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(view, "Checkout Failed: " + ex.getMessage());
                }
            }
        }
    }

    // 功能 c: 充值
    class RechargeAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                double amount = Double.parseDouble(view.getRechargeAmount());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(view, "Invalid amount.");
                    return;
                }
                
                // 更新内存对象
                double newBalance = currentUser.getBalance() + amount;
                currentUser.setBalance(newBalance);
                
                // 更新数据库
                if (userDAO.updateUser(currentUser)) {
                    JOptionPane.showMessageDialog(view, "Recharge Successful!");
                    reloadUserData(); // 刷新显示
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Please enter a valid number.");
            }
        }
    }

    // 功能 e: 修改信息
    class UpdateInfoAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String newPass = view.getNewPassword();
            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Password cannot be empty.");
                return;
            }
            currentUser.setPassword(newPass);
            if (userDAO.updateUser(currentUser)) {
                JOptionPane.showMessageDialog(view, "Password Updated!");
            }
        }
    }

    // 功能 h: 退出登录
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
                // 2. 关闭当前客户窗口
                view.dispose();

                // 3. 重新打开登录界面
                LoginView loginView = new LoginView();
                new LoginController(loginView);
                loginView.setVisible(true);
            }
        }
    }


    // 功能 f: 查看订单详情 (功能 g)
    class ViewDetailsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 1. 检查是否有选中行
            int row = view.getHistoryTable().getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Please select an order to view details.");
                return;
            }

            // 2. 获取订单ID (假设第一列是 Order ID)
            int orderId = (int) view.getHistoryTable().getValueAt(row, 0);

            // 3. 从 DAO 获取该订单的商品列表
            Vector<Vector<Object>> details = orderDAO.getOrderDetails(orderId);

            // 4. 构建一个临时的表格来显示详情
            String[] columns = {"Product Name", "Quantity", "Unit Price", "Subtotal"};
            JTable detailTable = new JTable(new javax.swing.table.DefaultTableModel(details, new Vector<>(java.util.Arrays.asList(columns))));

            // 设置表格不可编辑
            detailTable.setEnabled(false);

            // 5. 将表格放入滚动面板，并作为弹窗的内容显示
            JScrollPane scrollPane = new JScrollPane(detailTable);
            scrollPane.setPreferredSize(new java.awt.Dimension(500, 300)); // 设置弹窗大小

            JOptionPane.showMessageDialog(view,
                    scrollPane,
                    "Order Details (Order ID: " + orderId + ")",
                    JOptionPane.PLAIN_MESSAGE);
        }
    }
}