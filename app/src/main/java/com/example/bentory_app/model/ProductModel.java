package com.example.bentory_app.model;

import java.util.Date;

public class ProductModel {
    //stats list in inventory

    //product details list
    private String name;
    private String category;
    private int quantity;
    private double cost_price;
    private double sale_price;
    private String size;
    private String weight;

    private Date date_added;

    private String barcode;

    private String description;



    // open constructor for firebase
    public ProductModel(){

    }

    public ProductModel(String name, String category, int quantity, double cost_price, double sale_price, String size, String weight, String description) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.cost_price = cost_price;
        this.sale_price = sale_price;
        this.size = size;
        this.weight = weight;
    }



    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
    public double getCostPrice() { return cost_price; }
    public double getSalePrice() { return sale_price; }
    public String getSize() { return size; }
    public String getWeight() { return weight; }

    public String getDescription() {
        return description;
    }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setCost_price(double cost_price) { this.cost_price = cost_price; }
    public void setSale_price(double sale_price) { this.sale_price = sale_price; }
    public void setSize(String size) { this.size = size; }
    public void setWeight(String weight) { this.weight = weight; }
    public void setDescription(String description) { this.description = description; }
    public void setDate_added(Date date_added) { this.date_added = date_added; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

}
