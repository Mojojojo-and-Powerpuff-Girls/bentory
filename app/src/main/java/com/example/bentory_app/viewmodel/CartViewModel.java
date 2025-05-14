package com.example.bentory_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.repository.SellingWindowRepository;

import java.util.List;

public class CartViewModel extends ViewModel {

    private SellingWindowRepository repository;
    private MutableLiveData<List<CartModel>> cartItems;

    // Initialization
    public CartViewModel() {
        repository = new SellingWindowRepository();
        cartItems = new MutableLiveData<>(repository.getCartItems());
    }

    // Get Cart Items
    public LiveData<List<CartModel>> getCartItems() {
        return cartItems;
    }

    // Add product to cart
    public void addToCart(CartModel newItem) {
        repository.addToCart(newItem); // Add to cart repository
        cartItems.setValue(repository.getCartItems()); // Update liveData
    }



}
