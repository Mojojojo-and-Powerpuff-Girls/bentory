package com.example.bentory_app.model;

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

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public String getSold() {
        return sold;
    }

    public String getStock() {
        return stock;
    }

    public String getStatus() {
        return status;
    }
}
