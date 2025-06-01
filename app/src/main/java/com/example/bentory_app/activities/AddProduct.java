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

public class AddProduct extends BaseActivity {

    // ViewModels
    private ProductViewModel productViewModel;

    // XML UI
    private EditText itemName, itemCategory, itemQuantity, itemCostPrice, itemSalePrice, itemSize, itemWeight, itemDescription, scannedCode;
    private ImageButton itemSaveBtn, barcodeButton, backBtn;
    private DecoratedBarcodeView barcodeView;
    private View targetOverlay, touchBlock;

    // Data Types
    private boolean scanned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Model/List = set and prepare for use.
        // Setup toolbar using BaseActivity method
        setupToolbar(R.id.my_toolbar, "Add Product");

        // Initialize Model/List = set and prepare for use.
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Initialize Views = connect using findViewById.
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


        // STEP 1 (BARCODE SCANNER AND BUTTON):
        // Initialize the barcode scanner and start listening for scans continuously.
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.decodeContinuous(callback); //// 'callback' contains a method (found at the bottom of the code).

        // STEP 2 (BARCODE SCANNER AND BUTTON):
        // When the barcode button is clicked, show the scanner view and resume scanning.
        // This resets the 'scanned' flag so it can scan a new item.
        barcodeButton.setOnClickListener(v -> {
            barcodeView.setVisibility(View.VISIBLE);    // Show barcode camera view.
            targetOverlay.setVisibility(View.VISIBLE);  // Show overlay (like scan target frame).
            touchBlock.setVisibility(View.VISIBLE);
            barcodeView.resume();                       // Resume scanning if it was paused.
            scanned = false;                            // Reset flag to allow new scan.
        });

        // NOTE (BARCODE SCANNER AND BUTTON):
        //// To see the logic of the barcode after this, proceed to 'callback' (found at the bottom of the code).


        // STEP 1 (ADDING PRODUCTS TO FIREBASE):
        itemSaveBtn.setOnClickListener(v -> {
            // Validate required fields using a helper method that checks emptiness and sets error if empty.
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

            // Extract and validate barcode length
            if (scannedCode.length() < 13) {
                scannedCode.setError("Code must be at least 13 digits.");
                Toast.makeText(AddProduct.this, "Product code must be at least 13 digits.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Extract and sanitize user inputs from the EditText fields.
            String name = itemName.getText().toString().trim();
            String category = itemCategory.getText().toString().trim();
            int quantity = Integer.parseInt(itemQuantity.getText().toString().trim());
            double costPrice = Double.parseDouble(itemCostPrice.getText().toString().trim());
            double salePrice = Double.parseDouble(itemSalePrice.getText().toString().trim());
            String size = itemSize.getText().toString().trim();
            String weight = itemWeight.getText().toString().trim();
            String description = itemDescription.getText().toString().trim();
            String code = scannedCode.getText().toString().trim();

            ProductModel product = new ProductModel(); // creates a new productModel instance and populate its fields.
            List<String> barcode = new ArrayList<>(); // allowing for future support of multiple barcodes per product.
            // Setting of value for products and barcode.
            barcode.add(code); // Add the current scanned barcode. !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            product.setName(name);
            product.setCategory(category);
            product.setQuantity(quantity);
            product.setCost_Price(costPrice);
            product.setSale_Price(salePrice);
            product.setSize(size);
            product.setWeight(weight);
            product.setDescription(description);
            product.setBarcode(barcode); // Set barcodes list.

            // Use the productViewModel to handle data submission to Firebase.
            productViewModel.addProduct(product); //// 'addProduct' contains a method (found at 'ProductViewModel' in 'viewmodel' directory).

            // Notify the user with a confirmation message.
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

            // Navigates back to the 'Inventory' screen after saving.
            Intent intent = new Intent(AddProduct.this, Inventory.class);
            startActivity(intent);
            finish(); // Close current activity to prevent returning via back buttons.
        });
        // NOTE (ADDING PRODUCTS TO FIREBASE):
        //// This is the last part of code for the logic of adding products to firebase in the 'activities' directory.
        //// This fulfills the role of adding products to firebase when save button is clicked.
        //// These are the methods used for STEP 1: ADDING PRODUCTS TO FIREBASE
        //// 'isFieldEmpty' is a helper method (found at the bottom of the code).
        //// 'addProduct' contains a method (found at 'ProductViewModel' in 'viewmodel' directory).
    }



    //// !!! METHODS OUTSIDE onCreate !!! ////

    // (BARCODE SCANNER AND BUTTON):
    // This callback handles barcode scanning events.
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            // Only process the scan if one hasn't been handled yet.
            if (!scanned) {
                scanned = true; // prevent multiple scans from being processed.

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

    // These methods manage scanner behavior when the app gains or loses focus.
    // Ensures the scanner resumes only when it's visible and the app is active.
    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null && barcodeView.getVisibility() == View.VISIBLE) {
            barcodeView.resume(); // resume scanning if the scanner is visible.
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause(); // pause the scanner when activity is not in focus.
        }
    }
    // NOTE (BARCODE SCANNER AND BUTTON):
    //// This is the last part of code for the logic of barcode scanner and button in the 'activities' directory.
    //// This just fulfills the role that the barcode button should open a scanner when pressed.
    //// The barcode scanner should be able to scan code.
    //// If you want to know the logic structure of adding all the products as well as the scanned code to the firebase.
    //// Proceed to 'STEP 1: ADDING PRODUCTS TO FIREBASE' of AddProduct (found at 'activities' directory).



    // (ADDING PRODUCTS TO FIREBASE):
    // Helper method to check if an EditText field is empty.
    private boolean isFieldEmpty (EditText field, String errorMessage) {
        // Trimmed text from the field and checks if the field is empty.
        if (field.getText().toString().trim().isEmpty()) {
            field.setError(errorMessage); // Display error message directly on the field.
            return true; // Return true to indicate validation failed.
        }
        return false; // Return false if field is not empty (i.e validation passed).
    }

}