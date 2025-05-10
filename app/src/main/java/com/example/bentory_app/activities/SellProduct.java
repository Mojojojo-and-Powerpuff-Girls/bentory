package com.example.bentory_app.activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.subcomponents.InventoryAdapter;
import com.example.bentory_app.subcomponents.SellingProductAdapter;
import com.example.bentory_app.viewmodel.ProductViewModel;
import com.example.bentory_app.viewmodel.SellingViewModel;

import java.util.ArrayList;
import java.util.List;

public class SellProduct extends AppCompatActivity {

    private RecyclerView recyclerViewSelling;
    private SellingViewModel sellingViewModel;
    private SellingProductAdapter sellingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sell_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the RecyclerView from the layout using its ID
        recyclerViewSelling = findViewById(R.id.recyclerViewSelling);
        // Set a vertical list layout for the RecyclerView
        recyclerViewSelling.setLayoutManager(new LinearLayoutManager(this));

        // Create an instance of the adapter that will handle displaying the product items
        sellingAdapter = new SellingProductAdapter();
        // Set the adapter to the RecyclerView
        recyclerViewSelling.setAdapter(sellingAdapter);

        // Initialize the ViewModel which will provide the product data
        sellingViewModel = new ViewModelProvider(this).get(SellingViewModel.class);
        // Observe the product list from the ViewModel
        // Whenever the data changes, this will be called
        sellingViewModel.getItems().observe(this, productModels -> {
            // Pass the product list to the adapter so it can display the items in the RecyclerView
            sellingAdapter.setProductList(productModels);
        });
    }
}