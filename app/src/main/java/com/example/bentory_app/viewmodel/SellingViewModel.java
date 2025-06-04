package com.example.bentory_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.repository.ProductRepository;

import java.util.List;

public class SellingViewModel extends ViewModel {

    // Repository
    private ProductRepository repository;

    // 📋 LiveData list of all products (observed by UI).
    public LiveData<List<ProductModel>> items;

    // ✅ LiveData to track if barcode update was successful.
    private MutableLiveData<Boolean> barcodeUpdateSuccess = new MutableLiveData<>();

    // ❌ LiveData to store error message if barcode update fails.
    private MutableLiveData<String> barcodeUpdateError = new MutableLiveData<>();


    // 🧠 Constructor — called when this ViewModel is created.
    public SellingViewModel() {
        // Connect to Firebase product data using the repository.
        repository = new ProductRepository();

        // Fetch all products and store in LiveData.
        items = repository.getData();
    }

    // 📤 Makes the product list observable for UI updates.
    public LiveData<List<ProductModel>> getItems() {
        return items;
    }

}
