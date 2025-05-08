package com.example.bentory_app.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.bentory_app.R;
import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.viewmodel.ProductViewModel;

public class AddProduct extends AppCompatActivity {

    private EditText itemName, itemCategory, itemQuantity, itemCostPrice, itemSalePrice, itemSize, itemWeight, itemDescription;
    private ImageButton itemSaveBtn;
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

        // Initialize Model
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Initialize Views
        itemName = findViewById(R.id.newItemName);
        itemCategory = findViewById(R.id.newItemCategory);
        itemQuantity = findViewById(R.id.newItemQuantity);
        itemCostPrice = findViewById(R.id.newItemCostPrice);
        itemSalePrice = findViewById(R.id.newItemSalePrice);
        itemSize = findViewById(R.id.newItemSize);
        itemWeight = findViewById(R.id.newItemWeight);
        itemDescription = findViewById(R.id.newItemDescription);
        itemSaveBtn = findViewById(R.id.newItemSaveBtn);

        itemSaveBtn.setOnClickListener(v -> {
            // Step 1: Get user inputs from UI fields and clean them
            // WHY: Always sanitize user input to prevent accidental spaces and parsing errors.
            String name = itemName.getText().toString().trim();
            String category = itemCategory.getText().toString().trim();
            int quantity = Integer.parseInt(itemQuantity.getText().toString().trim());
            double costPrice = Double.parseDouble(itemCostPrice.getText().toString().trim());
            double salePrice = Double.parseDouble(itemSalePrice.getText().toString().trim());
            String size = itemSize.getText().toString().trim();
            String weight = itemWeight.getText().toString().trim();
            String description = itemDescription.getText().toString().trim();

            // Step 2: Construct a ProductModel object to hold the product data
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

            // Step 3: Call the ViewModel method to push this data to Firebase Realtime Database
            // WHY: The ViewModel acts as a bridge between UI and backend logic, keeping UI clean and testable.
            productViewModel.addProduct(product);

            // Step 4: Show a success message to confirm the action
            // WHY: Providing immediate feedback reassures the user that the operation was successful.
            Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();

            // Step 5: Clear all input fields after saving
            // WHY: Resets the form to allow the user to input a new item without manually clearing each field.
            itemName.setText("");
            itemCategory.setText("");
            itemQuantity.setText("");
            itemCostPrice.setText("");
            itemSalePrice.setText("");
            itemSize.setText("");
            itemWeight.setText("");
            itemDescription.setText("");
        });

    }

}