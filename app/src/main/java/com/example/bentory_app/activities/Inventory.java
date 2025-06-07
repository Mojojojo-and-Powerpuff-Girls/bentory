package com.example.bentory_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.appcompat.widget.Toolbar;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

// ===============================
// Inventory Activity
//
// Purpose:
// - Displays a list of all available products.
// - Allows filtering, searching, and managing inventory items.
// - Supports viewing/editing items via bottom sheet.
// - Includes barcode scanning functionality for quick product lookup.
// ===============================
public class Inventory extends BaseDrawerActivity { // Changed from BaseActivity to BaseDrawerActivity

    // UI Components
    private RecyclerView recyclerView;
    private ImageButton dltButton, filterBtn, searchScanBtn, backBtn;
    private EditText searchEditText;
    private DecoratedBarcodeView searchBarcode;
    private View searchTargetOverlay, touchBlock;

    // State
    private boolean isScannerActive = false;
    private boolean isDeleteModeActive;
    private String scannedBarcode;
    private int sizeInDp;
    private int sizeInPx;
    private float scale;
    private List<ProductModel> fullProductList; // store full list for filtering

    // ViewModel
    private ProductViewModel productViewModel;

    // Adapter
    private InventoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory);

        // ⬛ UI Setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        setupToolbar(R.id.my_toolbar, "Inventory", true); //// 'setupToolbar' contains a method (found at 'BaseDrawerActivity' in 'activities' directory).
        setupDrawer(); //// 'setupDrawer' contains a method (found at 'BaseDrawerActivity' in 'activities' directory).

        // ⬛ Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // ⬛ Bind Views
        recyclerView = findViewById(R.id.recyclerView);
        dltButton = findViewById(R.id.deleteBtn);
        filterBtn = findViewById(R.id.filterBtn);
        searchEditText = findViewById(R.id.searchView);
        searchScanBtn = findViewById(R.id.searchScannerBtn);
        searchBarcode = findViewById(R.id.barcodeScanner);
        searchTargetOverlay = findViewById(R.id.targetOverlay);
        touchBlock = findViewById(R.id.touchBlocker);
        backBtn = findViewById(R.id.back_btn);

        // 🔎 Retrieve the scanned barcode passed from the previous activity via intent [used to search for matching product code].
        scannedBarcode = getIntent().getStringExtra("scannedBarcode");

        // ===============================
        // 📦🔎 Set up RecyclerView: Handles product display for inventory view and Supports barcode-linking when a scan fails to find a match. (FEATURE)
        // ===============================
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter(this, new ArrayList<>(), item -> {
            if (scannedBarcode != null && !scannedBarcode.isEmpty()) {
                //// 'showProductSelectConfirmation' contains a method (found at the bottom of the code).
                showProductSelectConfirmation(item); // when barcode is used for searching product.
            } else {
                //// 'showBottomSheet' contains a method (found at the bottom of the code).
                showBottomSheet(item);               // when browsing manually.
            }
        });
        recyclerView.setAdapter(adapter);


        // 🔁 Observe data from ViewModel : Keeps product data up to date and refreshes UI.
        productViewModel.getItems().observe(this, itemList -> {
            fullProductList = new ArrayList<>(itemList); // keep original list for filtering.
            //// 'updateData' contains a method (found at 'InventoryAdapter' in 'subcomponents' directory).
            adapter.updateData(itemList);                // update only data, not adapter reference.
        });

        // ===============================
        // 🔽 Filter Button Setup: Show sorting options for the inventory list [A-Z / Z-A]. (FEATURE)
        // ===============================
        filterBtn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(Inventory.this, filterBtn);
            //// 'menu_filter' is manually created (found at 'menu' in 'res' directory).
            popupMenu.getMenuInflater().inflate(R.menu.menu_filter, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (fullProductList == null)
                    return false;
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


        // ===============================
        // 🔍 Search Input: Filter product list as user types in search field. (FEATURE)
        // ===============================
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString()); //// 'filterProducts' contains a method (found at the bottom of the code).
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        // ===============================
        // 📷 Barcode Scanner Setup: Enable continuous barcode scanning for product search [barcode search button]. (FEATURE)
        // ===============================
        searchBarcode.decodeContinuous(callback); //// 'callback' contains a method (found at the bottom of the code).
        searchScanBtn.setOnClickListener(v -> {
            if (isScannerActive) {
                // Disable scanner UI and pause scanning.
                searchBarcode.setVisibility(View.GONE);
                searchTargetOverlay.setVisibility(View.GONE);
                touchBlock.setVisibility(View.GONE);
                searchBarcode.pause();
            } else {
                // Enable scanner UI and resume scanning.
                searchBarcode.setVisibility(View.VISIBLE);
                searchTargetOverlay.setVisibility(View.VISIBLE);
                touchBlock.setVisibility(View.VISIBLE);
                searchBarcode.resume();
            }
            isScannerActive = !isScannerActive; // Update scanner state flag
        });


        // ===============================
        // 🗑️️ Delete Mode: Allow users to select and delete multiple products. (FEATURE)
        // ===============================
        dltButton.setOnClickListener(v -> {
            isDeleteModeActive = !adapter.getDeleteMode(); //// 'getDeleteMode' contains a method (found at 'InventoryAdapter' in 'subcomponents' directory).
            // Activate delete mode: allow item selection for deletion.
            if (isDeleteModeActive) {
                adapter.setDeleteMode(true); //// 'setDeleteMode' contains a method (found at 'InventoryAdapter' in 'subcomponents' directory).
                dltButton.setImageResource(R.drawable.delete_icon);

                // Shrink the delete icon.
                sizeInDp = 50;
                scale = getResources().getDisplayMetrics().density;
                sizeInPx = (int) (sizeInDp * scale + 0.5f);

                ViewGroup.LayoutParams params = dltButton.getLayoutParams();
                dltButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
                params.width = sizeInPx;
                params.height = sizeInPx;
                dltButton.setLayoutParams(params);

            } else {
                // Deactivate delete mode.
                Set<String> selectedItems = adapter.getSelectedItems(); //// 'getSelectedItems' contains a method (found at 'InventoryAdapter' in 'subcomponents' directory).
                if (!selectedItems.isEmpty()) {
                    // Confirm deletion if items selected.
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Items")
                            .setMessage("Are you sure you want to delete the selected items?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                productViewModel.deleteSelectedProducts(selectedItems); //// 'deleteSelectedProducts' contains a method (found at 'ProductViewModel' in 'viewmodel' directory).
                                adapter.setDeleteMode(false);
                                resetDeleteButtonToSelectItems(); //// 'resetDeleteButtonToSelectItems' contains a method (found at the bottom of the code).
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                adapter.setDeleteMode(false);
                                resetDeleteButtonToSelectItems();
                            }).show();
                } else {
                    // No items selected, just exit delete mode.
                    adapter.setDeleteMode(false);
                    resetDeleteButtonToSelectItems();
                }
            }
        });


        // ===============================
        // 🔙 Back Button: Close Activity. (FEATURE)
        // ===============================
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchBarcode.getVisibility() == View.VISIBLE) {
                    // Hide scanner and show form
                    searchBarcode.setVisibility(View.GONE);
                    searchTargetOverlay.setVisibility(View.GONE);
                    touchBlock.setVisibility(View.GONE);
                    searchBarcode.pause(); // stop scanning
                } else {
                    // Normal back behavior — e.g., finish activity
                    finish();
                }
            }
        });
    }


    // ===============================
    //             METHODS
    // ===============================

    // 🗑️ 'resetDeleteButtonToSelectItems' : reset delete button to select items without changing the size. (METHODS)
    private void resetDeleteButtonToSelectItems() {
        dltButton.setImageResource(R.drawable.select_items);
        dltButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // Reset scaleType

        // Reset layout size.
        ViewGroup.LayoutParams params = dltButton.getLayoutParams();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dltButton.setLayoutParams(params);
    }

    // 🔎 'filterProducts' : Filters the product list based on user query from the search input. (METHODS)
    private void filterProducts(String query) {
        if (fullProductList == null)
            return;

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


    // ✅ 'showProductSelectConfirmation' : Called when user selects a product to link a new scanned barcode. (METHODS)
    private void showProductSelectConfirmation(ProductModel product) {
        // Check if barcode already exists
        if (product.getBarcode().contains(scannedBarcode)) {
            showBottomSheet(product);
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


    // 📋 'showBottomSheet' : Display product details and Editing via bottom sheet dialog. (METHODS)
    private void showBottomSheet(ProductModel product) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_product, null);
        bottomSheetView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1000 // Fixed height for the bottom sheet.
        ));

        // Bind UI Elements.
        TextView detailsLabel = bottomSheetView.findViewById(R.id.product_details_label);
        ImageButton editButton = bottomSheetView.findViewById(R.id.editBtn);
        EditText name = bottomSheetView.findViewById(R.id.product_name);
        EditText category = bottomSheetView.findViewById(R.id.edit_category);
        EditText quantity = bottomSheetView.findViewById(R.id.edit_quantity);
        EditText costPrice = bottomSheetView.findViewById(R.id.edit_cost_price);
        EditText salePrice = bottomSheetView.findViewById(R.id.edit_sale_price);
        EditText size = bottomSheetView.findViewById(R.id.edit_size);
        EditText weight = bottomSheetView.findViewById(R.id.edit_weight);
        EditText description = bottomSheetView.findViewById(R.id.edit_description);
        EditText barcode_list = bottomSheetView.findViewById(R.id.edit_barcodes);

        // Set product data to views.
        name.setText(product.getName());
        category.setText(product.getCategory());
        quantity.setText(String.valueOf(product.getQuantity()));
        costPrice.setText(String.format("%.2f", product.getCost_Price())); // if you want two decimal places
        salePrice.setText(String.format("%.2f", product.getSale_Price()));
        size.setText(product.getSize());
        weight.setText(product.getWeight());
        description.setText(product.getDescription());

        // Label with field names.
        String combinedDetailsLabel = "Category: " + "\n" +
                "Quantity: " + "\n" +
                "Cost Price: " + "\n" +
                "Sale Price: " + "\n" +
                "Size: " + "\n" +
                "Weight: " + "\n" +
                "Description: " + "\n" +
                "Barcode List: ";

        // Join barcode list with new lines.
        String barcodeListText = TextUtils.join("\n", product.getBarcode());

        detailsLabel.setText(combinedDetailsLabel);
        barcode_list.setText(barcodeListText);

        // Add a textWatcher to limit barcode input to a maximum of 5 lines.
        barcode_list.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used, but required to implement interface.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used, but required to implement interface.
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Prevent the watcher from triggering edits recursively.
                barcode_list.removeTextChangedListener(this);

                // Split input into lines (barcodes are entered one per line).
                String[] lines = s.toString().split("\n");

                // If the number of lines exceeds 5, trim it to only keep the first 5.
                if (lines.length > 5) {
                    StringBuilder trimmed = new StringBuilder();

                    // Append only the first 5 lines back into the EditText.
                    for (int i=0; i < 5; i++) {
                        trimmed.append(lines[i]);
                        if (i < 4) trimmed.append("\n"); // Add newline after each line except the last one.
                    }
                    // Set the trimmed text and move the cursor to the end.
                    barcode_list.setText(trimmed.toString());
                    barcode_list.setSelection(barcode_list.getText().length()); // Move cursor to end.
                }
                // Reattach the listener after modification is complete.
                barcode_list.addTextChangedListener(this);
            }
        });

        // Toggle edit mode on edit button click.
        editButton.setOnClickListener(v -> {
            boolean isEditing = !category.isEnabled(); // If fields are disabled, entering edit mode.

            if (!isEditing) {
                // Validate all required fields.
                if (!validateField(name, "Name cannot be empty")) //// 'validateField' contains a method (found at the bottom of the code).
                    return;
                if (!validateField(category, "Category cannot be empty"))
                    return;
                if (!validateField(quantity, "Quantity cannot be empty"))
                    return;
                if (!validateField(costPrice, "Cost Price cannot be empty"))
                    return;
                if (!validateField(salePrice, "Sale Price cannot be empty"))
                    return;
                if (!validateField(barcode_list, "Barcode list cannot be empty"))
                    return;

                // Save data
                product.setName(name.getText().toString().trim());
                product.setCategory(category.getText().toString().trim());
                product.setQuantity(Integer.parseInt(quantity.getText().toString().trim()));
                product.setCost_Price(Double.parseDouble(costPrice.getText().toString().trim()));
                product.setSale_Price(Double.parseDouble(salePrice.getText().toString().trim()));
                product.setSize(size.getText().toString().trim());
                product.setWeight(weight.getText().toString().trim());
                product.setDescription(description.getText().toString().trim());

                // Optional fields: size, weight, description.
                product.setSize(getOptionalValue(size)); //// 'getOptionalValue' contains a method (found at the bottom of the code).
                product.setWeight(getOptionalValue(weight));
                product.setDescription(getOptionalValue(description));

                // Parse updated barcode list (comma-separated)
                String rawInput = barcode_list.getText().toString().trim();
                String[] rawBarcodes = rawInput.split("\\n");

                List<String> cleanedBarcodes = new ArrayList<>();
                // Validate the length of the barcode. Must be 13 digits.
                for (String code : rawBarcodes) {
                    code = code.trim();
                    if (code.isEmpty()) continue;

                    if (code.length() != 13 || !code.matches("\\d{13}")) {
                        Toast.makeText(this, "Barcode must be 13 digits.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    cleanedBarcodes.add(code);
                }

                // Validate barcode count. 5 max barcodes.
                if (cleanedBarcodes.size() > 5) {
                    Toast.makeText(this, "You can only enter up to 5 barcodes.", Toast.LENGTH_SHORT).show();
                    return;
                }

                product.setBarcode(cleanedBarcodes);
                //// 'updateProduct' contains a method (found at 'ProductViewModel' in 'viewmodel' directory).
                productViewModel.updateProduct(product);                // Update in firebase.
                Toast.makeText(this, "Product updated", Toast.LENGTH_SHORT).show();
                editButton.setImageResource(R.drawable.square_pen);     // the pen edit button.

            } else {
                // Resize the check icon.
                sizeInDp = 30;
                scale = getResources().getDisplayMetrics().density;
                sizeInPx = (int) (sizeInDp * scale + 0.5f);

                // Reset layout size.
                ViewGroup.LayoutParams params = editButton.getLayoutParams();
                editButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
                params.width = sizeInPx;
                params.height = sizeInPx;
                editButton.setLayoutParams(params);

                editButton.setImageResource(R.drawable.check_square_broken); // switch to save icon.
                category.requestFocus();                                     // Optional: focus on first field.
            }

            // Enable/disable all editable fields based on mode.
            name.setEnabled(isEditing);
            category.setEnabled(isEditing);
            quantity.setEnabled(isEditing);
            costPrice.setEnabled(isEditing);
            salePrice.setEnabled(isEditing);
            size.setEnabled(isEditing);
            weight.setEnabled(isEditing);
            description.setEnabled(isEditing);
            barcode_list.setEnabled(isEditing);

        });

        // Show the bottom sheet.
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }


    // ⚠️ 'validateField' : Validate fields to prevent empty input. (METHODS)
    private boolean validateField(EditText field, String errorMsg) {
        String text = field.getText().toString().trim();
        if (text.isEmpty()) {
            field.setError(errorMsg);
            field.requestFocus();
            return false;
        }
        return true;
    }


    // ⚙️ 'getOptionalValue' : Return N/A for optional empty fields to save in Firebase. (METHODS)
    private String getOptionalValue(EditText field) {
        String text = field.getText().toString().trim();
        return text.isEmpty() ? "N/A" : text;
    }


    // 📷 'callback' : This callback handles barcode scanning events. (METHODS)
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            String searchScannedBarcode = result.getText();
            Log.d("BarcodeScan", "Scanned Barcode: " + searchScannedBarcode);

            if (searchScannedBarcode != null && !searchScannedBarcode.isEmpty()) {
                scannedBarcode = searchScannedBarcode; // assign to global variable.

                searchBarcode.pause(); // Pause scanning to avoid duplicates.
                Log.d("BarcodeScan", "Calling ViewModel to search product...");

                // Query product by scanned barcode via ViewModel.
                //// 'searchProductByBarcode' contains a method (found at 'ProductViewModel' in 'viewmodel' directory).
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
                        Log.d("BarcodeScan", "Product not found");

                        // Offer to link the unrecognized barcode to an existing product.
                        new AlertDialog.Builder(Inventory.this)
                                .setTitle("Product not found")
                                .setMessage("This barcode is not linked to any product. \nWould you like to link it to an existing one?")
                                .setPositiveButton("Yes", (dialog, which) -> {

                                    // Show inventory list for user to choose a product to link the scanned barcode.
                                    productViewModel.getItems().observe(Inventory.this, itemList -> {
                                        adapter.updateData(itemList);               // Display full product list.
                                        recyclerView.setVisibility(View.VISIBLE);   // Make sure list is available.
                                        searchBarcode.setVisibility(View.GONE);
                                        searchTargetOverlay.setVisibility(View.GONE);
                                        touchBlock.setVisibility(View.GONE);
                                        isScannerActive = false;
                                        Toast.makeText(getApplicationContext(), "Select a product to link the barcode.", Toast.LENGTH_LONG).show();
                                    });
                                })
                                .setNegativeButton("No", (dialog, which) -> {
                                    searchBarcode.resume();                         // Resume scanner if user declines to link the barcode.
                                })
                                .show();
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
            // Optional: Handle result points if needed.
        }
    };

    // Resume Scanner (METHODS)
    @Override
    protected void onResume() {
        super.onResume();
        if (isScannerActive)
            searchBarcode.resume();
    }

    // Pause Scanner (METHODS)
    @Override
    protected void onPause() {
        super.onPause();
        searchBarcode.pause();
    }
}