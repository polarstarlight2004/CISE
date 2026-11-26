package com.shopping.dao;

import com.shopping.model.Product;
import com.shopping.model.User;
import com.shopping.sql.BusinessSql;
import com.shopping.util.DatabaseHelper;

import java.sql.*;
import java.util.Map;
import java.util.Vector;

/**
 * 订单数据访问对象
 * 负责处理：客户下单、客户查历史订单、商家查销售记录
 */
public class OrderDAO {

    private Connection conn;

    public OrderDAO() {
        conn = DatabaseHelper.getInstance().getConnection();
    }

    /**
     * 核心功能：客户结账 (Checkout)
     * 使用 事务 (Transaction) 保证数据一致性。
     * * @param user        当前购买的用户
     * @param cartItems   购物车内容 (商品 -> 数量)
     * @param totalAmount 订单总金额
     * @return boolean    是否支付成功
     * @throws SQLException 如果中间发生任何错误（库存不足、余额不足等）抛出异常
     */
    //一定要保证支付功能的原子性，有任何一步错误都要回滚所有操作
    public boolean checkout(User user, Map<Product, Integer> cartItems, double totalAmount) throws SQLException {
        boolean success = false;
        
        try {
            // 关闭自动提交，开启事务
            conn.setAutoCommit(false);
            //  扣除用户余额
            try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.DEDUCT_USER_BALANCE_SQL)) {
                pstmt.setDouble(1, totalAmount);
                pstmt.setInt(2, user.getUserId());
                int rows = pstmt.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Balance update failed (User ID not found).");
                }
            }

            // 创建主订单记录 (ORDERS 表)
            int orderId = -1;
            // Statement.RETURN_GENERATED_KEYS 用于获取数据库自动生成的 order_id
            try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.CREATE_ORDER_SQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, user.getUserId());
                pstmt.setDouble(2, totalAmount);
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1); // 获取生成的 ID
                    } else {
                        throw new SQLException("Failed to create order, no ID obtained.");
                    }
                }
            }

            // 处理购物车中的每件商品 (循环)
            // 1. 扣减库存
            // 2. 插入订单详情 (ORDER_ITEMS 表)
            //String itemSql = "INSERT INTO ORDER_ITEMS (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
            //String stockSql = "UPDATE PRODUCTS SET stock = stock - ? WHERE product_id = ? AND stock >= ?";  stock >= ? 防止超卖

            try (PreparedStatement itemStmt = conn.prepareStatement(BusinessSql.INSERT_ORDER_ITEM_SQL);
                 PreparedStatement stockStmt = conn.prepareStatement(BusinessSql.UPDATE_PRODUCT_STOCK_SQL)) {

                for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
                    Product p = entry.getKey();
                    int qty = entry.getValue();

                    // 扣减库存
                    stockStmt.setInt(1, qty);
                    stockStmt.setInt(2, p.getProductId());
                    stockStmt.setInt(3, qty); // WHERE stock >= qty
                    int stockRows = stockStmt.executeUpdate();
                    
                    if (stockRows == 0) {
                        // 如果受影响行数为0，说明库存不足或者商品不存在
                        throw new SQLException("Insufficient stock for product: " + p.getName());
                    }

                    //  插入订单项
                    itemStmt.setInt(1, orderId);
                    itemStmt.setInt(2, p.getProductId());
                    itemStmt.setInt(3, qty);
                    itemStmt.setDouble(4, p.getPrice());
                    itemStmt.addBatch(); // 使用批处理优化性能
                }
                
                itemStmt.executeBatch(); // 执行所有插入
            }


            //提交事务
            conn.commit();
            success = true;
            System.out.println("Checkout transaction completed successfully.");

        } catch (SQLException e) {
            // 如果果发生任何异常，回滚所有操作
            // 哪怕余额扣成功了，如果库存扣失败，钱也会退回去
            if (conn != null) {
                try {
                    System.err.println("Transaction failed, rolling back. Reason: " + e.getMessage());
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e; // 重新抛出异常，让 Controller 捕获并提示用户
        } finally {
            // 恢复自动提交状态
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
        
        return success;
    }

    /**
     * 客户功能：查询自己的历史订单
     * @param userId 客户ID
     * @return  Vector
     */
    public Vector<Vector<Object>> getOrdersByUser(int userId) {
        Vector<Vector<Object>> data = new Vector<>();
        // 聚合查询，显示订单概览
        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.GET_USER_ORDERS_SQL)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("order_id"));
                    row.add(rs.getTimestamp("order_date"));
                    row.add(rs.getDouble("total_amount"));
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    /**
     * 客户功能：查询某个具体订单的详情
     */
    public Vector<Vector<Object>> getOrderDetails(int orderId) {
        Vector<Vector<Object>> data = new Vector<>();

        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.GET_ORDER_DETAILS_SQL)) {
            pstmt.setInt(1, orderId);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Vector<Object> row = new java.util.Vector<>();
                    row.add(rs.getString("name"));              // 商品名
                    row.add(rs.getInt("quantity"));             // 数量
                    row.add(rs.getDouble("price_at_purchase")); // 购买时单价
                    row.add(rs.getDouble("subtotal"));          // 小计
                    data.add(row);
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 商家功能：查询属于该商家的销售记录
     * 注意：使用了多表联查，只筛选该商家的商品
     * @param merchantId 商家ID
     * @return 数据
     */
    public Vector<Vector<Object>> getMerchantSales(int merchantId) {
        Vector<Vector<Object>> data = new Vector<>();

        // 多表联查，筛选该商家的商品销售记录
        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.GET_MERCHANT_SALES_SQL)) {
            pstmt.setInt(1, merchantId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("order_id"));
                    row.add(rs.getTimestamp("order_date")); // 订单时间
                    row.add(rs.getString("name"));          // 商品名
                    row.add(rs.getInt("quantity"));         // 卖出数量
                    row.add(rs.getDouble("price_at_purchase")); // 单价
                    row.add(rs.getString("username"));      // 买家用户名
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
}