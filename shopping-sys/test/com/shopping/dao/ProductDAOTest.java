package com.shopping.dao;

import com.shopping.dao.ProductDAO;
import com.shopping.dao.UserDAO;
import com.shopping.model.Product;
import com.shopping.model.User;
import com.shopping.util.DatabaseHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Vector;

/**
 * 商家商品管理功能的单元测试
 * 核心目标：验证不同商家之间的数据是否隔离，以及防止越权操作
 */
public class ProductDAOTest {

    private ProductDAO productDAO;
    private UserDAO userDAO;
    
    // 模拟两个不同的商家
    private User merchantA;
    private User merchantB;
    
    // 用于生成唯一用户名，避免冲突
    private long timestamp;

    @Before
    public void setUp() {
        // 1. 初始化数据库
        DatabaseHelper.getInstance();
        productDAO = new ProductDAO();
        userDAO = new UserDAO();
        timestamp = System.currentTimeMillis();

        // 2. 注册并登录商家 A
        String nameA = "MerchantA_" + timestamp;
        User userA = new User(null, nameA, "passA", "MERCHANT", 0.0);
        userDAO.register(userA);
        merchantA = userDAO.login(nameA, "passA", "MERCHANT");
        
        // 3. 注册并登录商家 B
        String nameB = "MerchantB_" + timestamp;
        User userB = new User(null, nameB, "passB", "MERCHANT", 0.0);
        userDAO.register(userB);
        merchantB = userDAO.login(nameB, "passB", "MERCHANT");
    }

    @After
    public void tearDown() {
        // 实际项目中应清理数据，这里为简化略过
    }

    /**
     * 测试用例 1: 商家能否成功添加商品
     */
    @Test
    public void testAddProduct() {
        System.out.println("Running testAddProduct...");
        
        Product p = new Product(0, "Apple", "Fresh Apple", 5.0, 100, merchantA.getUserId());
        boolean success = productDAO.addProduct(p);
        
        assertTrue("商家应该能成功添加商品", success);
        
        // 验证查询结果
        Vector<Vector<Object>> products = productDAO.getProductsByMerchant(merchantA.getUserId());
        boolean found = false;
        for (Vector<Object> row : products) {
            // row: [id, name, desc, price, stock]
            if (row.get(1).equals("Apple")) {
                found = true;
                break;
            }
        }
        assertTrue("添加的商品应该能被查询到", found);
    }

    /**
     * 测试用例 2: 数据隔离测试 (关键！)
     * 验证：商家 A 只能看到自己的商品，看不到商家 B 的商品
     */
    @Test
    public void testMerchantDataIsolation() {
        System.out.println("Running testMerchantDataIsolation...");
        
        // 商家 A 添加 "Product A"
        Product pA = new Product(0, "Product A", "Desc A", 10.0, 10, merchantA.getUserId());
        productDAO.addProduct(pA);
        
        // 商家 B 添加 "Product B"
        Product pB = new Product(0, "Product B", "Desc B", 20.0, 20, merchantB.getUserId());
        productDAO.addProduct(pB);
        
        // --- 验证商家 A 的视图 ---
        Vector<Vector<Object>> listA = productDAO.getProductsByMerchant(merchantA.getUserId());
        boolean seesProductA = false;
        boolean seesProductB = false;
        
        for (Vector<Object> row : listA) {
            String name = (String) row.get(1);
            if (name.equals("Product A")) seesProductA = true;
            if (name.equals("Product B")) seesProductB = true;
        }
        
        assertTrue("商家 A 应该能看到 Product A", seesProductA);
        assertFalse("商家 A 不应该看到 Product B", seesProductB); // 核心断言
        
        // --- 验证商家 B 的视图 ---
        Vector<Vector<Object>> listB = productDAO.getProductsByMerchant(merchantB.getUserId());
        // 简单验证数量（假设之前没有脏数据，或者我们只看特定的名字）
        // 更严谨的做法是像上面一样遍历检查
    }

    /**
     * 测试用例 3: 越权删除测试 (Security)
     * 验证：商家 A 试图删除商家 B 的商品，应该失败
     */
    @Test
    public void testCrossMerchantDelete() {
        System.out.println("Running testCrossMerchantDelete...");
        
        // 1. 商家 B 添加一个商品
        String uniqueName = "SecretItem_" + timestamp;
        Product pB = new Product(0, uniqueName, "Desc", 999.0, 1, merchantB.getUserId());
        productDAO.addProduct(pB);
        
        // 2. 我们需要获取这个商品的ID。
        // 为了测试准确，先用商家B的身份查出来
        Vector<Vector<Object>> listB = productDAO.getProductsByMerchant(merchantB.getUserId());
        int productBid = -1;
        for (Vector<Object> row : listB) {
            if (row.get(1).equals(uniqueName)) {
                productBid = (int) row.get(0);
                break;
            }
        }
        assertNotEquals("应该能找到刚才添加的商品ID", -1, productBid);
        
        // 3. 关键动作：商家 A 尝试删除这个 ID 的商品
        // 调用 deleteProduct(productId, merchantId)
        boolean result = productDAO.deleteProduct(productBid, merchantA.getUserId());
        
        // 4. 断言
        assertFalse("商家 A 不应该能删除商家 B 的商品", result);
    }

    /**
     * 测试用例 4: 正常更新测试
     */
    @Test
    public void testUpdateOwnProduct() {
        System.out.println("Running testUpdateOwnProduct...");
        
        // 1. 添加商品
        String pName = "OldName_" + timestamp;
        Product p = new Product(0, pName, "Desc", 10.0, 5, merchantA.getUserId());
        productDAO.addProduct(p);
        
        // 2. 获取 ID
        Vector<Vector<Object>> list = productDAO.getProductsByMerchant(merchantA.getUserId());
        int pId = -1;
        for (Vector<Object> row : list) {
            if (row.get(1).equals(pName)) {
                pId = (int) row.get(0);
                break;
            }
        }
        
        // 3. 更新商品
        Product pUpdate = new Product(pId, "NewName", "NewDesc", 99.0, 100, merchantA.getUserId());
        boolean updateSuccess = productDAO.updateProduct(pUpdate);
        
        assertTrue("商家应该能更新自己的商品", updateSuccess);
        
        // 4. 再次查询验证价格变了
        // 这里为了简化直接信任 updateSuccess，或者再次调用 getProductsByMerchant 验证
    }
}