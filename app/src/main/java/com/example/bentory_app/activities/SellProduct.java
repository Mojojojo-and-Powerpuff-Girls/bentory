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
import com.example.bentory_app.viewmodel.CartViewModel;
import com.example.bentory_app.viewmodel.ProductViewModel;
import com.example.bentory_app.viewmodel.SellingViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class SellProduct extends AppCompatActivity {

    private RecyclerView recyclerViewSelling;
    private SellingViewModel sellingViewModel;
    private CartViewModel cartViewModel;
    private SellingProductAdapter sellingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sell_product);

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize ViewModels
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        sellingViewModel = new ViewModelProvider(this).get(SellingViewModel.class);

        // Setup RecyclerView for selling products
        recyclerViewSelling = findViewById(R.id.recyclerViewSelling);
        recyclerViewSelling.setLayoutManager(new LinearLayoutManager(this));

        // Setup adapter and handle Add to Cart logic
        sellingAdapter = new SellingProductAdapter(product -> {
            CartModel cartItem = new CartModel(
                    product.getId(),
                    product.getName(),
                    product.getSize(),
                    1,
                    product.getSale_Price()
            );
            cartViewModel.addToCart(cartItem);
        });

        recyclerViewSelling.setAdapter(sellingAdapter);

        // Observe product data from SellingViewModel
        sellingViewModel.getItems().observe(this, productModels -> {
            sellingAdapter.setProductList(productModels);
        });

        // Set up cart button click listener to show BottomSheet with cart items
        ImageButton cartButton = findViewById(R.id.pullout_btn);
        cartButton.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SellProduct.this);
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_cart, null);
            bottomSheetDialog.setContentView(bottomSheetView);

            RecyclerView recyclerViewTop = bottomSheetView.findViewById(R.id.recyclerViewCart);
            recyclerViewTop.setLayoutManager(new LinearLayoutManager(SellProduct.this));

            cartViewModel.getCartItems().observe(SellProduct.this, cartItems -> {
                CartAdapter adapter1 = new CartAdapter(cartItems);
                recyclerViewTop.setAdapter(adapter1);
            });

            bottomSheetDialog.show();
        });
    }
}