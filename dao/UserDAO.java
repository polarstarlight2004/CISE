package com.shopping.dao;

import com.shopping.model.User;
import com.shopping.sql.BusinessSql;
import com.shopping.util.DatabaseHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户数据访问对象
 */
public class UserDAO {

    private Connection conn;

    public UserDAO() {
        // 获取单例的数据库连接
        conn = DatabaseHelper.getInstance().getConnection();
    }

    /**
     * 用户登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param role     角色
     * @return 登录成功的 User 对象，失败返回 null
     */
    public User login(String username, String password, String role) {
        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.LOGIN_SQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));
                    user.setBalance(rs.getDouble("balance"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 用户注册
     *
     * @param user 用户对象
     * @return 是否注册成功
     */
    public boolean register(User user) {
        // 先检查用户名是否存在
        if (checkUsernameExists(user.getUsername())) {
            return false;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.REGISTER_SQL)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setDouble(4, user.getBalance()); // 初始余额通常为 0

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //检查用户名是否已存在
    public boolean checkUsernameExists(String username) {
        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.CHECK_USERNAME_EXISTS_SQL)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //个人界面修改个人信息
    public boolean updateUser(User user) {
        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.UPDATE_USER_SQL)) {
            pstmt.setString(1, user.getPassword());
            pstmt.setDouble(2, user.getBalance());
            pstmt.setInt(3, user.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 专门用于刷新用户最新数据（比如余额变动后）
    public User getUserById(int userId) {
        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.REFRESH_USER_BALANCE_SQL)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getDouble("balance")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 管理员获取所有用户列表 (排除管理员自己)
    public java.util.Vector<java.util.Vector<Object>> getAllUsers() {
        java.util.Vector<java.util.Vector<Object>> data = new java.util.Vector<>();
        try (java.sql.Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(BusinessSql.GET_ALL_USERS_SQL)) {
            while (rs.next()) {
                java.util.Vector<Object> row = new java.util.Vector<>();
                row.add(rs.getInt("user_id"));
                row.add(rs.getString("username"));
                row.add(rs.getString("role"));
                row.add(rs.getDouble("balance"));
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // 管理员 删除用户
    public boolean deleteUser(int userId) {
        try (PreparedStatement pstmt = conn.prepareStatement(BusinessSql.DELETE_USER_SQL)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}