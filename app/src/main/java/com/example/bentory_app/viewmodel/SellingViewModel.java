package com.example.bentory_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.repository.ProductRepository;

import java.util.List;

public class SellingViewModel extends ViewModel {
    private ProductRepository repository;
    public LiveData<List<ProductModel>> items;

    // ViewModel class that connects the UI with the data source (repository)
    public SellingViewModel() {
        // Create a new instance of the repository which handles data operations
        repository = new ProductRepository();

        // Get the product list (as LiveData) from the repository
        items = repository.getData();
    }

    // This method allows the UI (like Activity or Fragment) to observe the product data
    public LiveData<List<ProductModel>> getItems() {
        return items;
    }

    // Undo comment if this will be needed, function nito ma-update yung manual typing based sa current quantity.
//    public void updateQuantity(String productId, int newQuantity) {
//        List<CartModel> currentItems = cartItems.getValue();
//        for (CartModel item : currentItems) {
//            if (item.getId().equals(productId)) {
//                item.setQuantity(newQuantity);
//                break;
//            }
//        }
//        cartItems.setValue(currentItems);
//    }
}
