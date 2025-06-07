package com.example.bentory_app.activities;

import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.bentory_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// ===============================
// BaseDrawer Activity
//
// Purpose:
// - Extends BaseActivity to add shared drawer functionality across screens.
// - Sets up navigation drawer with user greeting, menu buttons, and logout.
// - Handles burger icon behavior for opening the drawer.
// ===============================
public abstract class BaseDrawerActivity extends BaseActivity {

    // UI Components
    ImageButton buttonLogout, imageButton, burgerIcon;
    TextView textViewGreeting;

    protected DrawerLayout drawerLayout;
    protected FirebaseAuth mAuth;

    // 🔧 Initializes the drawer: greeting, nav buttons, and logout.
    protected void setupDrawer() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        if (drawerLayout != null) {
            // Disable gesture (touch swipe)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            // Setup drawer greeting
            FirebaseUser currentUser = mAuth.getCurrentUser();
            textViewGreeting = findViewById(R.id.textViewGreeting);
            if (textViewGreeting != null) {
                if (currentUser != null && currentUser.getEmail() != null) {
                    String email = currentUser.getEmail();
                    String username = email.split("@")[0];
                    textViewGreeting.setText("Hi, " + username);
                } else {
                    textViewGreeting.setText("Hi, User");
                }
            }

            // Setup drawer menu buttons
            setupDrawerImageButton(R.id.buttonAddProduct, AddProduct.class);
            setupDrawerImageButton(R.id.buttonInventory, Inventory.class);
            setupDrawerImageButton(R.id.buttonSellingWindow, SellProduct.class);
            setupDrawerImageButton(R.id.buttonStatistics, Statistics.class);
            setupDrawerImageButton(R.id.buttonScanProduct, SellProduct.class);

            // Set up logout button in drawer
            buttonLogout = findViewById(R.id.buttonLogout);
            if (buttonLogout != null) {
                buttonLogout.setOnClickListener(v -> {
                    mAuth.signOut();
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            // Setup burger menu click after drawer is ready
            setupBurgerMenuClick();
        }
    }

    // 🔘 method for drawer menu buttons. (METHODS)
    private void setupDrawerImageButton(int buttonId, Class<?> activityClass) {
        imageButton = findViewById(buttonId);
        if (imageButton != null) {
            imageButton.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.closeDrawers(); // Close drawer when button is clicked
                }
                startActivity(new Intent(this, activityClass));
            });
        }
    }

    // 🧰 Override setupToolbar to handle drawer opening.
    @Override
    protected void setupToolbar(int toolbarId, String title, boolean showBurgerMenu) {
        super.setupToolbar(toolbarId, title, showBurgerMenu);

        // Setup burger menu click listener after drawer is initialized
        if (showBurgerMenu) {
            setupBurgerMenuClick();
        }
    }

    // 🍔 Burger menu icon opens the drawer. (METHODS)
    private void setupBurgerMenuClick() {
        // Use post to ensure views are fully inflated
        findViewById(android.R.id.content).post(() -> {
            burgerIcon = findViewById(R.id.burgerbttn);
            if (burgerIcon != null && drawerLayout != null) {
                burgerIcon.setOnClickListener(v -> {
                    drawerLayout.openDrawer(findViewById(R.id.nav_drawer));
                });
            }
        });
    }
}