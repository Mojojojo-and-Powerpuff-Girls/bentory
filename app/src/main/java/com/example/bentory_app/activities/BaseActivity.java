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

public class BaseActivity extends AppCompatActivity {

    protected void setupToolbar(int toolbarId, String title, boolean showBurgerMenu) {
        Toolbar toolbar = findViewById(toolbarId);
        if (toolbar == null) {
            // Fallback to a default toolbar ID if the provided one isn't found
            toolbar = findViewById(R.id.my_toolbar);
        }

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false); // Hide default title
            }

            TextView toolbarTitleTextView = toolbar.findViewById(R.id.toolbar_title_textview); // Assuming you'll add
                                                                                               // this ID
            if (toolbarTitleTextView != null) {
                toolbarTitleTextView.setText(title);
            }

            ImageButton burgerIcon = toolbar.findViewById(R.id.burgerbttn); // Changed from burger_icon to burgerbttn
            if (burgerIcon != null) {
                if (showBurgerMenu) {
                    burgerIcon.setVisibility(View.VISIBLE);

                } else {
                    burgerIcon.setVisibility(View.GONE);
                }
            }

            // Setup bell icon click listener for all activities
            setupBellIconClick();
        }
    }

    protected void setupToolbar(int toolbarId, String title) {
        setupToolbar(toolbarId, title, false);
    }

    protected void setupBellIconClick() {
        // Use post to ensure views are fully inflated
        findViewById(android.R.id.content).post(() -> {
            ImageButton bellIcon = findViewById(R.id.imageButton);
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