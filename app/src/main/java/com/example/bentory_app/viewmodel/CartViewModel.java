package com.example.bentory_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.repository.SellingWindowRepository;

import java.util.List;

public class CartViewModel extends ViewModel {

    // Repository
    private final SellingWindowRepository repository = SellingWindowRepository.getInstance();

    // LiveData
    private final MutableLiveData<List<CartModel>> cartItems = new MutableLiveData<>(repository.getCartItems());

    // 📤 Expose cart items to UI components as LiveData (read-only).
    public LiveData<List<CartModel>> getCartItems() {
        return cartItems;
    }

    // ➕ Add a new product to cart.
    public void addToCart(CartModel newItem) {
        repository.addToCart(newItem);                  // Add to cart repository
        cartItems.setValue(repository.getCartItems());  // Update liveData
    }

    // ✅ Confirm the cart and deduct stock from Firebase.
    public void confirmCart(Runnable onComplete) {
        repository.confirmCartAndDeductStock(() -> {
            cartItems.setValue(repository.getCartItems()); // Only update after stock changes
            if (onComplete != null) onComplete.run();      // Optional: Notify the UI
        });

    }

    // 💰 Calculate total cost of the items in the cart.
    public double getCartTotal() {
        return SellingWindowRepository.calculateTotal(repository.getCartItems());
    }

}
