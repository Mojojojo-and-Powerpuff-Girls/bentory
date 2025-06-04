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

    // Firebase reference pointing to the 'products' node in the RealTime Database.
    private DatabaseReference database;

    // Listener for data changes (used for testing or customization).
    private ValueEventListener listener;

    // Default Constructor initializes reference to "products" node in firebase.
    public ProductRepository() {
        this(FirebaseDatabase.getInstance().getReference().child("products"), null);
    }

    // This constructor allows injecting a custom listener (for testing).
    public ProductRepository(DatabaseReference databaseReference, ValueEventListener listener) {
        this.database = databaseReference;
        this.listener = listener;
    }


    // ===============================
    // 📦 All Products : Fetch all products as LiveData so UI can observe real-time updates. (METHODS)
    // ===============================
    public LiveData<List<ProductModel>> getData() {
        MutableLiveData<List<ProductModel>> mutableData = new MutableLiveData<>();

        // Use custom listener if injected, else create a default listener.
        ValueEventListener valueEventListener = listener != null ? listener : new ValueEventListener() {
            final List<ProductModel> listData = new ArrayList<>();
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listData.clear();

                    // Loop through all product nodes under 'products'.
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        ProductModel productModel = dataSnapshot.getValue(ProductModel.class);
                        if (productModel != null){

                            // Set product ID from the Firebase key (e.g. item1, item2).
                            productModel.setId(dataSnapshot.getKey());
                            listData.add(productModel);
                        }
                    }
                    mutableData.setValue(listData);         // notify observers with the updated product list.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: Handle errors if needed.
            }
        };
        database.addValueEventListener(valueEventListener); // Attach listener to Firebase database for live updates.
        return mutableData;
    }


    // ===============================
    // ➕🪪🕑 Add products with ID and Timestamp : Add a new product to Firebase with an auto-generated sequential ID and timestamp. (METHODS)
    // ===============================
    public void addProduct (ProductModel product) {
        // Read current data to determine the next product ID.
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long maxId = 0;
                // Find the highest numeric suffix among existing keys like ("item1", "item2")
                for (DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    if (key != null && key.startsWith("item")) {
                        try {
                            long idNum = Long.parseLong(key.replace("item", ""));
                            if (idNum > maxId) {
                                maxId = idNum;
                            }
                        } catch (NumberFormatException ignored) {
                            // Ignore if key is malformed.
                        }
                    }
                }

                // Generate new key with incremented number (e.g. item4).
                String itemKey = "item" + (maxId + 1);

                // Format and assign the current timestamp to product. Desired format: ("May 8, 2025 | 5:44 PM").
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy | h:mm a", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                String formattedDate = sdf.format(new Date());  // get the current date and time.
                product.setDate_Added(formattedDate);           // set formatted date.

                // Save new product under generated key.
                database.child(itemKey).setValue(product);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: Handle errors during read operation.
            }
        });

    }


    // ===============================
    // 🗑️ Delete Logic : Deletes multiple products by ID and re-numbers the remaining ones sequentially. (METHODS)
    // ===============================
    public void deleteProductsByIds (Set<String> idsToDelete) {
        // Delete each selected item by its ID (key).
        for (String id : idsToDelete) {
            if (id != null && !id.trim().isEmpty()) {
                database.child(id).removeValue();
            } else {
                Log.e("DeleteProducts", "Null or empty ID encountered in deleteProductsByIds");
            }
        }

        // After deletion, reassign keys to ensure sequential numbering with no gaps.
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

                // Clear entire 'products' node to rewrite with sequential keys.
                database.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // Reinsert products with new sequential keys (item1, item2...).
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
                // optional: Handle error on read operation.
            }
        });
    }


    // ===============================
    // 🔍 Match barcode : Finds product by scanning through all products and matching any barcode in a list of barcodes. (METHODS)
    // ===============================
    public void getProductByMatchingBarcode(String barcode, ProductCallback callback) {
        Log.d("ProductRepo", "getProductByMatchingBarcode called with: " + barcode);

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("ProductRepo", "Data snapshot received");
                ProductModel match = findMatchingProduct(snapshot, barcode); //// 'findMatchingProduct' contains a helper method (found at the bottom of the code).
                if (match != null) {
                    Log.d("ProductRepo", "Product match found: " + match.getName());
                    callback.onProductFound(match);
                } else {
                    Log.d("ProductRepo", "No product match found");
                    callback.onProductNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProductRepo", "Firebase error: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }


    // ===============================
    // ➕ Link New Barcode : Adds a new barcode to an existing product if it doesn't exist yet and Maximum of 5 barcode only. (METHODS)
    // ===============================
    public void addBarcodeToProduct(String productId, String newBarcode, BarcodeUpdateCallback callback) {
        database.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ProductModel product = snapshot.getValue(ProductModel.class);
                if (product != null) {
                    List<String> barcode = product.getBarcode();
                    if (barcode == null) barcode = new ArrayList<>();

                    // Prevent duplicate barcodes.
                    if (barcode.contains(newBarcode)) {
                        callback.onFailure("Barcode already exists.");
                        return;
                    }

                    // Enforce maximum limit of 5 barcodes per product.
                    if (barcode.size() >= 5) {
                        callback.onFailure("Limit of 5 barcodes reached.");
                        return;
                    }

                    // Add the new barcode and update Firebase.
                    barcode.add(newBarcode);
                    product.setBarcode(barcode);

                    database.child(productId).setValue(product)
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                } else {
                    // Product not found in Firebase.
                    callback.onFailure("Product not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }


    // ===============================
    // 📝 Update Product : Updates the entire product record in Firebase using product ID as key. (METHODS)
    // ===============================
    public void updateProduct(ProductModel updatedProduct) {
        if (updatedProduct == null || updatedProduct.getId() == null) return;
        database.child(updatedProduct.getId()).setValue(updatedProduct);
    }


    // ===============================
    // 🔍 Helper Method : Search all products for one with a barcode list containing the scanned barcode. (METHODS)
    // ===============================
    private ProductModel findMatchingProduct(DataSnapshot snapshot, String barcode) {
        // DEBUGGER
        if (barcode == null) {
            Log.d("ProductRepo", "Scanned barcode is null!");
            return null;
        }
        // Iterate all products and their barcodes.
        for (DataSnapshot child : snapshot.getChildren()) {
            ProductModel product = child.getValue(ProductModel.class);
            if (product != null && product.getBarcode() != null) {
                for (String b : product.getBarcode()) {
                    Log.d("ProductRepo", "Checking product barcode: " + b + " against scanned barcode: " + barcode);
                    if (b != null && barcode != null && b.trim().equals(barcode.trim())) {
                        Log.d("ProductRepo", "Match found: " + product.getName());
                        return product;
                    }
                }
            }
        }
        Log.d("ProductRepo", "No matching product found for barcode " + barcode);
        return null;
    }


    // ===============================
    //            INTERFACES
    // ===============================

    // 📦 Callback for product lookup by barcode. (INTERFACES)
    public interface ProductCallback {
        void onProductFound(ProductModel product);
        void onProductNotFound();
        void onError(String error);
    }

    // ➕ Callback for adding a barcode to an existing product. (INTERFACES)
    public interface BarcodeUpdateCallback {
        void onSuccess();
        void onFailure(String error);
    }

}
