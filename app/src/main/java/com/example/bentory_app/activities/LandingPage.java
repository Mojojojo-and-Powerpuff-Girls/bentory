package com.example.bentory_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bentory_app.R;
import com.example.bentory_app.model.StatsModel;
import com.example.bentory_app.subcomponents.MenuAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class LandingPage extends AppCompatActivity {

    private ImageButton addProductBtn, sellProductBtn, inventoryBtn, statsBtn;
    private ImageButton logoutBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing_page);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // setup toolbar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Set the title using the custom TextView in the toolbar
        TextView toolbarTitle = myToolbar.findViewById(R.id.textView);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Landing Page");
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayHomeAsUpEnabled(false);

            actionBar.setDisplayShowTitleEnabled(false);
        }

        // window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons
        addProductBtn = findViewById(R.id.addProductBtn);
        sellProductBtn = findViewById(R.id.sellProductBtn);
        inventoryBtn = findViewById(R.id.inventoryBtn);
        statsBtn = findViewById(R.id.statsBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        // animate buttons
        setButtonClickListener(addProductBtn, AddProduct.class);
        setButtonClickListener(sellProductBtn, SellProduct.class);
        setButtonClickListener(inventoryBtn, Inventory.class);
        setButtonClickListener(statsBtn, Statistics.class);

        // Set up logout button
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> {
                mAuth.signOut();
                Toast.makeText(LandingPage.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LandingPage.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

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

    // animation method
    private void setButtonClickListener(ImageButton button, Class<?> targetActivity) {
        button.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100);
                        startActivity(new Intent(LandingPage.this, targetActivity));
                    });
        });
    }

}
