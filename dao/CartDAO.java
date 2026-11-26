package com.shopping.dao;

import com.shopping.model.Product;
import com.shopping.util.DatabaseHelper;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class CartDAO {
    
    private Connection conn;

    public CartDAO() {
        conn = DatabaseHelper.getInstance().getConnection();
    }

    /**
     * 登录时调用：加载该用户之前的购物车数据
     */
    public Map<Product, Integer> loadCart(int userId) {
        Map<Product, Integer> items = new HashMap<>();
        String sql = "SELECT p.*, c.quantity " +
                     "FROM CART_ITEMS c " +
                     "JOIN PRODUCTS p ON c.product_id = p.product_id " +
                     "WHERE c.user_id = ?";
                     
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 还原 Product 对象
                    Product p = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getInt("merchant_id")
                    );
                    int qty = rs.getInt("quantity");
                    items.put(p, qty);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * 添加或更新购物车商品
     * 逻辑：如果数据库里有了，就 Update 数量；如果没有，就 Insert。
     */
    public void addOrUpdateItem(int userId, int productId, int quantity) {
        try {
            // 1. 先检查是否存在
            if (exists(userId, productId)) {
                // 2. 如果存在，更新数量 (原有数量 + 新增数量)
                String updateSql = "UPDATE CART_ITEMS SET quantity = quantity + ? WHERE user_id = ? AND product_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setInt(1, quantity);
                    pstmt.setInt(2, userId);
                    pstmt.setInt(3, productId);
                    pstmt.executeUpdate();
                }
            } else {
                // 3. 如果不存在，直接插入
                String insertSql = "INSERT INTO CART_ITEMS (user_id, product_id, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setInt(1, userId);
                    pstmt.setInt(2, productId);
                    pstmt.setInt(3, quantity);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 移除商品
     */
    public void removeItem(int userId, int productId) {
        String sql = "DELETE FROM CART_ITEMS WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空购物车 (结账成功后调用)
     */
    public void clearCart(int userId) {
        String sql = "DELETE FROM CART_ITEMS WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 辅助方法：检查记录是否存在
    private boolean exists(int userId, int productId) throws SQLException {
        String sql = "SELECT 1 FROM CART_ITEMS WHERE user_id = ? AND product_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}