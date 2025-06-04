package com.example.bentory_app.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.model.ProductModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

}
