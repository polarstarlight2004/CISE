package com.shopping.sql;

public class BusinessSql {
    //用户相关sql
    //登录
    public static final String LOGIN_SQL = "SELECT * FROM USERS WHERE username = ? AND password = ? AND role = ?";

    //用户zhuce
    public static final String REGISTER_SQL = "INSERT INTO USERS (username, password, role, balance) VALUES (?, ?, ?, ?)";

    //检查用户名是否已经存在
    public static final String CHECK_USERNAME_EXISTS_SQL = "SELECT 1 FROM USERS WHERE username = ?";

    //更新用户个人信息
    public static final String UPDATE_USER_SQL = "UPDATE USERS SET password = ?, balance = ? WHERE user_id = ?";

    //用于刷新信息，比如余额的改动
    public static final String REFRESH_USER_BALANCE_SQL = "SELECT * FROM USERS WHERE user_id = ?";


    /**
     * 管理员相关接口
     */
    //管理员获取全局用户列表 (排除管理员自己)
    public static final String GET_ALL_USERS_SQL = "SELECT user_id, username, role, balance FROM USERS WHERE role != 'ADMIN'";

    //管理员删除用户
    public static final String DELETE_USER_SQL = "DELETE FROM USERS WHERE user_id = ?";

    //管理员获取所有商品
    public static final String ADMIN_GET_ALL_PRODUCTS =
            "SELECT * FROM PRODUCTS";

    //管理员删除商品
    public static final String ADMIN_DELETE_PRODUCT =
            "DELETE FROM PRODUCTS WHERE product_id = ?";



    //商品相关sql
    //添加商品
    public static final String ADD_PRODUCT_SQL = "INSERT INTO PRODUCTS (name, description, price, stock, merchant_id) VALUES (?, ?, ?, ?, ?)";

    //删除商品
    public static final String DELETE_PRODUCT_SQL = "DELETE FROM PRODUCTS WHERE product_id = ? AND merchant_id = ?";

    //更新商品
    public static final String UPDATE_PRODUCT_SQL = "UPDATE PRODUCTS SET name=?, description=?, price=?, stock=? WHERE product_id=? AND merchant_id=?";

    //获取特定商家的所有商品
    public static final String GET_PRODUCTS_BY_MERCHANT_SQL = "SELECT * FROM PRODUCTS WHERE merchant_id = ?";

    //获取所有商品 只用显示库存有的 为0的不需要显示
    public static final String GET_ALL_PRODUCTS_SQL = "SELECT * FROM PRODUCTS WHERE stock > 0";


    /**
     * dingdan相关sql
     */
    //扣除用户余额
    public static final String DEDUCT_USER_BALANCE_SQL = "UPDATE USERS SET balance = balance - ? WHERE user_id = ?";

    //chuangjian订单表
    public static final String CREATE_ORDER_SQL = "INSERT INTO ORDERS (user_id, total_amount) VALUES (?, ?)";

    //插入订单详情
    public static final String INSERT_ORDER_ITEM_SQL = "INSERT INTO ORDER_ITEMS (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";

    //更新商品库存
    public static final String UPDATE_PRODUCT_STOCK_SQL = "UPDATE PRODUCTS SET stock = stock - ? WHERE product_id = ? AND stock >= ?";

    //聚合查询 显示订单概览
    public static final String GET_ORDER_OVERVIEW_SQL = "SELECT order_id, order_date, total_amount FROM ORDERS WHERE user_id = ? ORDER BY order_date DESC";

    //客户功能：查询自己的历史订单
    public static final String GET_USER_ORDERS_SQL =
            "SELECT order_id, order_date, total_amount FROM ORDERS WHERE user_id = ? ORDER BY order_date DESC";

    //获取订单详情
    public static final String GET_ORDER_DETAILS_SQL = "SELECT p.name, oi.quantity, oi.price_at_purchase, (oi.quantity * oi.price_at_purchase) as subtotal " +
            "FROM ORDER_ITEMS oi " +
            "JOIN PRODUCTS p ON oi.product_id = p.product_id " +
            "WHERE oi.order_id = ?";

    // 多表联查，筛选该商家的商品销售记录
    public static final String GET_MERCHANT_SALES_SQL = "SELECT o.order_id, o.order_date, p.name, oi.quantity, " +
            "oi.price_at_purchase, u.username " +
            "FROM ORDER_ITEMS oi " +
            "JOIN PRODUCTS p ON oi.product_id = p.product_id " +
            "JOIN ORDERS o ON oi.order_id = o.order_id " +
            "JOIN USERS u ON o.user_id = u.user_id " +
            "WHERE p.merchant_id = ? " +
            "ORDER BY o.order_date DESC";

    //查询订单详细商品
    public static final String GET_ORDER_DETAILS_PRODUCT_SQL = "SELECT p.name, oi.quantity, oi.price_at_purchase, (oi.quantity * oi.price_at_purchase) as subtotal " +
            "FROM ORDER_ITEMS oi " +
            "JOIN PRODUCTS p ON oi.product_id = p.product_id " +
            "WHERE oi.order_id = ?";

}
