package com.example.bentory_app.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.model.ProductModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class SellingWindowRepository {

    // Singleton instance for global access.
    private static SellingWindowRepository instance;
    public static SellingWindowRepository getInstance() {
        if (instance == null) {
            instance = new SellingWindowRepository();
        }
        return instance;
    }

    // Firebase reference pointing to the 'products' node in the RealTime Database.
    private DatabaseReference productsRef;

    // Local list to hold cart items.
    private List<CartModel> cartItems;

    // Private constructor to enforce singleton pattern.
    private SellingWindowRepository() {
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        cartItems = new ArrayList<>();
    }


    // ===============================
    // ➕ Add item to cart : If item already exists in cart, increase its quantity by 1. (METHODS)
    // ===============================
    public void addToCart(CartModel newItems) {
        // Check if item is already in cart
        boolean exists = false;
        for (CartModel item : cartItems) {
            if (item.getId().equals(newItems.getId())) {
                item.setQuantity(item.getQuantity() + 1); // Increase quantity
                exists = true;
                break;
            }
        }

        if (!exists) {
            cartItems.add(newItems);                      // Add new item.
        }
    }


    // ===============================
    // 📦 All Cart Items : Get all cart items. (METHODS)
    // ===============================
    public List<CartModel> getCartItems() { return cartItems; }


    // ===============================
    // 🧹 Clear Cart : Clear all items from the cart. (METHODS)
    // ===============================
    public void clearCart() {
        cartItems.clear();
    }


    // ===============================
    // 🏷️ Stock Deduction : Confirm purchase by deducting quantities in Firebase. (METHODS)
    // ===============================
    public void confirmCartAndDeductStock(Runnable onComplete) {
        if (cartItems.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        // Track how many product stock updates complete.
        AtomicInteger pendingUpdates = new AtomicInteger(cartItems.size());

        for (CartModel cartItem : cartItems) {
            String productId = cartItem.getId();
            int quantityToDeduct = cartItem.getQuantity();

            // Fetch current product info from Firebase.
            productsRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        ProductModel product = snapshot.getValue(ProductModel.class);
                        if (product != null) {
                            int currentStock = product.getQuantity();
                            int newStock = Math.max(currentStock - quantityToDeduct, 0); // prevent negative stock

                            // Update new stock value in Firebase
                            productsRef.child(productId).child("quantity").setValue(newStock)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("CartConfirm", "Successfully updated stock for " + productId);
                                        } else {
                                            Log.e("CartConfirm", "Failed to update stock for " + productId, task.getException());
                                        }

                                        // When all updates done, clear cart and call callback.
                                        if (pendingUpdates.decrementAndGet() == 0) {
                                            clearCart();
                                            if (onComplete != null) onComplete.run();
                                        }
                                    });
                        } else {
                            // If product null, just decrement and maybe clear.
                            if (pendingUpdates.decrementAndGet() == 0) {
                                clearCart();
                                if (onComplete != null) onComplete.run();
                            }
                        }
                    } else {
                        // If snapshot doesn't exist, treat as done.
                        if (pendingUpdates.decrementAndGet() == 0) {
                            clearCart();
                            if (onComplete != null) onComplete.run();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("CartConfirm", "Failed to update stock: " + error.getMessage());
                    if (pendingUpdates.decrementAndGet() == 0) {
                        clearCart();
                        if (onComplete != null) onComplete.run();
                    }
                }

            });
        }

        updateSalesStats(cartItems);

    }

    private void updateSalesStats(List<CartModel> cartItems) {
        Calendar calendar = Calendar.getInstance();
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        int month = calendar.get(Calendar.MONTH); // 0 = Jan
        String monthName = new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());

        double transactionTotal = calculateTotal(cartItems);
        double transactionProfit = calculateProfit(cartItems);
        int quantitySold = cartItems.stream().mapToInt(CartModel::getQuantity).sum();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        // 1. Update Weekly Sales
        db.child("selling_stats/weekly_sale/week" + weekOfYear + "/total_sale")
                .runTransaction(newNumberTransaction(transactionTotal));

        // 2. Update Monthly Sales
        DatabaseReference monthlyRef = db.child("selling_stats/monthly_stats/month" + (month + 1));
        monthlyRef.child("total_sales").runTransaction(newNumberTransaction(transactionTotal));
        monthlyRef.child("monthly_profit").runTransaction(newNumberTransaction(transactionProfit));
        monthlyRef.child("products_sold").runTransaction(newIntTransaction(quantitySold));

        // 3. Update Top Selling
        DatabaseReference topSellingRef = db.child("selling_stats/top_selling/month" + (month + 1));
        for (CartModel item : cartItems) {
            String key = item.getId(); // or item.getLinkedProduct().getName() + "_" + size
            DatabaseReference itemRef = topSellingRef.child(key);

            itemRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Map<String, Object> itemData = (Map<String, Object>) currentData.getValue();
                    int sold = item.getQuantity();
                    if (itemData == null) {
                        currentData.child("name").setValue(item.getLinkedProduct().getName());
                        currentData.child("size").setValue(item.getLinkedProduct().getSize());
                        currentData.child("sold").setValue(sold);
                    } else {
                        long currentSold = itemData.get("sold") != null ? (Long) itemData.get("sold") : 0;
                        currentData.child("sold").setValue(currentSold + sold);
                    }
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {}
            });
        }
    }



    // ===============================
    // 💰️ Total Calculation : Calculate total price of all cart items. (METHODS)
    // ===============================
    public static double calculateTotal(List<CartModel> cartItems) {
        double total = 0;
        for (CartModel item : cartItems) {
            total += item.getLinkedProduct().getSale_Price() * item.getQuantity();
        }
        return total;
    }

    public static double calculateProfit(List<CartModel> cartItems) {
        double profit = 0;
        for (CartModel item : cartItems) {
            double salePrice = item.getLinkedProduct().getSale_Price();
            double costPrice = item.getLinkedProduct().getCost_Price();
            profit += (salePrice - costPrice) * item.getQuantity();
        }
        return profit;
    }

    private Transaction.Handler newNumberTransaction(double valueToAdd) {
        return new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Double current = currentData.getValue(Double.class);
                currentData.setValue((current == null ? 0.0 : current) + valueToAdd);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {}
        };
    }

    private Transaction.Handler newIntTransaction(int valueToAdd) {
        return new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                currentData.setValue((current == null ? 0 : current) + valueToAdd);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {}
        };
    }

}
