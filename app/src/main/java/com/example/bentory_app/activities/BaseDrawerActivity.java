package com.example.bentory_app.activities;

import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.bentory_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class BaseDrawerActivity extends BaseActivity {

    protected DrawerLayout drawerLayout;
    protected FirebaseAuth mAuth;

    protected void setupDrawer() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        if (drawerLayout != null) {
            // Setup drawer greeting
            FirebaseUser currentUser = mAuth.getCurrentUser();
            TextView textViewGreeting = findViewById(R.id.textViewGreeting);
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
            ImageButton buttonLogout = findViewById(R.id.buttonLogout);
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

    // method for drawer menu buttons
    private void setupDrawerImageButton(int buttonId, Class<?> activityClass) {
        ImageButton imageButton = findViewById(buttonId);
        if (imageButton != null) {
            imageButton.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.closeDrawers(); // Close drawer when button is clicked
                }
                startActivity(new Intent(this, activityClass));
            });
        }
    }

    // Override setupToolbar to handle drawer opening
    @Override
    protected void setupToolbar(int toolbarId, String title, boolean showBurgerMenu) {
        super.setupToolbar(toolbarId, title, showBurgerMenu);

        // Setup burger menu click listener after drawer is initialized
        if (showBurgerMenu) {
            setupBurgerMenuClick();
        }
    }

    private void setupBurgerMenuClick() {
        // Use post to ensure views are fully inflated
        findViewById(android.R.id.content).post(() -> {
            ImageButton burgerIcon = findViewById(R.id.burger_icon);
            if (burgerIcon != null && drawerLayout != null) {
                burgerIcon.setOnClickListener(v -> {
                    drawerLayout.openDrawer(findViewById(R.id.nav_drawer));
                });
            }
        });
    }
}