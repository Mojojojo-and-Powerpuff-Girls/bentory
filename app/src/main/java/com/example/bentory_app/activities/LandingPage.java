package com.example.bentory_app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.StatsModel;
import com.example.bentory_app.subcomponents.MenuAdapter;

import java.util.ArrayList;
import java.util.List;

// ===============================
// LandingPage Activity
//
// Purpose:
// - Serves as the main dashboard of the app after login.
// - Provides quick access to key features: Add Product, Inventory, etc...
// - Implements animated navigation buttons for enhanced UX.
// - Displays a summary of business metrics using a RecyclerView.
// - Inherits drawer and toolbar setup from BaseDrawer Activity.
// ===============================
public class LandingPage extends BaseDrawerActivity {

    // UI Components
    private ImageButton addProductBtn, sellProductBtn, inventoryBtn, statsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing_page);

        // ⬛ UI Setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.my_toolbar), (v, insets) -> {
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        setupToolbar(R.id.my_toolbar, "Landing Page", true);//// 'setupToolbar' contains a method (found at 'BaseDrawerActivity' in 'activities' directory).
        setupDrawer(); //// 'setupDrawer' contains a method (found at 'BaseDrawerActivity' in 'activities' directory).

        // ⬛ Bind Views
        addProductBtn = findViewById(R.id.addProductBtn);
        sellProductBtn = findViewById(R.id.sellProductBtn);
        inventoryBtn = findViewById(R.id.inventoryBtn);
        statsBtn = findViewById(R.id.statsBtn);

        // animate buttons
        setButtonClickListener(addProductBtn, AddProduct.class);
        setButtonClickListener(sellProductBtn, SellProduct.class);
        setButtonClickListener(inventoryBtn, Inventory.class);
        setButtonClickListener(statsBtn, Statistics.class);

        // SETUP RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        List<StatsModel> itemList = new ArrayList<>();
        itemList.add(new StatsModel("Total Expenses", "Month of April", "PHP 10,456.00"));
        itemList.add(new StatsModel("Net Profit", "Month of April", "PHP 45,000.90"));
        itemList.add(new StatsModel("Projected Sales", "Month of May", "PHP 70,000.00"));

        MenuAdapter adapter = new MenuAdapter(itemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }


    // ===============================
    //             METHODS
    // ===============================

    // 🧩 'setButtonClickListener' : animates a button on click, then opens the target activity.
    private void setButtonClickListener(ImageButton button, Class<?> targetActivity) {
        button.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100);
                        startActivity(new Intent(LandingPage.this, targetActivity));
                        overridePendingTransition(R.anim.fade_in_fast, R.anim.fade_out_fast);
                    });
        });
    }
}
