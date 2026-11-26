package com.shopping.dao;

import com.shopping.dao.ProductDAO;
import com.shopping.dao.UserDAO;
import com.shopping.model.Product;
import com.shopping.model.User;
import com.shopping.util.DatabaseHelper;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Vector;

public class AdminDAOTest {

    private UserDAO userDAO;
    private ProductDAO productDAO;
    private long timestamp;

    @Before
    public void setUp() {
        DatabaseHelper.getInstance();
        userDAO = new UserDAO();
        productDAO = new ProductDAO();
        timestamp = System.currentTimeMillis();
    }

    /**
     * 测试用例 1: 验证管理员能获取所有普通用户，但不包含管理员自己
     * (对应 BusinessSql.ADMIN_GET_ALL_USERS)
     */
    @Test
    public void testAdminViewAllUsers() {
        // 1. 创建几个测试用户
        String cName = "TestCust_" + timestamp;
        String mName = "TestMerch_" + timestamp;
        userDAO.register(new User(null, cName, "123", "CUSTOMER", 0.0));
        userDAO.register(new User(null, mName, "123", "MERCHANT", 0.0));

        // 2. 调用管理员接口获取用户列表
        Vector<Vector<Object>> allUsers = userDAO.getAllUsers();
        
        boolean foundCustomer = false;
        boolean foundMerchant = false;
        boolean foundAdmin = false;

        for (Vector<Object> row : allUsers) {
            String role = (String) row.get(2); // Role is at index 2
            String name = (String) row.get(1); // Username is at index 1
            
            if (name.equals(cName)) foundCustomer = true;
            if (name.equals(mName)) foundMerchant = true;
            if (role.equals("ADMIN")) foundAdmin = true;
        }

        assertTrue("管理员应该能看到 Customer", foundCustomer);
        assertTrue("管理员应该能看到 Merchant", foundMerchant);
        assertFalse("为了安全，管理员列表中通常不显示 Admin 自己(防止误删)", foundAdmin);
    }

    /**
     * 测试用例 2: 验证管理员能查看全局所有商品 (跨商家)
     */
    @Test
    public void testAdminViewAllProducts() {
        // 1. 模拟两个不同商家的商品
        // 商家A
        User merchA = new User(null, "MerchA_" + timestamp, "123", "MERCHANT", 0.0);
        userDAO.register(merchA);
        User dbMerchA = userDAO.login(merchA.getUsername(), "123", "MERCHANT");
        Product pA = new Product(0, "ItemA", "Desc", 10.0, 10, dbMerchA.getUserId());
        productDAO.addProduct(pA);

        // 商家B
        User merchB = new User(null, "MerchB_" + timestamp, "123", "MERCHANT", 0.0);
        userDAO.register(merchB);
        User dbMerchB = userDAO.login(merchB.getUsername(), "123", "MERCHANT");
        Product pB = new Product(0, "ItemB", "Desc", 10.0, 10, dbMerchB.getUserId());
        productDAO.addProduct(pB);

        // 2. 管理员查看
        Vector<Vector<Object>> allProds = productDAO.getAllProductsForAdmin();
        
        boolean seesItemA = false;
        boolean seesItemB = false;
        
        for(Vector<Object> row : allProds) {
            String name = (String) row.get(1);
            if(name.equals("ItemA")) seesItemA = true;
            if(name.equals("ItemB")) seesItemB = true;
        }

        assertTrue("管理员应能看到商家A的商品", seesItemA);
        assertTrue("管理员应能看到商家B的商品", seesItemB);
    }

    /**
     * 测试用例 3: 验证管理员强制删除用户
     */
    @Test
    public void testAdminDeleteUser() {
        // 1. 创建一个要被删除的倒霉用户
        String deleteTarget = "ToBeDeleted_" + timestamp;
        User u = new User(null, deleteTarget, "123", "CUSTOMER", 0.0);
        userDAO.register(u);
        
        // 确保注册成功
        assertTrue(userDAO.checkUsernameExists(deleteTarget));
        
        // 获取真实ID
        User dbUser = userDAO.login(deleteTarget, "123", "CUSTOMER");
        
        // 2. 执行删除
        boolean deleted = userDAO.deleteUser(dbUser.getUserId());
        assertTrue("删除操作应返回 true", deleted);
        
        // 3. 验证是否真的消失了
        assertFalse("用户不应再存在于数据库中", userDAO.checkUsernameExists(deleteTarget));
    }

    /**
     * 测试用例 4: 验证管理员强制删除商品 (无视归属权)
     */
    @Test
    public void testAdminDeleteProduct() {
        // 1. 创建一个商品
        User m = userDAO.login("admin", "admin123", "ADMIN"); // 借用默认admin作为owner
        if(m == null) {
            // 如果admin没登陆成功，临时注册一个
             String tempM = "TempM_" + timestamp;
             userDAO.register(new User(null, tempM, "123", "MERCHANT", 0.0));
             m = userDAO.login(tempM, "123", "MERCHANT");
        }
        
        String pName = "BadProduct_" + timestamp;
        Product p = new Product(0, pName, "Desc", 99.0, 1, m.getUserId());
        productDAO.addProduct(p);
        
        // 获取ID
        int pId = -1;
        for(Vector<Object> row : productDAO.getAllProductsForAdmin()) {
            if(row.get(1).equals(pName)) {
                pId = (int) row.get(0);
                break;
            }
        }
        
        // 2. 管理员强制删除
        boolean result = productDAO.adminDeleteProduct(pId);
        assertTrue("管理员删除商品应成功", result);
        
        // 3. 验证
        boolean stillExists = false;
        for(Vector<Object> row : productDAO.getAllProductsForAdmin()) {
            if(row.get(0).equals(pId)) stillExists = true;
        }
        assertFalse("商品应当被彻底删除", stillExists);
    }
}