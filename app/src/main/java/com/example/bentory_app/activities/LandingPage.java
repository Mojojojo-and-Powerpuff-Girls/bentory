package com.example.bentory_app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.StatsModel;
import com.example.bentory_app.subcomponents.MenuAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        // ⬛ Camera Permission
        checkCameraPermission();

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
        RecyclerView recyclerViewTop = findViewById(R.id.recyclerView);
        List<StatsModel> statsList = new ArrayList<>();
        MenuAdapter adapter1 = new MenuAdapter(statsList);
        recyclerViewTop.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTop.setAdapter(adapter1);

        DatabaseReference statsRef = FirebaseDatabase.getInstance().getReference("selling_stats");
        DatabaseReference monthRef = statsRef.child("monthly_stats").child("month6");
        DatabaseReference weekRef = statsRef.child("weekly_sale").child("week24");

        monthRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalSales = snapshot.child("total_sales").getValue(Double.class) != null ? snapshot.child("total_sales").getValue(Double.class) : 0.0;
                double profit = snapshot.child("monthly_profit").getValue(Double.class) != null ? snapshot.child("monthly_profit").getValue(Double.class) : 0.0;
                int productsSold = snapshot.child("products_sold").getValue(Integer.class) != null ? snapshot.child("products_sold").getValue(Integer.class) : 0;

                statsList.add(new StatsModel("Total Sales", "Month of June", "PHP " + totalSales));
                statsList.add(new StatsModel("Monthly Profit", "Month of June", "PHP " + profit));
                statsList.add(new StatsModel("Products Sold", "Month of June", String.valueOf(productsSold)));

                adapter1.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        weekRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double weeklySale = snapshot.child("total_sale").getValue(Double.class) != null ? snapshot.child("total_sale").getValue(Double.class) : 0.0;
                statsList.add(0, new StatsModel("Weekly Sales", "Week 24", "PHP " + weeklySale));
                adapter1.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });




        recyclerViewTop.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTop.setAdapter(adapter1);



    }


    // ===============================
    //             METHODS
    // ===============================

    // 📷 'checkCameraPermission' : Method to check if the app has permission to use the camera. (METHODS)
    private static final int CAMERA_REQUEST_CODE = 101; // Unique request code for camera permission.
    private void checkCameraPermission() {
        // Check if the camera permission has NOT been granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Request the camera permission from the user at runtime.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }


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
