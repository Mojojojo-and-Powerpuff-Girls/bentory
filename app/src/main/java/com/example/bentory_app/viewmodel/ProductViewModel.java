package com.example.bentory_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.repository.ProductRepository;

import java.util.List;
public class ProductViewModel extends ViewModel {
    private ProductRepository repository;
    public LiveData<List<ProductModel>> items;

    public ProductViewModel() {
        repository = new ProductRepository();
        items = repository.getData();
    }

    public LiveData<List<ProductModel>> getItems() {
        return items;
    }


    // Method to add a new product through the repository, this method acts as a middle layer between the UI and
    // the repo, delegating the actual database operation to the repository to maintain a clean separation of concerns.
    public void addProduct (ProductModel product) {
        // Step 1: Pass the product object to the repository for saving to the database.
        // WHY: The repository is responsible for managing data operations, including adding products to Firebase.
        repository.addProduct(product);
    }

}
