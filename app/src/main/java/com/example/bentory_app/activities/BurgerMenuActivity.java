package com.example.bentory_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bentory_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// ===============================
// BurgerMenu Activity
//
// Purpose:
// - Displays a static menu screen with navigation buttons and user greeting.
// - Allows user to navigate to key app features like Add Product, Inventory, etc...
// - Provides logout functionality for the authenticated user.
// - Does not include drawer; uses standalone layout.
// ===============================
public class BurgerMenuActivity extends BaseActivity {

    // UI Components
    ImageButton buttonLogout, imageButton;
    TextView textViewGreeting;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burger_menu);

        // Setup toolbar without burger menu.
        setupToolbar(R.id.my_toolbar, "Menu", false);

        // Initialize Firebase Auth.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Display greeting based on user email.
        textViewGreeting = findViewById(R.id.textViewGreeting);
        if (currentUser != null && currentUser.getEmail() != null) {
            String email = currentUser.getEmail();
            String username = email.split("@")[0];
            textViewGreeting.setText("Hi, " + username);
        } else {
            textViewGreeting.setText("Hi, User");
        }

        // Setup navigation buttons.
        setupImageButton(R.id.buttonAddProduct, AddProduct.class);
        setupImageButton(R.id.buttonInventory, Inventory.class);
        setupImageButton(R.id.buttonSellingWindow, SellProduct.class);
        setupImageButton(R.id.buttonStatistics, Statistics.class);

        // Logout button handler.
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(BurgerMenuActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BurgerMenuActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // 🔘 Navigation button click handler. (METHODS)
    private void setupImageButton(int buttonId, Class<?> activityClass) {
        imageButton = findViewById(buttonId);
        if (imageButton != null) {
            imageButton.setOnClickListener(v -> {
                Intent intent = new Intent(BurgerMenuActivity.this, activityClass);
                startActivity(intent);
            });
        }
    }
}