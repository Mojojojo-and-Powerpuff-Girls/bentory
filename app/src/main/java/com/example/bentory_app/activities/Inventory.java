package com.example.bentory_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.repository.ProductRepository;
import com.example.bentory_app.subcomponents.InventoryAdapter;
import com.example.bentory_app.viewmodel.ProductViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Inventory extends AppCompatActivity {

    // ViewModels
    private ProductViewModel productViewModel;

    // Adapter
    private InventoryAdapter adapter;

    // XML UI
    private RecyclerView recyclerView;
    private ImageButton dltButton, filterBtn, searchScanBtn, backBtn;
    private EditText searchEditText;
    private DecoratedBarcodeView searchBarcode;
    private View searchTargetOverlay, touchBlock;

    // Data Types
    private String scannedBarcode;
    private boolean isScannerActive = false;
    private List<ProductModel> fullProductList; // store full list for filtering

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory);

        // Initialize Views FIRST
        recyclerView = findViewById(R.id.recyclerView);
        dltButton = findViewById(R.id.deleteBtn);
        filterBtn = findViewById(R.id.filterBtn);
        searchEditText = findViewById(R.id.searchView);
        searchScanBtn = findViewById(R.id.searchScannerBtn);
        searchBarcode = findViewById(R.id.barcodeScanner);
        searchTargetOverlay = findViewById(R.id.targetOverlay);
        touchBlock = findViewById(R.id.touchBlocker);
        backBtn = findViewById(R.id.back_btn);

        // Get scanned barcode
        scannedBarcode = getIntent().getStringExtra("scannedBarcode");

        // System bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter(this, new ArrayList<>(), item -> {
            if (scannedBarcode != null && !scannedBarcode.isEmpty()) {
                showProductSelectConfirmation(item);
            } else {
                showBottomSheet(item);
            }
        });
        recyclerView.setAdapter(adapter);

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Observe data from ViewModel
        productViewModel.getItems().observe(this, itemList -> {
            fullProductList = new ArrayList<>(itemList); // For filtering
            adapter.updateData(itemList); // Just update data — don't reinitialize adapter
        });

        // Filter Button
        filterBtn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Inventory.this, filterBtn);
            popupMenu.getMenuInflater().inflate(R.menu.menu_filter, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (fullProductList == null) return false;
                List<ProductModel> sortedList = new ArrayList<>(fullProductList);
                if (item.getItemId() == R.id.menu_az) {
                    Collections.sort(sortedList, Comparator.comparing(ProductModel::getName));
                } else if (item.getItemId() == R.id.menu_za) {
                    Collections.sort(sortedList, Comparator.comparing(ProductModel::getName).reversed());
                }
                adapter.updateData(sortedList);
                return true;
            });
            popupMenu.show();
        });

        // Search Input
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Search by barcode
        searchBarcode.decodeContinuous(callback);
        searchScanBtn.setOnClickListener(v -> {
            if (isScannerActive) {
                searchBarcode.setVisibility(View.GONE);
                searchTargetOverlay.setVisibility(View.GONE);
                touchBlock.setVisibility(View.GONE);
                searchBarcode.pause();
            } else {
                searchBarcode.setVisibility(View.VISIBLE);
                searchTargetOverlay.setVisibility(View.VISIBLE);
                touchBlock.setVisibility(View.VISIBLE);
                searchBarcode.resume();
            }
            isScannerActive = !isScannerActive; // Update scanner state flag
        });

        // Delete Mode Logic
        dltButton.setOnClickListener(v -> {
            boolean isDeleteModeActive = !adapter.getDeleteMode();
            if (isDeleteModeActive) {
                adapter.setDeleteMode(true);
                dltButton.setImageResource(R.drawable.trash);
            } else {
                Set<String> selectedItems = adapter.getSelectedItems();
                if (!selectedItems.isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Items")
                            .setMessage("Are you sure you want to delete the selected items?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                productViewModel.deleteSelectedProducts(selectedItems);
                                adapter.setDeleteMode(false);
                                dltButton.setImageResource(R.drawable.select_items);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                adapter.setDeleteMode(false);
                                dltButton.setImageResource(R.drawable.select_items);
                            }).show();
                } else {
                    adapter.setDeleteMode(false);
                    dltButton.setImageResource(R.drawable.select_items);
                }
            }
        });

        backBtn.setOnClickListener(v -> {
            finish();


//            if (isScannerActive) {
//                // Hide scanner if it's active
//                searchBarcode.setVisibility(View.GONE);
//                searchTargetOverlay.setVisibility(View.GONE);
//                touchBlock.setVisibility(View.GONE);
//                backBtn.setVisibility(View.GONE); // hide back button again
//                searchBarcode.pause();
//                isScannerActive = false;
//            } else {
//                // Otherwise, act like a normal back press
//                finish();
//            }
        });



    }



    //// !!! METHODS OUTSIDE onCreate !!! ////

    // Filtering Search Results
    // Called when user types in the search field.
    private void filterProducts(String query) {
        if (fullProductList == null) return;

        List<ProductModel> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(fullProductList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ProductModel product : fullProductList) {
                if (product.getName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(product);
                }
            }
        }
        adapter.updateData(filteredList);
    }



    // Confirm Barcode Addition
    // Called when user selects a product to link a new scanned barcode.
    private void showProductSelectConfirmation(ProductModel product) {
        // Check if barcode already exists
        if (product.getBarcode().contains(scannedBarcode)) {
            Toast.makeText(this, "Barcode already linked to this product.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the product already has 5 barcodes (limit)
        if (product.getBarcode().size() >= 5) {
            Toast.makeText(this, "This product already has 5 barcodes. Cannot add more.", Toast.LENGTH_LONG).show();
            return;
        }

        // Prompt user for confirmation to link the barcode.
        new android.app.AlertDialog.Builder(this)
                .setTitle("Link Barcode")
                .setMessage("Add barcode " + scannedBarcode + " to product " + product.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    product.getBarcode().add(scannedBarcode);
                    productViewModel.updateProduct(product); // Update product in Firebase.
                    Toast.makeText(this, "Barcode linked to " + product.getName(), Toast.LENGTH_SHORT).show();
                    finish(); // Return to previous activity (SellProduct).
                })
                .setNegativeButton("No", null)
                .show();
    }



    // Bottom Sheet: Display Product Details
    private void showBottomSheet(ProductModel product) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_product, null);
        bottomSheetView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                700 // Fixed height for the bottom sheet.
        ));

        // Bind UI Elements.
        TextView name = bottomSheetView.findViewById(R.id.product_name);
        TextView detailsLabel = bottomSheetView.findViewById(R.id.product_details_label);
        TextView details = bottomSheetView.findViewById(R.id.product_details);

        // Set data.
        name.setText(product.getName());

        String combinedDetailsLabel = "Category: " +  "\n" +
                "Quantity: " +  "\n" +
                "Cost Price: " +  "\n" +
                "Sale Price: " + "\n" +
                "Size: " +  "\n" +
                "Weight: " + "\n" +
                "Description: ";

        detailsLabel.setText(combinedDetailsLabel);

        String combinedDetails = product.getCategory() + "\n" +
                product.getQuantity() + "\n" +
                product.getCost_Price() + "\n" +
                product.getSale_Price() + "\n" +
                product.getSize() + "\n" +
                product.getWeight() + "\n" +
                product.getDescription();

        details.setText(combinedDetails);

        // Show the bottom sheet.
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            String searchScannedBarcode = result.getText();
            Log.d("BarcodeScan", "Scanned Barcode: " + searchScannedBarcode);

            if (searchScannedBarcode != null && !searchScannedBarcode.isEmpty()) {
                searchBarcode.pause(); // Pause to prevent multiple scans
                Log.d("BarcodeScan", "Calling ViewModel to search product...");

                // Search using viewmodel.
                productViewModel.searchProductByBarcode(searchScannedBarcode, new ProductRepository.ProductCallback() {
                    @Override
                    public void onProductFound(ProductModel product) {
                        Log.d("BarcodeScan", "Product found: " + product.getName());
                        List<ProductModel> scannedList = new ArrayList<>();
                        scannedList.add(product);
                        adapter.updateData(scannedList);

                        recyclerView.setVisibility(View.VISIBLE); // <-- Ensure it's visible
                        searchBarcode.setVisibility(View.GONE);
                        searchTargetOverlay.setVisibility(View.GONE);
                        touchBlock.setVisibility(View.GONE);
                        isScannerActive = false;
                    }

                    @Override
                    public void onProductNotFound() {
                        Log.d("BarcodeScan", "Product NOT found");
                        Toast.makeText(getApplicationContext(), "Product not found", Toast.LENGTH_SHORT).show();
                        searchBarcode.resume();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("BarcodeScan", "Error: " + error);
                        Toast.makeText(getApplicationContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        searchBarcode.resume();
                    }
                });
            } else {
                Log.d("BarcodeScan", "Scanned barcode is empty or null");
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // Optional.
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (isScannerActive) searchBarcode.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        searchBarcode.pause();
    }
}