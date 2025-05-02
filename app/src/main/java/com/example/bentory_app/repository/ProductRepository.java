package com.example.bentory_app.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.bentory_app.model.ProductModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
public class ProductRepository {

    private DatabaseReference database;

    public ProductRepository() {
        database = FirebaseDatabase.getInstance().getReference().child("products");
    }

    public LiveData<List<ProductModel>> getData() {

        MutableLiveData<List<ProductModel>> mutableData = new MutableLiveData<>();
        database.addValueEventListener(new ValueEventListener() {
            final List<ProductModel> listData = new ArrayList<>();
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listData.clear();

                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        ProductModel productModel = dataSnapshot.getValue(ProductModel.class);
                        if (productModel != null){
                            listData.add(productModel);
                        }
                    }
                    mutableData.setValue(listData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return mutableData;
    }

}
