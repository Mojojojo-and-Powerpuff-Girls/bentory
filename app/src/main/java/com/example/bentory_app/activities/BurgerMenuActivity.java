package com.example.bentory_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bentory_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BurgerMenuActivity extends BaseActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burger_menu);

        setupToolbar(R.id.my_toolbar, "Menu", false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        TextView textViewGreeting = findViewById(R.id.textViewGreeting);
        if (currentUser != null && currentUser.getEmail() != null) {
            String email = currentUser.getEmail();
            String username = email.split("@")[0];
            textViewGreeting.setText("Hi, " + username);
        } else {
            textViewGreeting.setText("Hi, User");
        }

        setupImageButton(R.id.buttonAddProduct, AddProduct.class);
        setupImageButton(R.id.buttonInventory, Inventory.class);
        setupImageButton(R.id.buttonSellingWindow, SellProduct.class);
        setupImageButton(R.id.buttonStatistics, Statistics.class);
        setupImageButton(R.id.buttonScanProduct, SellProduct.class);

        ImageButton buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(BurgerMenuActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BurgerMenuActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupImageButton(int buttonId, Class<?> activityClass) {
        ImageButton imageButton = findViewById(buttonId);
        if (imageButton != null) {
            imageButton.setOnClickListener(v -> {
                Intent intent = new Intent(BurgerMenuActivity.this, activityClass);
                startActivity(intent);
            });
        }
    }
}