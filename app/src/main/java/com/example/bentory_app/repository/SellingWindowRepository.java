package com.example.bentory_app.repository;

import com.example.bentory_app.model.CartModel;

import java.util.ArrayList;
import java.util.List;

public class SellingWindowRepository {
    private List<CartModel> cartItems = new ArrayList<>(); // Temporal local storage

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

}
