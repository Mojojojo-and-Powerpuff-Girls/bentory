package com.example.bentory_app.model;

import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductModel {
    //stats list in inventory

    //product details list
    private String id; // Needed for the multiple delete button
    private String name;
    private String category;
    private int quantity;
    private double cost_price;
    private double sale_price;
    private String size;
    private String weight;

    private String date_added;

    private List<String> barcode = new ArrayList<>();

    private String description;



    // open constructor for firebase
    public ProductModel(){}

    public ProductModel(String id, String name, String category, int quantity, double cost_price, double sale_price, String size, String weight, String description, String date_added, List<String> barcode) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.cost_price = cost_price;
        this.sale_price = sale_price;
        this.size = size;
        this.weight = weight;
        this.description = description;
        this.date_added = date_added;
        this.barcode = barcode;
    }

    // Minimal constructor for test purposes
    public ProductModel(String id, String name, double cost_price, int quantity) {
        this.id = id;
        this.name = name;
        this.cost_price = cost_price;
        this.quantity = quantity;
        this.barcode = new ArrayList<>();
    }



    // Getters
    @PropertyName("id")
    public String getId() { return id; }
    @PropertyName("name") // Maps the java field to the Firebase key. WHY: Ensures Firebase uses the correct field name, avoiding automatic conversion to camelCase.
    public String getName() { return name; }
    @PropertyName("category")
    public String getCategory() { return category; }
    @PropertyName("quantity")
    public int getQuantity() { return quantity; }
    @PropertyName("cost_price")
    public double getCost_Price() { return cost_price; }
    @PropertyName("sale_price")
    public double getSale_Price() { return sale_price; }
    @PropertyName("size")
    public String getSize() { return size; }
    @PropertyName("weight")
    public String getWeight() { return weight; }
    @PropertyName("description")
    public String getDescription() {
        return description;
    }
    @PropertyName("date_added")
    public String getDate_Added() { return date_added; }
    @PropertyName("barcode")
    public List<String> getBarcode() { return barcode; }


    // Setters
    @PropertyName("id")
    public void setId(String id) { this.id = id; }
    @PropertyName("name")
    public void setName(String name) { this.name = name; }
    @PropertyName("category")
    public void setCategory(String category) { this.category = category; }
    @PropertyName("quantity")
    public void setQuantity(int quantity) { this.quantity = quantity; }
    @PropertyName("cost_price")
    public void setCost_Price(double cost_price) { this.cost_price = cost_price; }
    @PropertyName("sale_price")
    public void setSale_Price(double sale_price) { this.sale_price = sale_price; }
    @PropertyName("size")
    public void setSize(String size) { this.size = size; }
    @PropertyName("weight")
    public void setWeight(String weight) { this.weight = weight; }
    @PropertyName("description")
    public void setDescription(String description) { this.description = description; }
    @PropertyName("date_added")
    public void setDate_Added(String date_added) { this.date_added = date_added; }
    @PropertyName("barcode")
    public void setBarcode(List<String> barcode) { this.barcode = barcode; }
}
