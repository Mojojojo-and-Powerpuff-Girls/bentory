package com.example.bentory_app.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.bentory_app.model.CartModel;
import com.example.bentory_app.model.ProductModel;
import com.example.bentory_app.model.StatsModel;
import com.example.bentory_app.subcomponents.CartAdapter;
import com.example.bentory_app.subcomponents.InventoryAdapter;
import com.example.bentory_app.subcomponents.MenuAdapter;
import com.example.bentory_app.subcomponents.SellingProductAdapter;
import com.example.bentory_app.viewmodel.ProductViewModel;
import com.example.bentory_app.viewmodel.SellingViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
        ImageButton cartButton = findViewById(R.id.pullout_btn);

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



        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SellProduct.this);
                View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_cart, null);
                bottomSheetDialog.setContentView(bottomSheetView);

                RecyclerView recyclerViewTop = bottomSheetView.findViewById(R.id.recyclerViewCart);
                List<CartModel> itemList1 = new ArrayList<>();
                itemList1.add(new CartModel("Coca Cola", "Mismo", 4, "1000"));
                itemList1.add(new CartModel("Coca Cola", "Mismo", 4, "1000"));
                itemList1.add(new CartModel("Coca Cola", "Mismo", 4, "1000"));
                itemList1.add(new CartModel("Coca Cola", "Mismo", 4, "1000"));

                CartAdapter adapter1 = new CartAdapter(itemList1);
                recyclerViewTop.setLayoutManager(new LinearLayoutManager(SellProduct.this));
                recyclerViewTop.setAdapter(adapter1);

                bottomSheetDialog.show();
            }
        });





    }
}