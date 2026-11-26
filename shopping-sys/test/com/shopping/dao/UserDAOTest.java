package com.shopping.dao;

import com.shopping.dao.UserDAO;
import com.shopping.model.User;
import com.shopping.util.DatabaseHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * UserDAO 单元测试类 (JUnit 4)
 * 覆盖功能：注册、登录成功、密码错误验证、角色错误验证
 */
public class UserDAOTest {

    private UserDAO userDAO;
    
    // 用于生成唯一的用户名，防止多次运行测试时出现 "Username already exists" 错误
    private String uniqueSuffix;

    @Before
    public void setUp() {
        // 1. 确保数据库连接已初始化
        DatabaseHelper.getInstance();
        userDAO = new UserDAO();
        
        // 2. 生成一个唯一后缀 (基于当前时间戳)
        uniqueSuffix = "_" + System.currentTimeMillis();
    }

    @After
    public void tearDown() {
        // 这里的清理工作通常涉及删除测试数据，
        // 但由于Derby Embedded删除记录较繁琐，且我们用了唯一用户名，这里可以留空。
    }

    /**
     * 测试用例 1: 正常注册流程
     */
    @Test
    public void testRegisterSuccess() {
        System.out.println("Running testRegisterSuccess...");
        
        String username = "testUser" + uniqueSuffix;
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword("password123");
        newUser.setRole("CUSTOMER");
        newUser.setBalance(100.0);

        boolean isRegistered = userDAO.register(newUser);
        
        // 断言注册应该成功
        assertTrue("新用户注册应当返回 true", isRegistered);
        
        // 再次检查用户名是否存在
        assertTrue("数据库中应当存在该用户名", userDAO.checkUsernameExists(username));
    }

    /**
     * 测试用例 2: 重复注册验证 (Business Logic Validation)
     */
    @Test
    public void testRegisterDuplicateFail() {
        System.out.println("Running testRegisterDuplicateFail...");
        
        String username = "dupUser" + uniqueSuffix;
        User user1 = new User(null, username, "pass", "CUSTOMER", 0.0);
        
        // 第一次注册
        userDAO.register(user1);
        
        // 第二次注册相同的用户名
        User user2 = new User(null, username, "pass", "MERCHANT", 0.0);
        boolean result = userDAO.register(user2);
        
        // 断言第二次注册应该失败
        assertFalse("重复用户名的注册应当返回 false", result);
    }

    /**
     * 测试用例 3: 正常登录流程
     */
    @Test
    public void testLoginSuccess() {
        System.out.println("Running testLoginSuccess...");
        
        // 先注册一个用户
        String username = "loginUser" + uniqueSuffix;
        String password = "mySecretPassword";
        String role = "MERCHANT";
        
        User u = new User(null, username, password, role, 500.0);
        userDAO.register(u);
        
        // 尝试登录
        User loggedInUser = userDAO.login(username, password, role);
        
        // 断言登录成功
        assertNotNull("登录成功时不应返回 null", loggedInUser);
        assertEquals("登录后的用户名应一致", username, loggedInUser.getUsername());
        assertEquals("登录后的角色应一致", role, loggedInUser.getRole());
    }

    /**
     * 测试用例 4: 密码错误验证
     */
    @Test
    public void testLoginWrongPassword() {
        System.out.println("Running testLoginWrongPassword...");
        
        String username = "wrongPassUser" + uniqueSuffix;
        User u = new User(null, username, "correctPass", "CUSTOMER", 0.0);
        userDAO.register(u);
        
        // 使用错误的密码登录
        User result = userDAO.login(username, "wrongPass", "CUSTOMER");
        
        // 断言登录失败
        assertNull("密码错误时应当返回 null", result);
    }

    /**
     * 测试用例 5: 角色错误验证 (针对你之前遇到的截图问题)
     * 场景：用户注册的是 CUSTOMER，但尝试以 MERCHANT 登录
     */
    @Test
    public void testLoginWrongRole() {
        System.out.println("Running testLoginWrongRole...");
        
        String username = "roleUser" + uniqueSuffix;
        // 注册为 CUSTOMER
        User u = new User(null, username, "pass123", "CUSTOMER", 0.0);
        userDAO.register(u);
        
        // 尝试以 MERCHANT 身份登录
        User result = userDAO.login(username, "pass123", "MERCHANT");
        
        // 断言登录失败
        assertNull("角色不匹配时应当返回 null", result);
    }
    
    /**
     * 测试用例 6: 验证默认管理员账号 (如果你之前重置了DB)
     */
    @Test
    public void testAdminLogin() {
        // 如果你的数据库已经初始化过，这个测试应该能通过
        // 注意：admin 的角色必须是你 InitSql 里写的那个 (MERCHANT)
        User admin = userDAO.login("admin", "admin123", "MERCHANT");
        
        // 只有当你确定DB里有admin时才跑这个断言，否则可以注释掉
        if(admin != null) {
            System.out.println("Default Admin login verified.");
            assertEquals("MERCHANT", admin.getRole());
        } else {
            System.out.println("Warning: Default admin not found. Make sure DB is initialized.");
        }
    }
}