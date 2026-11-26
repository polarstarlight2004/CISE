package com.shopping.util;
import com.shopping.sql.InitSql;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:derby:shopping_db;create=true";
    private static DatabaseHelper instance;
    private Connection connection;

    private DatabaseHelper() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("数据库连接成功");
            initializeTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeTables() {
        try (Statement stmt = connection.createStatement()) {
            DatabaseMetaData dbm = connection.getMetaData();

            // 1. 检查并创建 USERS 表
            // 注意：Derby 内部表名通常是大写 "USERS"
            ResultSet tables = dbm.getTables(null, null, "USERS", null);
            if (!tables.next()) {
                // 使用 InitSql 中的常量
                stmt.execute(InitSql.CREATE_USERS_TABLE);
                //System.out.println("表 USERS 已创建");

                // 只有在表刚创建时才插入默认商户
                stmt.execute(InitSql.INSERT_DEFAULT_MERCHANT);
                //System.out.println("默认商户已插入: merchant / 1234 / MERCHANT");

                // 只有在表刚创建时才插入默认客户
                stmt.execute(InitSql.INSERT_DEFAULT_CUSTOMER);
                //System.out.println("默认客户已插入: customer / 1234 / CUSTOMER");

                // 只有在表刚创建时才插入默认管理员
                stmt.execute(InitSql.INSERT_DEFAULT_ADMIN);
                //System.out.println("默认管理员已插入: admin / admin123 / ADMIN");
            }

            // 2. 检查并创建 PRODUCTS 表
            tables = dbm.getTables(null, null, "PRODUCTS", null);
            if (!tables.next()) {
                stmt.execute(InitSql.CREATE_PRODUCTS_TABLE);
                //System.out.println("表 PRODUCTS 已创建");
            }

            // 3. 检查并创建 ORDERS 表
            tables = dbm.getTables(null, null, "ORDERS", null);
            if (!tables.next()) {
                stmt.execute(InitSql.CREATE_ORDERS_TABLE);
                //System.out.println("表 ORDERS 已创建");
            }

            //4. jiancha并创建 ORDER_ITEMS 表
            tables = dbm.getTables(null, null, "ORDER_ITEMS", null);
            if (!tables.next()) {
                stmt.execute(InitSql.CREATE_ORDER_ITEMS_TABLE);
                //System.out.println("表 ORDER_ITEMS 已创建");
            }

            //5. 检查并创建CART_ITEMS 表
            tables = dbm.getTables(null, null, "CART_ITEMS", null);
            if (!tables.next()) {
                stmt.execute(InitSql.CREATE_CART_ITEMS_TABLE);
                //System.out.println("表 CART_ITEMS 已创建");
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}