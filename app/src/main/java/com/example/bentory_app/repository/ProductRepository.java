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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

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


    // 1. Method to add a new product to Firebase Realtime Database
    // WHY: This method handles saving the product data to Firebase under a unique key.
    public void addProduct (ProductModel product) {

        // Step 1: Fetch the existing data from the Firebase database.
        // WHY: We need to know how many items are already stored to generate a new, unique key for the new item.
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Step 2: Find the highest numbered item key (e.g., "item3", "item4").
                // WHY: So we can create a new item key that doesn't conflict (e.g., "item5").
                long maxId = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    if (key != null && key.startsWith("item")) {
                        try {
                            long idNum = Long.parseLong(key.replace("item", ""));
                            if (idNum > maxId) {
                                maxId = idNum;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                String itemKey = "item" + (maxId + 1);

                // Step 3: Format the current date into a readable format and set the correct timezone
                // WHY: This step ensures the date is displayed in the desired format ("May 8, 2025 | 5:44 PM")
                // and in the correct timezone, regardless of the device's locale or timezone settings.
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy | h:mm a", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                String formattedDate = sdf.format(new Date()); // get the current date and time
                product.setDate_Added(formattedDate); // set formatted date

                // Step 4: Save the product data under the generated unique key.
                // WHY: This stores the product's information in the Firebase database under the new key.
                product.setId(itemKey); // Save the key in the product model for multiple deletion
                database.child(itemKey).setValue(product);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

    }


    // 2. Method to delete multiple products by their IDs from Firebase
    // WHY: Centralizes deletion logic in repository, keeping activity/viewmodel clean.
    public void deleteProductsByIds (Set<String> idsToDelete) {

        // Step 1: Delete each selected item by its ID (key).
        // WHY: This removes the chosen items from the database.
        for (String id : idsToDelete) {
            database.child(id).removeValue();
        }

        // Step 2: After deletion, fetch all remaining products.
        // WHY: We'll need to reassign new sequential keys to remaining products.
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ProductModel> remainingProducts = new ArrayList<>();

                // Step 3: Collect all remaining products into a list.
                // WHY: We need to temporarily store them so we can reinsert them with new keys.
                for (DataSnapshot child : snapshot.getChildren()) {
                    ProductModel product = child.getValue(ProductModel.class);
                    if (product != null) {
                        remainingProducts.add(product);
                    }
                }

                // Step 4: Clear the entire database node.
                // WHY: This ensures we remove all old keys before reinserting.
                database.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Step 5: Reinsert remaining products with new sequential keys (item1, item2...).
                        // WHY: This keeps the keys ordered and avoids gaps like "item1", "item3".
                        for (int i = 0; i < remainingProducts.size(); i++) {
                            String newKey = "item" + (i + 1);
                            ProductModel updateProduct = remainingProducts.get(i);
                            updateProduct.setId(newKey); // update the product's ID to the new key
                            database.child(newKey).setValue(updateProduct);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

}
