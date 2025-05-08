package com.example.bentory_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.bentory_app.R;
import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.viewmodel.ProductViewModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Date;

public class AddProduct extends AppCompatActivity {

    private EditText itemName, itemCategory, itemQuantity, itemCostPrice, itemSalePrice, itemSize, itemWeight, itemDescription, scannedCode;
    private ImageButton itemSaveBtn, barcodeButton;
    private ProductViewModel productViewModel;

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

        // 1. Initialize Model
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // 2. Initialize Views
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

        // 1. Register a callback to handle the result of the barcode scan.
        // If a result is found and it's not null, set the scanned text to the EditText.
        ActivityResultCallback<ScanIntentResult> scanCallBack = new ActivityResultCallback<ScanIntentResult>() {
            @Override
            public void onActivityResult(ScanIntentResult result) {
                if (result != null && result.getContents() != null) {
                    scannedCode.setText(result.getContents());
                }
                else {
                    Toast.makeText(AddProduct.this, "Scan cancelled or failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // 2. Register an ActivityResultLauncher to launch the ZXing scanner and handle its result.
        // This ensures barcode scanning is handled within Android’s recommended lifecycle-aware API.
        ActivityResultLauncher<ScanOptions> barcodeScanner = registerForActivityResult(new ScanContract(), scanCallBack);

        // 3. Set up the click listener for the scan button.
        // When clicked, launch the barcode scanner with desired settings:
        barcodeButton.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan a barcode"); // A prompt to guide the user
            options.setBeepEnabled(true); // Beep sound enabled for scan confirmation
            options.setOrientationLocked(true); // Locking orientation ensures a consistent scanning experience, especially on devices with sensors that auto-rotate.
            options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES); // Accepts all barcode types (1D & 2D)

            barcodeScanner.launch(options);
        });


        itemSaveBtn.setOnClickListener(v -> {
            // Step 1: Validate required fields using a helper method that checks emptiness and sets error if empty.
            // WHY: This ensures key data is not missing before proceeding to data processing.
            if (isFieldEmpty(itemName, "Name is required") ||
                isFieldEmpty(itemCategory, "Category is required") ||
                isFieldEmpty(itemQuantity, "Quantity is required") ||
                isFieldEmpty(itemCostPrice, "Cost Price is required") ||
                isFieldEmpty(itemSalePrice, "Sale Price is required") ||
                isFieldEmpty(scannedCode, "Product Code is required")) {

                Toast.makeText(AddProduct.this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Step 2: Get user inputs from UI fields and clean them
            // WHY: Always sanitize user input to prevent accidental spaces and parsing errors.
            String name = itemName.getText().toString().trim();
            String category = itemCategory.getText().toString().trim();
            int quantity = Integer.parseInt(itemQuantity.getText().toString().trim());
            double costPrice = Double.parseDouble(itemCostPrice.getText().toString().trim());
            double salePrice = Double.parseDouble(itemSalePrice.getText().toString().trim());
            String size = itemSize.getText().toString().trim();
            String weight = itemWeight.getText().toString().trim();
            String description = itemDescription.getText().toString().trim();
            String code = scannedCode.getText().toString().trim();

            // Step 3: Construct a ProductModel object to hold the product data
            // WHY: This object will be used to transfer structured data to Firebase.
            ProductModel product = new ProductModel();
            product.setName(name);
            product.setCategory(category);
            product.setQuantity(quantity);
            product.setCost_Price(costPrice);
            product.setSale_Price(salePrice);
            product.setSize(size);
            product.setWeight(weight);
            product.setDescription(description);
            product.setBarcode(code);

            // Step 4: Call the ViewModel method to push this data to Firebase Realtime Database
            // WHY: The ViewModel acts as a bridge between UI and backend logic, keeping UI clean and testable.
            productViewModel.addProduct(product);

            // Step 5: Show a success message to confirm the action
            // WHY: Providing immediate feedback reassures the user that the operation was successful.
            Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();

            // Step 6: Clear all input fields after saving
            // WHY: Resets the form to allow the user to input a new item without manually clearing each field.
            itemName.setText("");
            itemCategory.setText("");
            itemQuantity.setText("");
            itemCostPrice.setText("");
            itemSalePrice.setText("");
            itemSize.setText("");
            itemWeight.setText("");
            itemDescription.setText("");
            scannedCode.setText("");

            Intent intent = new Intent(AddProduct.this, Inventory.class);
            startActivity(intent);
            finish();
        });

    }

    // Helper method
    private boolean isFieldEmpty (EditText field, String errorMessage) {
        if (field.getText().toString().trim().isEmpty()) {
            field.setError(errorMessage);
            return true;
        }
        return false;
    }

}