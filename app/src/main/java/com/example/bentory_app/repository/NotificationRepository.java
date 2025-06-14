package com.example.bentory_app.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import com.example.bentory_app.model.NotificationModel;
import com.example.bentory_app.model.ProductModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationRepository {
    private final DatabaseReference notificationsRef;
    private final ProductRepository productRepository;
    private static final int LOW_STOCK_THRESHOLD = 10; // Products with quantity below this are considered low stock

    public NotificationRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        notificationsRef = database.getReference("notifications");
        productRepository = new ProductRepository();
    }

    // Check for low stock products and create notifications
    public void checkLowStockProducts() {
        LiveData<List<ProductModel>> productsLiveData = productRepository.getData();
        productsLiveData.observeForever(products -> {
            if (products != null) {
                for (ProductModel product : products) {
                    if (product.getQuantity() <= LOW_STOCK_THRESHOLD) {
                        createLowStockNotification(product);
                    }
                }
            }
        });
    }

    // Create a low stock notification
    private void createLowStockNotification(ProductModel product) {
        String notificationId = notificationsRef.push().getKey();
        if (notificationId == null)
            return;

        String title = "Low Stock Alert";
        String message = product.getName() + " is running low. Only " + product.getQuantity() + " items left in stock.";

        NotificationModel notification = new NotificationModel(
                notificationId,
                title,
                message,
                "LOW_STOCK",
                product.getId());

        notificationsRef.child(notificationId).setValue(notification);
    }

    // Get all notifications
    public void getNotifications(OnNotificationsLoadedListener listener) {
        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<NotificationModel> notifications = new ArrayList<>();
                for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                    try {
                        // First try to get the data as a map
                        Map<String, Object> data = (Map<String, Object>) notificationSnapshot.getValue();
                        if (data != null) {
                            NotificationModel notification = new NotificationModel();
                            notification.setId(notificationSnapshot.getKey());

                            // Safely extract and convert values
                            notification.setTitle((String) data.get("title"));
                            notification.setMessage((String) data.get("message"));
                            notification.setType((String) data.get("type"));
                            notification.setProductId((String) data.get("productId"));

                            // Handle timestamp conversion
                            Object timestampObj = data.get("timestamp");
                            if (timestampObj instanceof Long) {
                                notification.setTimestamp((Long) timestampObj);
                            } else if (timestampObj instanceof Map) {
                                // If timestamp is a map, use current time
                                notification.setTimestamp(System.currentTimeMillis());
                            } else {
                                // If timestamp is missing or invalid, use current time
                                notification.setTimestamp(System.currentTimeMillis());
                            }

                            notifications.add(notification);
                        }
                    } catch (Exception e) {
                        // Skip invalid notifications
                        continue;
                    }
                }

                // Sort notifications by timestamp in descending order (newest first)
                Collections.sort(notifications, (n1, n2) -> {
                    Long t1 = n1.getTimestamp();
                    Long t2 = n2.getTimestamp();
                    if (t1 == null && t2 == null)
                        return 0;
                    if (t1 == null)
                        return 1;
                    if (t2 == null)
                        return -1;
                    return t2.compareTo(t1); // Descending order
                });

                listener.onNotificationsLoaded(notifications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // Interface for notification loading callback
    public interface OnNotificationsLoadedListener {
        void onNotificationsLoaded(List<NotificationModel> notifications);

        void onError(String error);
    }
}