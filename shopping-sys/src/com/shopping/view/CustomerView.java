package com.shopping.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class CustomerView extends JFrame {
    
    // --- 顶部组件 ---
    private JLabel lblBalance; // 功能 d: 展示余额
    private JButton btnLogout;

    // --- Tab组件 ---
    private JTabbedPane tabbedPane;

    // Tab 1: 商城 (功能 a)
    private JTable shopTable;
    private DefaultTableModel shopModel;
    private JButton btnAddToCart;
    private JButton btnRefreshShop;

    // Tab 2: 购物车 (功能 b, g)
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel lblCartTotal;
    private JButton btnRemoveFromCart;
    private JButton btnCheckout; // 最重要的支付

    // Tab 3: 个人中心 (功能 c, e)
    private JTextField txtPassword;
    private JTextField txtRechargeAmount;
    private JButton btnUpdateInfo;
    private JButton btnRecharge;

    // Tab 4: 历史订单 (功能 f)
    private JTable historyTable;
    private DefaultTableModel historyModel;
    private JButton btnRefreshHistory;
    private JButton btnViewOrderDetails;

    public CustomerView() {
        super("Online Shopping System - Customer");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        // 1. 顶部面板 (余额 + 退出)
        JPanel topPanel = new JPanel(new BorderLayout());
        lblBalance = new JLabel(" Balance: $0.00 ");
        lblBalance.setFont(new Font("Arial", Font.BOLD, 16));
        lblBalance.setForeground(new Color(0, 100, 0)); // 深绿色显示钱
        
        btnLogout = new JButton("Logout");
        
        topPanel.add(lblBalance, BorderLayout.WEST);
        topPanel.add(btnLogout, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. 主体 Tabs
        tabbedPane = new JTabbedPane();
        createShopTab();    // a. 商城
        createCartTab();    // b. 购物车
        createProfileTab(); // c, e. 个人中心
        createHistoryTab(); // f. 历史
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- Tab 1: 商城界面 ---
    private void createShopTab() {
        JPanel panel = new JPanel(new BorderLayout());
        // 列名: ID, Name, Desc, Price, Stock, MerchantID(隐藏或显示均可)
        String[] cols = {"ID", "Name", "Desc", "Price", "Stock", "MerchantID"};
        shopModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        shopTable = new JTable(shopModel);
        
        JPanel btnPanel = new JPanel();
        btnRefreshShop = new JButton("Refresh List");
        btnAddToCart = new JButton("Add to Cart");
        btnPanel.add(btnRefreshShop);
        btnPanel.add(btnAddToCart);
        
        panel.add(new JScrollPane(shopTable), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Shop Market", panel);
    }

    // --- Tab 2: 购物车界面 ---
    private void createCartTab() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"Product ID", "Name", "Unit Price", "Quantity", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0);
        cartTable = new JTable(cartModel);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblCartTotal = new JLabel("Total: $0.00   ");
        lblCartTotal.setFont(new Font("Arial", Font.BOLD, 14));
        
        btnRemoveFromCart = new JButton("Remove Item");
        btnCheckout = new JButton("Checkout (Pay)"); // 功能 g
        btnCheckout.setBackground(new Color(255, 69, 0)); // 橙红色按钮更显眼
        btnCheckout.setForeground(Color.WHITE);
        
        bottomPanel.add(lblCartTotal);
        bottomPanel.add(btnRemoveFromCart);
        bottomPanel.add(btnCheckout);
        
        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("My Cart", panel);
    }

    // --- Tab 3: 个人中心界面 ---
    private void createProfileTab() {
        JPanel panel = new JPanel(null); // 绝对布局简单点
        
        JLabel l1 = new JLabel("Change Password:");
        l1.setBounds(50, 50, 150, 25);
        txtPassword = new JTextField();
        txtPassword.setBounds(200, 50, 150, 25);
        btnUpdateInfo = new JButton("Update Password");
        btnUpdateInfo.setBounds(370, 50, 150, 25);
        
        JLabel l2 = new JLabel("Recharge Balance ($):");
        l2.setBounds(50, 100, 150, 25);
        txtRechargeAmount = new JTextField();
        txtRechargeAmount.setBounds(200, 100, 150, 25);
        btnRecharge = new JButton("Recharge");
        btnRecharge.setBounds(370, 100, 150, 25);
        
        panel.add(l1); panel.add(txtPassword); panel.add(btnUpdateInfo);
        panel.add(l2); panel.add(txtRechargeAmount); panel.add(btnRecharge);
        
        tabbedPane.addTab("My Profile", panel);
    }

    // --- Tab 4: 历史订单界面 ---
    private void createHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout());

        // 订单号, 时间, 总金额
        String[] cols = {"Order ID", "Date", "Total Amount"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(historyModel);

        JPanel btnPanel = new JPanel();
        btnRefreshHistory = new JButton("Refresh Orders");

        //查看详情按钮
        btnViewOrderDetails = new JButton("View Order Details");

        btnPanel.add(btnRefreshHistory);
        btnPanel.add(btnViewOrderDetails); // 加入面板

        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Order History", panel);
    }

    public void addViewDetailsListener(ActionListener l) {
        btnViewOrderDetails.addActionListener(l);
    }

    // 获取历史订单表格对象
    public JTable getHistoryTable() {
        return historyTable;
    }

    // --- Getters & Listeners (供 Controller 调用) ---
    public JTable getShopTable() { return shopTable; }
    public DefaultTableModel getShopModel() { return shopModel; }
    public DefaultTableModel getCartModel() { return cartModel; }
    public JTable getCartTable() { return cartTable; }
    public DefaultTableModel getHistoryModel() { return historyModel; }
    
    // 获取输入框内容
    public String getNewPassword() { return txtPassword.getText(); }
    public String getRechargeAmount() { return txtRechargeAmount.getText(); }
    
    // 更新显示
    public void setBalanceText(double balance) { 
        lblBalance.setText(" Balance: $" + String.format("%.2f", balance) + " "); 
    }
    public void setCartTotalText(double total) { 
        lblCartTotal.setText("Total: $" + String.format("%.2f", total) + "   "); 
    }

    // 绑定事件
    public void addLogoutListener(ActionListener l) { btnLogout.addActionListener(l); }
    public void addRefreshShopListener(ActionListener l) { btnRefreshShop.addActionListener(l); }
    public void addAddToCartListener(ActionListener l) { btnAddToCart.addActionListener(l); }
    public void addRemoveCartListener(ActionListener l) { btnRemoveFromCart.addActionListener(l); }
    public void addCheckoutListener(ActionListener l) { btnCheckout.addActionListener(l); }
    public void addUpdateInfoListener(ActionListener l) { btnUpdateInfo.addActionListener(l); }
    public void addRechargeListener(ActionListener l) { btnRecharge.addActionListener(l); }
    public void addRefreshHistoryListener(ActionListener l) { btnRefreshHistory.addActionListener(l); }
}