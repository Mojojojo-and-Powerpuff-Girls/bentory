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

    // For realtime recalculation
    private final MutableLiveData<Double> totalPrice = new MutableLiveData<>(0.0);
    public LiveData<Double> getTotalPrice() {
        return totalPrice;
    }

    // 📤 Expose cart items to UI components as LiveData (read-only).
    public LiveData<List<CartModel>> getCartItems() {
        return cartItems;
    }

    // ➕ Add a new product to cart.
    public void addToCart(CartModel newItem) {
        repository.addToCart(newItem);                  // Add to cart repository
        updateCartState();
    }

    // ✅ Confirm the cart and deduct stock from Firebase.
    public void confirmCart(Runnable onComplete) {
        repository.confirmCartAndDeductStock(() -> {
            updateCartState();
            if (onComplete != null) onComplete.run();      // Optional: Notify the UI
        });

    }

    // 💰 Calculate total cost of the items in the cart.
    public double getCartTotal() {
        return SellingWindowRepository.calculateTotal(repository.getCartItems());
    }

    // 🛒🔄 Updates the current cart state: retrieves latest cart items and recalculates total price. (INTERNAL USE)
    private void updateCartState() {
        List<CartModel> currentItems = repository.getCartItems(); // Get items from repo.
        cartItems.setValue(currentItems);                         // Update liveData with current cart items.

        double total = 0;
        for (CartModel item : currentItems) {
            total += item.getQuantity() * item.getPrice();        // Multiply quantity x price for each item.
        }
        totalPrice.setValue(total);                               // Set updated total price.
    }

    // 🧩📣 External trigger to manually refresh the cart state. (PUBLIC METHOD)
    public void forceUpdate() {
        updateCartState();                                        // Force refresh of cart items and total price.
    }

}
