package com.shopping.dao;

import com.shopping.model.Product;
import com.shopping.sql.BusinessSql;
import com.shopping.util.DatabaseHelper;
import java.sql.*;
import java.util.Vector;

/**
 * 商品数据访问对象
 */
public class ProductDAO {
    private Connection conn;

    public ProductDAO() {
        conn = DatabaseHelper.getInstance().getConnection();
    }

    // 添加商品
    public boolean addProduct(Product product) {
        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.ADD_PRODUCT_SQL)) {
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getStock());
            pstmt.setInt(5, product.getMerchantId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除商品
    public boolean deleteProduct(int productId, int merchantId) {
        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.DELETE_PRODUCT_SQL)) {
            pstmt.setInt(1, productId);
            pstmt.setInt(2, merchantId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新商品
    public boolean updateProduct(Product product) {
        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.UPDATE_PRODUCT_SQL)) {
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getStock());
            pstmt.setInt(5, product.getProductId());
            pstmt.setInt(6, product.getMerchantId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取特定商家的所有商品
    public Vector<Vector<Object>> getProductsByMerchant(int merchantId) {
        Vector<Vector<Object>> data = new Vector<>();
        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.GET_PRODUCTS_BY_MERCHANT_SQL)) {
            pstmt.setInt(1, merchantId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("product_id"));
                    row.add(rs.getString("name"));
                    row.add(rs.getString("description"));
                    row.add(rs.getDouble("price"));
                    row.add(rs.getInt("stock"));
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    //获取所有商品
    public Vector<Vector<Object>> getAllAvailableProducts() {
        Vector<Vector<Object>> data = new Vector<>();
        // 只显示有库存的商品
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BusinessSql.GET_ALL_PRODUCTS_SQL)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("product_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("description"));
                row.add(rs.getDouble("price"));
                row.add(rs.getInt("stock"));
                row.add(rs.getInt("merchant_id")); // 隐藏字段，用于逻辑处理
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    //管理员获取所有商品
    public java.util.Vector<java.util.Vector<Object>> getAllProductsForAdmin() {
        java.util.Vector<java.util.Vector<Object>> data = new java.util.Vector<>();

        // 使用刚才定义的 SQL
        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(com.shopping.sql.BusinessSql.ADMIN_GET_ALL_PRODUCTS)) {

            while (rs.next()) {
                java.util.Vector<Object> row = new java.util.Vector<>();
                row.add(rs.getInt("product_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("description"));
                row.add(rs.getDouble("price"));
                row.add(rs.getInt("stock"));
                row.add(rs.getInt("merchant_id"));
                data.add(row);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean adminDeleteProduct(int productId) {
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(com.shopping.sql.BusinessSql.ADMIN_DELETE_PRODUCT)) {
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}