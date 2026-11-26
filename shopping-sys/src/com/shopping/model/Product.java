package com.shopping.model;

public class Product {
    //商品id
    private Integer productId;
    //商品名称
    private String name;
    //商品描述
    private String description;
    //商品价格
    private double price;
    //商品库存
    private Integer stock;
    //商户id 用于绑定商品和商户
    private Integer merchantId;

    public Product(){}

    public Product(int productId, String name, String description, double price, int stock, int merchantId) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.merchantId = merchantId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Integer merchantId) {
        this.merchantId = merchantId;
    }

    @Override
    public int hashCode() {
        // 只使用 productId 来区分商品是否相同
        int hash = 7;
        hash = 97 * hash + (this.productId != null ? this.productId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Product other = (Product) obj;
        // 如果两个商品的 ID 一样，我们就认为它们是同一个商品
        if (this.productId != other.productId && (this.productId == null || !this.productId.equals(other.productId))) {
            return false;
        }
        return true;
    }
}
