package com.shopping.dao;

import com.shopping.dao.OrderDAO;
import com.shopping.dao.UserDAO;
import com.shopping.dao.ProductDAO;
import com.shopping.model.Product;
import com.shopping.model.User;
import com.shopping.util.DatabaseHelper;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class OrderDAOTest {

    private OrderDAO orderDAO;
    private UserDAO userDAO;
    private ProductDAO productDAO;
    
    private User buyer;
    private Product product;
    
    // 动态生成的用户名，防止冲突
    private String uniqueUser;

    @Before
    public void setUp() {
        DatabaseHelper.getInstance();
        orderDAO = new OrderDAO();
        userDAO = new UserDAO();
        productDAO = new ProductDAO();
        
        long ts = System.currentTimeMillis();
        uniqueUser = "Buyer_" + ts;
        
        // 1. 创建一个富有的买家 (余额 1000)
        User u = new User(null, uniqueUser, "pass", "CUSTOMER", 1000.0);
        userDAO.register(u);
        buyer = userDAO.login(uniqueUser, "pass", "CUSTOMER");
        
        // 2. 创建一个库存很少的商品 (库存只有 2，单价 100)
        // 假设商家ID是 1 (默认admin)
        Product p = new Product(0, "LimitedItem_" + ts, "Desc", 100.0, 2, 1); 
        productDAO.addProduct(p);
        
        // 获取刚才插入的商品的真实ID
        // (简单起见，通过遍历获取，实际项目应有更精确的方法)
        for(var row : productDAO.getProductsByMerchant(1)) {
            if(row.get(1).equals(p.getName())) {
                p.setProductId((Integer)row.get(0));
                break;
            }
        }
        product = p;
    }

    /**
     * 测试场景 1: 正常购买 (余额够，库存够)
     */
    @Test
    public void testCheckoutSuccess() throws SQLException {
        System.out.println("Running testCheckoutSuccess...");
        
        Map<Product, Integer> cart = new HashMap<>();
        cart.put(product, 1); // 买 1 个
        
        // 执行支付
        boolean result = orderDAO.checkout(buyer, cart, 100.0);
        
        assertTrue("支付应该成功", result);
        
        // 验证余额：应该剩 900
        User updatedUser = userDAO.getUserById(buyer.getUserId());
        assertEquals(900.0, updatedUser.getBalance(), 0.01);
    }

    /**
     * 测试场景 2: 事务回滚测试 (Transaction Rollback)
     * 场景：用户余额充足，但试图购买的数量超过库存。
     * 预期：虽然余额检查通过了，但扣库存时会失败，系统应触发回滚，用户的钱不能少。
     */
    @Test
    public void testTransactionRollbackOnStockFail() {
        System.out.println("Running testTransactionRollbackOnStockFail...");
        
        Map<Product, Integer> cart = new HashMap<>();
        // 库存只有 2，试图买 10 个
        cart.put(product, 10); 
        
        double totalCost = 10 * 100.0; // 1000元，用户余额刚好够
        
        try {
            // 执行支付 (预期会抛出异常)
            orderDAO.checkout(buyer, cart, totalCost);
            fail("应该抛出 SQLException 因为库存不足"); // 如果没抛异常，测试失败
        } catch (SQLException e) {
            System.out.println("捕获到预期异常: " + e.getMessage());
            // 验证异常信息包含库存不足
            assertTrue(e.getMessage().contains("Insufficient stock"));
        }
        
        // --- 关键验证：验证回滚是否成功 ---
        // 即使代码里先执行了 UPDATE USERS SET balance ...
        // 因为后面报错回滚了，所以数据库里余额应该还是 1000，而不是 0
        User updatedUser = userDAO.getUserById(buyer.getUserId());
        assertEquals("事务回滚失败！用户的钱被扣了！", 1000.0, updatedUser.getBalance(), 0.01);
    }
}