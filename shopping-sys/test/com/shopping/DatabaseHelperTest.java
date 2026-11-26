package com.shopping;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.shopping.util.DatabaseHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DatabaseHelper 的 JUnit 4 单元测试
 * 目的：验证数据库能否连接，以及表是否自动生成
 */
public class DatabaseHelperTest {
    
    private DatabaseHelper dbHelper;
    private Connection connection;

    /**
     * @Before: 在每个测试方法执行前运行
     */
    @Before
    public void setUp() {
        // 获取单例实例
        dbHelper = DatabaseHelper.getInstance();
        connection = dbHelper.getConnection();
    }

    /**
     * @After: 在每个测试方法执行后运行
     * 注意：这里我们不关闭连接，因为 DatabaseHelper 是单例的。
     * 如果关闭了，后续测试可能无法获取连接。
     * 实际项目中通常每个测试会回滚事务，但针对 Derby Embedded 简单测试即可。
     */
    @After
    public void tearDown() {
        // 这里留空，或者可以做一些数据清理工作
    }

    /**
     * 测试用例 1: 验证单例模式和连接状态
     * 对应评分标准：Database connection
     */
    @Test
    public void testConnectionIsNotNull() throws SQLException {
        System.out.println("Running testConnectionIsNotNull...");
        
        assertNotNull("DatabaseHelper 实例不应为空", dbHelper);
        assertNotNull("数据库连接对象不应为空", connection);
        assertFalse("数据库连接应该是开启状态", connection.isClosed());
        
        // 验证单例特性：两次获取应该是同一个对象
        DatabaseHelper anotherInstance = DatabaseHelper.getInstance();
        assertSame("DatabaseHelper 应该是单例的", dbHelper, anotherInstance);
    }

    /**
     * 测试用例 2: 验证表格是否自动创建 (USERS, PRODUCTS, ORDERS)
     * 对应评分标准：DB should be set up automatically
     */
    @Test
    public void testTablesExist() throws SQLException {
        System.out.println("Running testTablesExist...");
        
        DatabaseMetaData metaData = connection.getMetaData();
        
        // 检查 USERS 表是否存在
        // Derby 存储表名通常是大写的，所以使用 "USERS"
        ResultSet rsUsers = metaData.getTables(null, null, "USERS", null);
        assertTrue("USERS 表应该被自动创建", rsUsers.next());
        
        // 检查 PRODUCTS 表是否存在
        ResultSet rsProducts = metaData.getTables(null, null, "PRODUCTS", null);
        assertTrue("PRODUCTS 表应该被自动创建", rsProducts.next());
        
        // 检查 ORDERS 表是否存在
        ResultSet rsOrders = metaData.getTables(null, null, "ORDERS", null);
        assertTrue("ORDERS 表应该被自动创建", rsOrders.next());
    }

    /**
     * 测试用例 3: 验证默认管理员账号是否已插入
     * 对应评分标准：Database interactions (Input)
     */
    @Test
    public void testDefaultAdminExists() throws SQLException {
        System.out.println("Running testDefaultAdminExists...");
        
        String sql = "SELECT * FROM USERS WHERE username = 'admin'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            assertTrue("应该存在用户名为 'admin' 的记录", rs.next());
            assertEquals("管理员密码应为 admin123", "admin123", rs.getString("password"));
            assertEquals("管理员角色应为 MERCHANT", "MERCHANT", rs.getString("role"));
        }
    }
    
    /**
     * 测试用例 4: 简单的插入测试 (确保数据库不仅能读，还能写)
     * 对应评分标准：Can achieve database interactions
     */
    @Test
    public void testInsertProduct() throws SQLException {
        System.out.println("Running testInsertProduct...");
        
        String testName = "TestProduct_" + System.currentTimeMillis();
        String sql = String.format("INSERT INTO PRODUCTS (name, description, price, stock) VALUES ('%s', 'Test Desc', 99.9, 10)", testName);
        
        try (Statement stmt = connection.createStatement()) {
            int rowsAffected = stmt.executeUpdate(sql);
            assertEquals("应该成功插入 1 行数据", 1, rowsAffected);
            
            // 验证刚才插入的数据
            ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCTS WHERE name = '" + testName + "'");
            assertTrue("应该能查询到刚才插入的商品", rs.next());
            assertEquals(99.9, rs.getDouble("price"), 0.01);
        }
    }
}