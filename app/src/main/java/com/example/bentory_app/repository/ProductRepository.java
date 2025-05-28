package com.example.bentory_app.repository;

import android.util.Log;

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

    // Firebase reference pointing to the 'products' node in the RealTime Database
    private DatabaseReference database;
    public ProductRepository() {
        database = FirebaseDatabase.getInstance().getReference().child("products");
    }

    // Retrieve all products from Firebase and wraps the result in LiveData.
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
                            productModel.setId(dataSnapshot.getKey());
                            listData.add(productModel);
                        }
                    }
                    mutableData.setValue(listData); // Push updated list to observers.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: Handle read error.
            }
        });
        return mutableData;
    }



    // (ADDING PRODUCTS TO FIREBASE):
    // Adds a new product to Firebase with an (customized) auto-generated ID and timestamp.
    public void addProduct (ProductModel product) {
        // Fetch the existing data from the Firebase database.
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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

                // Generate new unique product ID (e.g, "item4").
                String itemKey = "item" + (maxId + 1);

                // Format and assign the current timestamp to product. Desired format: ("May 8, 2025 | 5:44 PM").
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy | h:mm a", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                String formattedDate = sdf.format(new Date());  // get the current date and time.
                product.setDate_Added(formattedDate);           // set formatted date.

                // Save product under new key.
                database.child(itemKey).setValue(product);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: Handle error.
            }
        });

    }



    // Deletes products by ID and re-numbers the remaining ones sequentially.
    public void deleteProductsByIds (Set<String> idsToDelete) {
        // Delete each selected item by its ID (key).
        for (String id : idsToDelete) {
            if (id != null && !id.trim().isEmpty()) {
                database.child(id).removeValue();
            } else {
                Log.e("DeleteProducts", "Null or empty ID encountered in deleteProductsByIds");
            }
        }

        // Reassign keys to remove gaps.
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ProductModel> remainingProducts = new ArrayList<>();

                // Collect all remaining products into a list.
                for (DataSnapshot child : snapshot.getChildren()) {
                    ProductModel product = child.getValue(ProductModel.class);
                    if (product != null) {
                        product.setId(child.getKey());
                        remainingProducts.add(product);
                    }
                }

                // Clear the entire database node.
                database.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // Reinsert remaining products with new sequential keys (item1, item2...).
                        for (int i = 0; i < remainingProducts.size(); i++) {
                            String newKey = "item" + (i + 1);
                            ProductModel updateProduct = remainingProducts.get(i);
                            updateProduct.setId(newKey); // update the product's ID to the new key.
                            database.child(newKey).setValue(updateProduct);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // optional: Handle error.
            }
        });
    }



    // Looks for a product where the first/only barcode matches.
    public void getProductByBarcode(String barcode, ProductCallback callback) {
        database.orderByChild("barcode").equalTo(barcode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                ProductModel product = child.getValue(ProductModel.class);
                                if (product != null) {
                                    callback.onProductFound(product);    // Product found
                                    return;
                                }
                            }
                        }
                        callback.onProductNotFound();                   // No matching product found
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());           // Handle Firebase query error
                    }
                });
    }



    // Looks through all products to find one that contains the given barcode in its barcode list.
    public void getProductByMatchingBarcode(String barcode, ProductCallback callback) {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ProductModel match = findMatchingProduct(snapshot, barcode);
                if (match != null) {
                    callback.onProductFound(match);
                } else {
                    callback.onProductNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }



    // Adds a new barcode to a product, if not already in the list and under the 5-barcode limit.
    public void addBarcodeToProduct(String productId, String newBarcode, BarcodeUpdateCallback callback) {
        database.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ProductModel product = snapshot.getValue(ProductModel.class);
                if (product != null) {
                    List<String> barcode = product.getBarcode();
                    if (barcode == null) barcode = new ArrayList<>();

                    if (barcode.contains(newBarcode)) {
                        callback.onFailure("Barcode already exists.");
                        return;
                    }

                    if (barcode.size() >= 5) {
                        callback.onFailure("Limit of 5 barcodes reached.");
                        return;
                    }

                    barcode.add(newBarcode);
                    product.setBarcode(barcode);

                    database.child(productId).setValue(product)
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                } else {
                    callback.onFailure("Product not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }



    // Updates an existing product record in Firebase.
    public void updateProduct(ProductModel updatedProduct) {
        if (updatedProduct == null || updatedProduct.getId() == null) return;
        database.child(updatedProduct.getId()).setValue(updatedProduct);
    }



    //// !!! CALLBACK INTERFACES !!! ////

    // Callback for product search based on barcode.
    public interface ProductCallback {
        void onProductFound(ProductModel product);
        void onProductNotFound();
        void onError(String error);
    }

    // Callback for adding a barcode to an existing product.
    public interface BarcodeUpdateCallback {
        void onSuccess();
        void onFailure(String error);
    }



    //// !!! METHODS !!! ////

    // Helper method to search all products and match any barcode in the list.
    private ProductModel findMatchingProduct(DataSnapshot snapshot, String barcode) {
        for (DataSnapshot child : snapshot.getChildren()) {
            ProductModel product = child.getValue(ProductModel.class);
            if (product != null && product.getBarcode() != null) {
                for (String b : product.getBarcode()) {
                    if (b.equals(barcode)) {
                        return product;
                    }
                }
            }
        }
        return null;
    }

}
