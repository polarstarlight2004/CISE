package com.shopping.model;

import com.shopping.model.Product;
import com.shopping.model.ShoppingCart;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ShoppingCartTest {

    private ShoppingCart cart;
    private Product p1;
    private Product p2;

    @Before
    public void setUp() {
        cart = new ShoppingCart();
        // 模拟两个商品 (id, name, desc, price, stock, merchantId)
        p1 = new Product(101, "Java Book", "Learn Java", 50.0, 10, 1);
        p2 = new Product(102, "Mouse", "Wireless Mouse", 20.0, 20, 1);
    }

    @Test
    public void testAddProduct() {
        cart.addProduct(p1, 2); // 加2本
        
        assertEquals("购物车应该有1种商品", 1, cart.getItemCount());
        assertEquals("总价应该是 100.0", 100.0, cart.getTotalPrice(), 0.01);
    }

    @Test
    public void testAddDuplicateProduct() {
        cart.addProduct(p1, 1);
        cart.addProduct(p1, 1); // 再加1本，应该是数量叠加，而不是新增一行
        
        assertEquals("购物车仍然只有1种商品", 1, cart.getItemCount());
        int qty = cart.getItems().get(p1);
        assertEquals("商品数量应该是2", 2, qty);
    }

    @Test
    public void testCalculateTotal() {
        cart.addProduct(p1, 1); // 50.0
        cart.addProduct(p2, 2); // 20.0 * 2 = 40.0
        
        assertEquals("总价应该是 90.0", 90.0, cart.getTotalPrice(), 0.01);
    }

    @Test
    public void testRemoveProduct() {
        cart.addProduct(p1, 1);
        cart.removeProduct(p1);
        
        assertEquals("购物车应该是空的", 0, cart.getItemCount());
        assertEquals("总价应该是 0.0", 0.0, cart.getTotalPrice(), 0.01);
    }
}