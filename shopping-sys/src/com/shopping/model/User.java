package com.shopping.model;

public class User {
    //用户id
    private Integer userId;
    //用户名
    private String username;
    //密码
    private String password;
    //用户角色 客户商家 CUSTOMER or MERCHANT
    private String role;
    //用户余额
    private double balance;

    public User(){}

    public User(Integer userId, String username, String password, String role, double balance) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.balance = balance;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
