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

}
