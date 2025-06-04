package com.example.bentory_app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.bentory_app.R;
import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.repository.ProductRepository;
import com.example.bentory_app.viewmodel.ProductViewModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// ===============================
// AddProduct Activity
//
// Purpose:
// - Allows users to add new products to Firebase.
// - Includes barcode scanning functionality.
// ===============================
public class AddProduct extends BaseDrawerActivity {

    // UI Components
    private ImageButton itemSaveBtn, barcodeButton, backBtn;
    private EditText itemName, itemCategory, itemQuantity, itemCostPrice, itemSalePrice, itemSize, itemWeight, itemDescription, scannedCode;
    private DecoratedBarcodeView barcodeView;
    private View targetOverlay, touchBlock;

    // State
    private boolean hasScannedBarcode = false;
    private String name;
    private String category;
    private String size;
    private String weight;
    private String description;
    private String code;
    private int quantity;
    private double costPrice;
    private double salePrice;

    // ViewModel
    private ProductViewModel productViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);

        // ⬛ UI Setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupToolbar(R.id.my_toolbar, "Add Product", true); //// 'setupToolbar' contains a method (found at 'BaseDrawerActivity' in 'activities' directory).
        setupDrawer(); //// 'setupDrawer' contains a method (found at 'BaseDrawerActivity' in 'activities' directory).

        // ⬛ Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // ⬛ Bind Views
        itemName = findViewById(R.id.newItemName);
        itemCategory = findViewById(R.id.newItemCategory);
        itemQuantity = findViewById(R.id.newItemQuantity);
        itemCostPrice = findViewById(R.id.newItemCostPrice);
        itemSalePrice = findViewById(R.id.newItemSalePrice);
        itemSize = findViewById(R.id.newItemSize);
        itemWeight = findViewById(R.id.newItemWeight);
        itemDescription = findViewById(R.id.newItemDescription);
        itemSaveBtn = findViewById(R.id.newItemSaveBtn);
        scannedCode = findViewById(R.id.code);
        barcodeButton = findViewById(R.id.barcodeBtn);
        barcodeView = findViewById(R.id.addBarcodeScanner);
        targetOverlay = findViewById(R.id.targetOverlay);
        touchBlock = findViewById(R.id.touchBlocker);
        backBtn = findViewById(R.id.back_btn);

        // ===============================
        // 🔙 Back Button: Close Activity. (FEATURE)
        // ===============================
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barcodeView.getVisibility() == View.VISIBLE) {
                    // Hide scanner and show form
                    barcodeView.setVisibility(View.GONE);
                    targetOverlay.setVisibility(View.GONE);
                    touchBlock.setVisibility(View.GONE);
                    barcodeView.pause(); // stop scanning
                } else {
                    // Normal back behavior — e.g., finish activity
                    finish();
                }
            }
        });


        // ===============================
        // 📷 Barcode Scanner Setup: Initialize the barcode scanner and start listening for scans continuously. (FEATURE)
        // ===============================
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.decodeContinuous(callback); //// 'callback' contains a method (found at the bottom of the code).

        // When the barcode button is clicked, show the scanner view and resume scanning.
        barcodeButton.setOnClickListener(v -> {
            barcodeView.setVisibility(View.VISIBLE);    // show barcode camera view.
            targetOverlay.setVisibility(View.VISIBLE);  // show overlay (like scan target frame).
            touchBlock.setVisibility(View.VISIBLE);     // prevents users from interacting with the UI behind the scanner (like form fields or buttons).
            barcodeView.resume();                       // Resume scanning if it was paused.
            hasScannedBarcode = false;                            // Reset flag to allow new scan.
        });


        // ===============================
        // 💾 Save Button: Validates input & barcode then save new product. (FEATURE)
        // ===============================
        itemSaveBtn.setOnClickListener(v -> {
            //// 'isFieldEmpty' is a helper method (found at the bottom of the code).
            if (isFieldEmpty(itemName, "Name is required") ||
                    isFieldEmpty(itemCategory, "Category is required") ||
                    isFieldEmpty(itemQuantity, "Quantity is required") ||
                    isFieldEmpty(itemCostPrice, "Cost Price is required") ||
                    isFieldEmpty(itemSalePrice, "Sale Price is required") ||
                    isFieldEmpty(scannedCode, "Product Code is required")) {

                // Show a message if any required field is missing.
                Toast.makeText(AddProduct.this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                return; // Stop further processing if validation fails.
            }

            // Validate scanned code length: show error if it's less than 13 digits (EAN-13).
            if (scannedCode.length() < 13) {
                scannedCode.setError("Code must be 13 digits.");
                Toast.makeText(AddProduct.this, "Product code must 13 digits.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Extract and trim user inputs from the EditText fields; convert numeric fields to appropriate types.
            name = itemName.getText().toString().trim();
            category = itemCategory.getText().toString().trim();
            quantity = Integer.parseInt(itemQuantity.getText().toString().trim());
            costPrice = Double.parseDouble(itemCostPrice.getText().toString().trim());
            salePrice = Double.parseDouble(itemSalePrice.getText().toString().trim());
            size = itemSize.getText().toString().trim();
            weight = itemWeight.getText().toString().trim();
            description = itemDescription.getText().toString().trim();
            code = scannedCode.getText().toString().trim();

            // Build ProductModel
            ProductModel product = new ProductModel(); // creates a new productModel instance and populate its fields.
            List<String> barcode = new ArrayList<>();  // allowing for future support of multiple barcodes per product.
            barcode.add(code);                         // Add the current scanned barcode.
            product.setName(name);
            product.setCategory(category);
            product.setQuantity(quantity);
            product.setCost_Price(costPrice);
            product.setSale_Price(salePrice);
            product.setSize(size);
            product.setWeight(weight);
            product.setDescription(description);
            product.setBarcode(barcode);

            // Save to Firebase: Use the productViewModel to handle data submission to Firebase.
            productViewModel.addProduct(product); //// 'addProduct' contains a method (found at 'ProductViewModel' in 'viewmodel' directory).
            Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();

            // Clear the form fields for the next input.
            itemName.setText("");
            itemCategory.setText("");
            itemQuantity.setText("");
            itemCostPrice.setText("");
            itemSalePrice.setText("");
            itemSize.setText("");
            itemWeight.setText("");
            itemDescription.setText("");
            scannedCode.setText("");

            // Returns to Inventory Screen after Saving.
            Intent intent = new Intent(AddProduct.this, Inventory.class);
            startActivity(intent);
            finish(); // Close current activity to prevent returning via back buttons.
        });
    }


    // ===============================
    //             METHODS
    // ===============================

    // 📷 'callback' : This callback handles barcode scanning events. (METHODS)
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            // Only process the scan if one hasn't been handled yet.
            if (!hasScannedBarcode) {
                hasScannedBarcode = true; // prevent multiple scans from being processed.

                // Play a short beep sound to indicate a successful scan.
                ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 200);
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150);

                // Display the scanned barcode in the EditText.
                scannedCode.setText(result.getText());

                // Hide the scanner view and target overlay after successful scan.
                barcodeView.setVisibility(View.GONE);
                targetOverlay.setVisibility(View.GONE);
                touchBlock.setVisibility(View.GONE);

                // Pause the scanner to prevent further scanning (stops scanning).
                barcodeView.pause();
            }
        }

        @Override
        public void possibleResultPoints(List resultPoints) {
            // Optional: used to show possible scan points. Not needed for now.
        }
    };

    // Resume Scanner (METHODS)
    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null && barcodeView.getVisibility() == View.VISIBLE) {
            barcodeView.resume(); // resume scanning if the scanner is visible.
        }
    }

    // Pause Scanner (METHODS)
    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause(); // pause the scanner when activity is not in focus.
        }
    }


    // 💾 'isFieldEmpty' : Input validation. (METHODS)
    private boolean isFieldEmpty(EditText field, String errorMessage) {
        // Trimmed text from the field and checks if the field is empty.
        if (field.getText().toString().trim().isEmpty()) {
            field.setError(errorMessage); // Display error message directly on the field.
            return true;                  // Return true to indicate validation failed.
        }
        return false;                     // Return false if field is not empty (i.e validation passed).
    }
}