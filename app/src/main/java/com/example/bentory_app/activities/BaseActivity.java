package com.example.bentory_app.activities;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.bentory_app.R;

// ===============================
// BaseActivity Activity
//
// Purpose:
// - Serves as a reusable base class for activities with shared toolbar behavior.
// - Provides setup methods for a custom toolbar with title and optional burger menu.
// - Adds notification bell click support across activities.
// ===============================
public class BaseActivity extends AppCompatActivity {

    // UI Components
    ImageButton burgerIcon, bellIcon;
    TextView toolbarTitleTextView;
    Toolbar toolbar;
    ActionBar actionBar;

    // ===============================
    // 🧱 Toolbar: Reusable method to setup toolbar with optional burger menu (☰). (METHODS)
    // ===============================
    protected void setupToolbar(int toolbarId, String title, boolean showBurgerMenu) {

        // Try to find the toolbar by the given ID.
        toolbar = findViewById(toolbarId);

        // If not found, fall back to a default toolbar ID.
        if (toolbar == null) {
            toolbar = findViewById(R.id.my_toolbar);
        }

        // If toolbar exists, configure it.
        if (toolbar != null) {
            setSupportActionBar(toolbar); // Set as the app's top bar;

            // Hide default app title to use a custom one instead.
            actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }

            // Set custom title in the toolbar (e.g., "Inventory", "Add Product")
            toolbarTitleTextView = toolbar.findViewById(R.id.toolbar_title_textview); // Assuming you'll add this ID.
            if (toolbarTitleTextView != null) {
                toolbarTitleTextView.setText(title);
            }

            // Handle showing/hiding the burger menu icon.
            burgerIcon = toolbar.findViewById(R.id.burgerbttn); // Changed from burger_icon to burgerbttn.
            if (burgerIcon != null) {
                if (showBurgerMenu) {
                    burgerIcon.setVisibility(View.VISIBLE); // Show it

                } else {
                    burgerIcon.setVisibility(View.GONE);    // Hide it
                }
            }

            // Setup bell icon click listener for all activities.
            setupBellIconClick();
        }
    }


    // ===============================
    // ✨ Overloaded method: call this if you don't need the burger icon. (METHODS)
    // ===============================
    protected void setupToolbar(int toolbarId, String title) {
        setupToolbar(toolbarId, title, false); // Just pass false for burger.
    }


    // ===============================
    // 🔔 Notification bell: setup the click behavior for the notification bell icon. (METHODS)
    // ===============================
    protected void setupBellIconClick() {
        // Use post to ensure views are fully inflated
        findViewById(android.R.id.content).post(() -> {
            bellIcon = findViewById(R.id.imageButton);
            if (bellIcon != null) {
                bellIcon.setOnClickListener(v -> {
                    // Navigate to Notifications activity
                    Intent intent = new Intent(this, Notifications.class);
                    startActivity(intent);
                });
            }
        });
    }
}