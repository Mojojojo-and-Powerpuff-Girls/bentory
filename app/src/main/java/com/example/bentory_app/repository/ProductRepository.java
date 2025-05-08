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


    // Method to add a new product to Firebase Realtime Database
    // WHY: This method handles saving the product data to Firebase under a unique key.
    public void addProduct (ProductModel product) {

        // Step 1: Fetch the existing data from the Firebase database.
        // WHY: We need to know how many items are already stored to generate a new, unique key for the new item.
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Step 2: Get the current count of existing items in the database.
                // WHY: The count allows us to generate a unique key like "item1", "item2", etc.
                long count = snapshot.getChildrenCount();
                String itemKey = "item" + (count + 1); // Generate key like "item1"

                // Step 3: Save the product data under the generated unique key.
                // WHY: This stores the product's information in the Firebase database under the new key.
                database.child(itemKey).setValue(product);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

}
