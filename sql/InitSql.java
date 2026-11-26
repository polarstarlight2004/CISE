package com.shopping.sql;

/**
 * 存储数据库初始化sql脚本
 */
public class InitSql {

    // 创建 USERS 表的 SQL 语句
    public static final String CREATE_USERS_TABLE =
            "CREATE TABLE USERS (" +
                    "user_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(50) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "balance DOUBLE DEFAULT 0.0" +
                    ")";

    // 创建 PRODUCTS 表的 SQL 语句
    public static final String CREATE_PRODUCTS_TABLE =
            "CREATE TABLE PRODUCTS (" +
                    "product_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "description VARCHAR(255), " +
                    "price DOUBLE NOT NULL, " +
                    "stock INT DEFAULT 0, " +
                    "merchant_id INT, " +
                    "FOREIGN KEY (merchant_id) REFERENCES USERS(user_id)" +
                    ")";

    // 创建 ORDERS 表的 SQL 语句
    public static final String CREATE_ORDERS_TABLE =
            "CREATE TABLE ORDERS (" +
                    "order_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                    "user_id INT, " +
                    "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "total_amount DOUBLE, " +
                    "FOREIGN KEY (user_id) REFERENCES USERS(user_id)" +
                    ")";

    //ORDER_ITEMS 表 用于存储订单中的商品项，同时标注商家 用于商家查看自己的订单
    public static final String CREATE_ORDER_ITEMS_TABLE =
            "CREATE TABLE ORDER_ITEMS (" +
                    "item_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                    "order_id INT, " +
                    "product_id INT, " +
                    "quantity INT, " +
                    "price_at_purchase DOUBLE, " + /// 单价
                    "FOREIGN KEY (order_id) REFERENCES ORDERS(order_id), " +
                    "FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id)" +
                    ")";

   // 创建购物车商品表
    public static final String CREATE_CART_ITEMS_TABLE =
            "CREATE TABLE CART_ITEMS (" +
                    "cart_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                    "user_id INT, " +
                    "product_id INT, " +
                    "quantity INT, " +
                    "FOREIGN KEY (user_id) REFERENCES USERS(user_id), " +
                    "FOREIGN KEY (product_id) REFERENCES PRODUCTS(product_id), " +
                    "CONSTRAINT unique_user_product UNIQUE (user_id, product_id)" +
                    ")";

    // 插入默认商户的 SQL 语句
    public static final String INSERT_DEFAULT_MERCHANT =
            "INSERT INTO USERS (username, password, role, balance) VALUES ('user2', '1234', 'MERCHANT', 0.0)"; // 商家2

    // 插入默认客户的 SQL 语句
    public static final String INSERT_DEFAULT_CUSTOMER =
            "INSERT INTO USERS (username, password, role, balance) VALUES ('user1', '1234', 'CUSTOMER', 0.0)"; // 客户1

    // 插入默认管理员的 SQL 语句
    public static final String INSERT_DEFAULT_ADMIN =
            "INSERT INTO USERS (username, password, role, balance) VALUES ('admin', '1234', 'ADMIN', 0.0)";

    // 查询表是否存在的元数据查询（Derby特定）
    public static final String CHECK_TABLE_EXISTS =
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
}