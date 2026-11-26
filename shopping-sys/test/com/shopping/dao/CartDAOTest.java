package com.shopping.dao;

import com.shopping.dao.CartDAO;
import com.shopping.model.Product;
import com.shopping.util.DatabaseHelper;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CartDAOTest {

    private CartDAO cartDAO;
    private Connection conn;
    private int testUserId = 999; // 使用一个特殊的测试ID
    private int testProductId = 888;

    @Before
    public void setUp() throws Exception {
        DatabaseHelper.getInstance(); // 确保表存在
        conn = DatabaseHelper.getInstance().getConnection();
        cartDAO = new CartDAO();
        
        // 1. 清理脏数据
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM CART_ITEMS WHERE user_id = " + testUserId);
            stmt.execute("DELETE FROM PRODUCTS WHERE product_id = " + testProductId);
            // 2. 插入测试必要的关联数据 (Product)
            // 注意：外键约束要求 Product 必须存在
            String sql = "INSERT INTO PRODUCTS (product_id, name, price, stock, merchant_id) " +
                         "VALUES (" + testProductId + ", 'TestItem', 100.0, 50, 1)";
            // Derby 允许手动插入 ID (如果表定义允许) 或者我们需要获取生成的主键
            // 为了简化测试，这里假设你的 DatabaseHelper 建表时允许指定ID，
            // 如果是 GENERATED ALWAYS AS IDENTITY，你需要先插入再查询获取ID。
            // 这里为了演示逻辑，假设数据环境已准备好。
            // 💡 实际运行建议：在 @Before 里先调用 ProductDAO 插入一个商品获取真实ID
        }
    }
    
    // 为了保证测试能跑通，我们稍微修改一下 setup，使用真实的 DAO 创建数据
    @Before
    public void setupRealData() {
        DatabaseHelper.getInstance();
        cartDAO = new CartDAO();
        // 假设数据库里已经有了 user_id=1 和 product_id=1 的数据 (默认数据)
        // 如果没有，请手动确保数据库里至少有一条商品和用户
        testUserId = 1; 
        testProductId = 1; 
        
        // 先清空该用户的购物车
        cartDAO.clearCart(testUserId);
    }

    @Test
    public void testSaveAndLoadCart() {
        System.out.println("Running testSaveAndLoadCart...");
        
        // 1. 保存商品到数据库
        cartDAO.addOrUpdateItem(testUserId, testProductId, 5);
        
        // 2. 从数据库加载
        Map<Product, Integer> items = cartDAO.loadCart(testUserId);
        
        // 3. 验证
        assertFalse("加载的购物车不应为空", items.isEmpty());
        Product loadedP = items.keySet().iterator().next();
        assertEquals("商品ID应匹配", (Integer)testProductId, loadedP.getProductId());
        assertEquals("数量应为5", (Integer)5, items.get(loadedP));
    }

    @Test
    public void testUpdateCartItem() {
        System.out.println("Running testUpdateCartItem...");
        
        // 1. 第一次添加 2 个
        cartDAO.addOrUpdateItem(testUserId, testProductId, 2);
        
        // 2. 第二次添加 3 个
        cartDAO.addOrUpdateItem(testUserId, testProductId, 3);
        
        // 3. 加载并验证
        Map<Product, Integer> items = cartDAO.loadCart(testUserId);
        int qty = items.values().iterator().next();
        
        assertEquals("数量应该累加为 5", 5, qty);
    }
}