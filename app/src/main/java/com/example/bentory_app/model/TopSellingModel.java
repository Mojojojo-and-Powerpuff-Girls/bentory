package com.example.bentory_app.model;

import com.google.firebase.database.PropertyName;

public class TopSellingModel {
    private String name;
    private String size;
    private String sold;
    private String stock;
    private String status;

    public TopSellingModel(String name, String size, String sold, String stock, String status) {
        this.name = name;
        this.size = size;
        this.sold = sold;
        this.stock = stock;
        this.status = status;
    }

    // Getters
    @PropertyName("name")
    public String getName() {
        return name;
    }
    @PropertyName("size")
    public String getSize() {
        return size;
    }
    @PropertyName("sold")
    public String getSold() {
        return sold;
    }
    @PropertyName("stock")
    public String getStock() {
        return stock;
    }
    @PropertyName("status")
    public String getStatus() {
        return status;
    }
}
