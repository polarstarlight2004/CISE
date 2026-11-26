package com.shopping.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class AdminView extends JFrame {
    
    private JButton btnLogout;
    private JTabbedPane tabbedPane;

    // Tab 1: 用户管理
    private JTable userTable;
    private DefaultTableModel userModel;
    private JButton btnRefreshUsers;
    private JButton btnDeleteUser;

    // Tab 2: 商品管理
    private JTable prodTable;
    private DefaultTableModel prodModel;
    private JButton btnRefreshProds;
    private JButton btnDeleteProd;

    public AdminView() {
        super("Online Shopping System - ADMINISTRATOR");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // 顶部
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel(" System Administration Console ");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.RED);
        btnLogout = new JButton("Logout");
        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(btnLogout, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        createUserTab();
        createProductTab();
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void createUserTab() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"User ID", "Username", "Role", "Balance"};
        userModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(userModel);
        
        JPanel btnPanel = new JPanel();
        btnRefreshUsers = new JButton("Refresh Users");
        btnDeleteUser = new JButton("Delete Selected User");
        btnPanel.add(btnRefreshUsers);
        btnPanel.add(btnDeleteUser);

        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("User Management", panel);
    }

    private void createProductTab() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"ID", "Name", "Desc", "Price", "Stock", "Merchant ID"};
        prodModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        prodTable = new JTable(prodModel);

        JPanel btnPanel = new JPanel();
        btnRefreshProds = new JButton("Refresh Products");
        btnDeleteProd = new JButton("Delete Selected Product");
        btnPanel.add(btnRefreshProds);
        btnPanel.add(btnDeleteProd);

        panel.add(new JScrollPane(prodTable), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Product Management", panel);
    }

    // Getters and Listeners
    public JTable getUserTable() { return userTable; }
    public DefaultTableModel getUserModel() { return userModel; }
    public JTable getProdTable() { return prodTable; }
    public DefaultTableModel getProdModel() { return prodModel; }

    public void addLogoutListener(ActionListener l) { btnLogout.addActionListener(l); }
    public void addUserActionsListener(ActionListener refresh, ActionListener delete) {
        btnRefreshUsers.addActionListener(refresh);
        btnDeleteUser.addActionListener(delete);
    }
    public void addProdActionsListener(ActionListener refresh, ActionListener delete) {
        btnRefreshProds.addActionListener(refresh);
        btnDeleteProd.addActionListener(delete);
    }
}