package com.example.bentory_app.activities;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
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

            ImageButton burgerIcon = toolbar.findViewById(R.id.burger_icon); // Assuming you'll add this ID
            if (burgerIcon != null) {
                if (showBurgerMenu) {
                    burgerIcon.setVisibility(View.VISIBLE);
                    // Remove the automatic click listener - let individual activities handle this
                    // burgerIcon.setOnClickListener(v -> {
                    // Intent intent = new Intent(BaseActivity.this, BurgerMenuActivity.class);
                    // startActivity(intent);
                    // });
                } else {
                    burgerIcon.setVisibility(View.GONE);
                }
            }
        }
    }

    // Overloaded method for activities that don't need to show the burger menu by
    // default
    protected void setupToolbar(int toolbarId, String title) {
        setupToolbar(toolbarId, title, false);
    }
}