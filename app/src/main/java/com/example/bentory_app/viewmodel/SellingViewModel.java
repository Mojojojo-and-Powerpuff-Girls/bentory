package com.example.bentory_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.repository.ProductRepository;

import java.util.List;

public class SellingViewModel extends ViewModel {
    private ProductRepository repository;
    public LiveData<List<ProductModel>> items;

    // COMMENTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
    // LiveData for observing barcode update success or failure
    private MutableLiveData<Boolean> barcodeUpdateSuccess = new MutableLiveData<>();
    private MutableLiveData<String> barcodeUpdateError = new MutableLiveData<>();




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






    // COMMENTTTTTTTTTTTTTTTTTTTT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //      * Looks up a product by a matching barcode, including support for multi-barcode fields.
    //     * WHY: Enables detection of existing products when scanning a barcode.
    public void getProductByBarcode(String barcode, ProductRepository.ProductCallback callback) {
        repository.getProductByMatchingBarcode(barcode, callback);
    }

    // New: LiveData getters for barcode update feedback
    public LiveData<Boolean> getBarcodeUpdateSuccess() {
        return barcodeUpdateSuccess;
    }

    public LiveData<String> getBarcodeUpdateError() {
        return barcodeUpdateError;
    }

    // New: Add a barcode to a product with callbacks for success/failure
    public void addBarcodeToProduct(String productId, String newBarcode) {
        repository.addBarcodeToProduct(productId, newBarcode, new ProductRepository.BarcodeUpdateCallback() {
            @Override
            public void onSuccess() {
                barcodeUpdateSuccess.postValue(true);
            }

            @Override
            public void onFailure(String error) {
                barcodeUpdateError.postValue(error);
            }
        });
    }

    public void updateProduct(ProductModel updatedProduct) {
        repository.updateProduct(updatedProduct);
    }

}
