package com.example.bentory_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.repository.ProductRepository;

import java.util.List;
import java.util.Set;

public class ProductViewModel extends ViewModel {

    // Repository instance used to abstract and manage all Firebase operations.
    private ProductRepository repository;

    // LiveData that holds the current list of products retrieved from the repository.
    public LiveData<List<ProductModel>> items;
    public LiveData<List<ProductModel>> getItems() {
        return items;
    }



    // Constructor for the ViewModel.
    // Initializes the repository and fetches the product list from Firebase through the repository.
    public ProductViewModel() {
        // Connect LiveData to repository’s product list.
        repository = new ProductRepository();
        items = repository.getData(); //// 'getData()' contains a method (found at 'ProductRepository' in 'repository' directory).
    }



    // (ADDING PRODUCTS TO FIREBASE):
    // Adds a new product to Firebase through the repository.
    public void addProduct (ProductModel product) {
        repository.addProduct(product); //// 'addProduct()' contains a method (found at 'ProductRepository' in 'repository' directory).
    }



    // Delete multiple products from Firebase using their IDs.
    public void deleteSelectedProducts(Set<String> idsToDelete) {
        repository.deleteProductsByIds(idsToDelete); //// 'deleteProductsByIds()' contains a method (found at 'ProductRepository' in 'repository' directory).
    }



    // Updates an existing product in Firebase.
    public void updateProduct(ProductModel updatedProduct) {
        repository.updateProduct(updatedProduct); //// 'updateProduct()' contains a method (found at 'ProductRepository' in 'repository' directory).
    }

}
