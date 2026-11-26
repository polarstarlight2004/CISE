package com.shopping.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 购物车业务模型 (内存版)
 * 负责管理当前会话中用户选择的商品列表
 */
public class ShoppingCart {

    // 核心数据结构：Map<商品对象, 购买数量>
    // 使用 Product 对象作为 Key，这样我们可以直接获取价格和名称，不用再去查库
    private Map<Product, Integer> items = new HashMap<>();

    public ShoppingCart() {
    }

    /**
     * 添加商品到购物车
     * @param product 商品对象
     * @param quantity 数量
     */
    public void addProduct(Product product, int quantity) {
        if (quantity <= 0) return;

        // getOrDefault逻辑：如果购物车里已经有这个商品，就在原有数量上增加；如果没有，就是0+quantity
        int currentQty = items.getOrDefault(product, 0);
        items.put(product, currentQty + quantity);
    }

    /**
     * 移除某种商品
     */
    public void removeProduct(Product product) {
        items.remove(product);
    }

    /**
     * 更新某个商品的数量 (用于购物车界面直接修改数量)
     */
    public void updateQuantity(Product product, int quantity) {
        if (quantity <= 0) {
            removeProduct(product);
        } else {
            items.put(product, quantity);
        }
    }

    /**
     * 清空购物车 (结账后调用)
     */
    public void clear() {
        items.clear();
    }

    /**
     * 获取所有商品条目 (供 Controller 遍历显示)
     */
    public Map<Product, Integer> getItems() {
        return items;
    }

    /**
     * 计算购物车总金额
     * @return 总价
     */
    public double getTotalPrice() {
        double total = 0.0;
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            Product p = entry.getKey();
            Integer qty = entry.getValue();
            // 单价 * 数量
            total += p.getPrice() * qty;
        }
        return total;
    }

    /**
     * 获取商品种类数量 (不是总件数，而是几种商品)
     */
    public int getItemCount() {
        return items.size();
    }
}