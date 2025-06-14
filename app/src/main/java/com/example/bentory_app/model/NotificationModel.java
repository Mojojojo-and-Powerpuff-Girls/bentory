package com.example.bentory_app.model;

import com.google.firebase.database.Exclude;
import java.util.Date;

public class NotificationModel {
    private String id;
    private String title;
    private String message;
    private Long timestamp; // Changed from Date to Long for Firebase compatibility
    private String type; // "LOW_STOCK", "SALES_UPDATE", etc.
    private String productId; // For product-related notifications

    public NotificationModel() {
        // Required empty constructor for Firebase
    }

    public NotificationModel(String id, String title, String message, String type, String productId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = System.currentTimeMillis(); // Store as Long
        this.type = type;
        this.productId = productId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude // This method won't be serialized to Firebase
    public Date getDate() {
        return timestamp != null ? new Date(timestamp) : null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}