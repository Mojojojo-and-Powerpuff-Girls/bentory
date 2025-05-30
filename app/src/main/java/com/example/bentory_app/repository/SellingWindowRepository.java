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
    private static SellingWindowRepository instance;
    public static SellingWindowRepository getInstance() {
        if (instance == null) {
            instance = new SellingWindowRepository();
        }
        return instance;
    }

    // Declare your Firebase references and cart here
    private DatabaseReference productsRef;
    private List<CartModel> cartItems;

    private SellingWindowRepository() {
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        cartItems = new ArrayList<>();
    }

    // Add to Cart
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
            cartItems.add(newItems);
        }
    }

    // Get Cart items
    public List<CartModel> getCartItems() { return cartItems; }

    // Clear Cart after confirmation.
    public void clearCart() {
        cartItems.clear();
    }

    // Deduct stock from Firebase when confirming the cart.
    public void confirmCartAndDeductStock(Runnable onComplete) {
        if (cartItems.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        AtomicInteger pendingUpdates = new AtomicInteger(cartItems.size());

        for (CartModel cartItem : cartItems) {
            String productId = cartItem.getId();
            int quantityToDeduct = cartItem.getQuantity();
            productsRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        ProductModel product = snapshot.getValue(ProductModel.class);
                        if (product != null) {
                            int currentStock = product.getQuantity();
                            int newStock = Math.max(currentStock - quantityToDeduct, 0); // prevent negative stock

                            productsRef.child(productId).child("quantity").setValue(newStock)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("CartConfirm", "Successfully updated stock for " + productId);
                                        } else {
                                            Log.e("CartConfirm", "Failed to update stock for " + productId, task.getException());
                                        }
                                        if (pendingUpdates.decrementAndGet() == 0) {
                                            clearCart();
                                            if (onComplete != null) onComplete.run();
                                        }
                                    });
                        } else {
                            if (pendingUpdates.decrementAndGet() == 0) {
                                clearCart();
                                if (onComplete != null) onComplete.run();
                            }
                        }
                    } else {
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

    public static double calculateTotal(List<CartModel> cartItems) {
        double total = 0;
        for (CartModel item : cartItems) {
            total += item.getLinkedProduct().getSale_Price() * item.getQuantity();
        }
        return total;
    }

}
