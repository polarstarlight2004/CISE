package com.shopping.controller;

import com.shopping.constant.Constants;
import com.shopping.dao.UserDAO;
import com.shopping.model.User;
import com.shopping.util.DatabaseHelper;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoginSecurityTest {

    private UserDAO userDAO;

    @Before
    public void setUp() {
        DatabaseHelper.getInstance();
        userDAO = new UserDAO();
    }

    /**
     * 安全性测试 1: 验证系统中必须存在至少一个管理员账号
     * (防止系统无主)
     */
    @Test
    public void testDefaultAdminExists() {
        // 尝试登录默认管理员
        User admin = userDAO.login("admin", "1234", "ADMIN");
        
        assertNotNull("系统初始化后必须存在默认管理员账号 (admin)", admin);
        assertEquals("管理员角色必须正确", Constants.ROLE_ADMIN, admin.getRole());
    }

    /**
     * 安全性测试 2: 验证普通用户注册不会意外变成管理员
     */
    @Test
    public void testCustomerRegistrationRole() {
        String name = "JustUser_" + System.currentTimeMillis();
        User u = new User(null, name, "pass", Constants.ROLE_CUSTOMER, 0.0);
        
        userDAO.register(u);
        
        User dbUser = userDAO.login(name, "pass", Constants.ROLE_CUSTOMER);
        assertNotNull(dbUser);
        assertNotEquals("普通用户注册绝对不能变成管理员", Constants.ROLE_ADMIN, dbUser.getRole());
    }
    
    /**
     * 边界测试: 验证错误的凭证无法登录管理员
     */
    @Test
    public void testAdminLoginFail() {
        User u = userDAO.login("admin", "wrongpass", "ADMIN");
        assertNull("密码错误不应登录成功", u);
        
        User u2 = userDAO.login("admin", "admin123", "CUSTOMER");
        assertNull("角色不匹配不应登录成功 (防止越权)", u2);
    }
}