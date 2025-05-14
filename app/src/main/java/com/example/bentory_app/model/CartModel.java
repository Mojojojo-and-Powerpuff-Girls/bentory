package com.example.bentory_app.model;

import com.google.firebase.database.PropertyName;

public class CartModel {
    private String id;
    private String name;
    private String size;
    private int quantity;
    private double price;

    public CartModel(String id, String name, String size, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters
    @PropertyName("id")
    public String getId() {
        return id;
    }
    @PropertyName("name")
    public String getName() {
        return name;
    }
    @PropertyName("size")
    public String getSize() {
        return size;
    }
    @PropertyName("quantity")
    public int getQuantity() {
        return quantity;
    }
    @PropertyName("price")
    public double getPrice() {
        return price;
    }


    // Setters
    @PropertyName("id")
    public void setId(String id) { this.id = id; }
    @PropertyName("quantity")
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
