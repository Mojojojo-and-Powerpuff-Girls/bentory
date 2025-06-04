package com.example.bentory_app.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.repository.ProductRepository;

import java.util.List;
import java.util.Set;

public class ProductViewModel extends ViewModel {

    // Repository
    private ProductRepository repository;

    // 🧠 LiveData to observe a list of products (automatically updates UI when data changes)
    public LiveData<List<ProductModel>> items;

    // 🔍 Public getter to expose product list to UI as LiveData.
    public LiveData<List<ProductModel>> getItems() {
        return items;
    }

    // 🛠 Constructor - initialize repository and fetch LiveData from it.
    public ProductViewModel() {
        repository = new ProductRepository();   // Connect to Firebase.
        items = repository.getData();           // Retrieve and observe product list.
    }

    // ➕ Add a new product to Firebase.
    public void addProduct (ProductModel product) {
        repository.addProduct(product); //// 'addProduct()' contains a method (found at 'ProductRepository' in 'repository' directory).
    }

    // ❌ Delete multiple products using a set of product IDs
    public void deleteSelectedProducts(Set<String> idsToDelete) {
        repository.deleteProductsByIds(idsToDelete);
    }

    // 🧾 Search for a product by matching barcode.
    public void searchProductByBarcode(String barcode, ProductRepository.ProductCallback callback) {
        Log.d("ProductVM", "searchProductByBarcode called with: " + barcode);
        repository.getProductByMatchingBarcode(barcode, callback);
    }

    // 🔄 Update a product's info in Firebase.
    public void updateProduct(ProductModel updatedProduct) {
        repository.updateProduct(updatedProduct);
    }

}
