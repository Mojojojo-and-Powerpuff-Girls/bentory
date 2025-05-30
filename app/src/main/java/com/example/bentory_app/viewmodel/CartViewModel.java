package com.example.bentory_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.repository.SellingWindowRepository;

import java.util.List;

public class CartViewModel extends ViewModel {

    private final SellingWindowRepository repository = SellingWindowRepository.getInstance();
    private final MutableLiveData<List<CartModel>> cartItems = new MutableLiveData<>(repository.getCartItems());

    // Get Cart Items
    public LiveData<List<CartModel>> getCartItems() {
        return cartItems;
    }

    // Add product to cart
    public void addToCart(CartModel newItem) {
        repository.addToCart(newItem); // Add to cart repository
        cartItems.setValue(repository.getCartItems()); // Update liveData
    }

    // Confirm cart and deduct stock in Firebase
    public void confirmCart(Runnable onComplete) {
        repository.confirmCartAndDeductStock(() -> {
            cartItems.setValue(repository.getCartItems()); // Only update after stock changes
            if (onComplete != null) onComplete.run();      // Optional: Notify the UI
        });

    }

    // Calculates the total cost of all cart items.
    public double getCartTotal() {
        return SellingWindowRepository.calculateTotal(repository.getCartItems());
    }

}
