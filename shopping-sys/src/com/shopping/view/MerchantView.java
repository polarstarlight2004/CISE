package com.shopping.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Vector;

public class MerchantView extends JFrame {
    
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JLabel lblWelcome;
    private JButton btnLogout;

    public MerchantView() {
        super("Merchant Dashboard");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 实际项目中可能改为 DISPOSE 并返回登录页
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // 顶部欢迎栏
        //  修改顶部区域 
        JPanel topPanel = new JPanel(new BorderLayout());

        lblWelcome = new JLabel("Welcome, Merchant!");
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 初始化 Logout 按钮
        btnLogout = new JButton("Logout");

        topPanel.add(lblWelcome, BorderLayout.WEST); // 欢迎语在左
        topPanel.add(btnLogout, BorderLayout.EAST);  // 退出按钮在右

        add(topPanel, BorderLayout.NORTH); // 把整个顶部面板加入窗口

        // 主体：选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();
        
        //  Tab 1: 商品管理 
        JPanel productPanel = new JPanel(new BorderLayout());
        
        // 表格设置
        String[] columnNames = {"ID", "Name", "Description", "Price", "Stock"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override // 禁止直接编辑单元格，强制使用按钮
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        // 按钮栏
        JPanel btnPanel = new JPanel();
        btnAdd = new JButton("Add Product");
        btnEdit = new JButton("Edit Product");
        btnDelete = new JButton("Delete Product");
        btnRefresh = new JButton("Refresh List");
        
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);
        productPanel.add(btnPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Product Management", productPanel);
        
        //  Tab 2: 订单管理 (预留) 
        JPanel orderPanel = new JPanel();
        orderPanel.add(new JLabel("Order History will be displayed here."));
        tabbedPane.addTab("Sales Orders", orderPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    // 数据更新方法 
    public void setTableData(Vector<Vector<Object>> data) {
        tableModel.setRowCount(0); // 清空旧数据
        for (Vector<Object> row : data) {
            tableModel.addRow(row);
        }
    }

    //  退出按钮事件 
    public void addLogoutListener(ActionListener l) {
        btnLogout.addActionListener(l);
    }

    public void setWelcomeMessage(String msg) {
        lblWelcome.setText(msg);
    }

    //  获取选中的行数据 
    public int getSelectedRow() {
        return productTable.getSelectedRow();
    }

    public Object getValueAt(int row, int col) {
        return productTable.getValueAt(row, col);
    }

    //  事件监听 
    public void addAddListener(ActionListener l) { btnAdd.addActionListener(l); }
    public void addEditListener(ActionListener l) { btnEdit.addActionListener(l); }
    public void addDeleteListener(ActionListener l) { btnDelete.addActionListener(l); }
    public void addRefreshListener(ActionListener l) { btnRefresh.addActionListener(l); }
}