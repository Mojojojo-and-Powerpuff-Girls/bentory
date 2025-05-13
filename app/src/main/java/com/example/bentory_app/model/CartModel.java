package com.example.bentory_app.model;

public class CartModel {
    private String name;
    private String size;
    private int quantity;
    private String price;

    public CartModel(String name, String size, int quantity, String price) {
        this.name = name;
        this.size = size;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
