package com.example.bentory_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bentory_app.R;
import com.example.bentory_app.model.OnboardingPage;
import com.example.bentory_app.subcomponents.OnboardingAdapter;

import java.util.Arrays;
import java.util.List;

// ===============================
// Onboarding Activity
//
// Purpose:
// - Guides new users through a multi-step onboarding process using ViewPager2.
// - Allows navigation with Back and Next/Done buttons.
// - Stores onboarding completion status using SharedPreferences.
// ===============================
public class Onboarding extends AppCompatActivity {

    // UI Components
    private ImageButton btnNext, btnBack;
    private ViewPager2 viewPager;

    // State
    private boolean isManualLaunch;
    private List<OnboardingPage> pages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        // ⬛ UI Setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // ⬛ Bind Views
        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        // If not manually launched and onboarding already completed, skip it.
        isManualLaunch = getIntent().getBooleanExtra("manualLaunch", false);

        // Onboarding pages with image resources.
        pages = Arrays.asList(
                new OnboardingPage(R.drawable.img1),
                new OnboardingPage(R.drawable.img2),
                new OnboardingPage(R.drawable.img3));
        viewPager.setAdapter(new OnboardingAdapter(pages));

        // ===============================
        // 📄 Page Change: Handle page change events [indicator, button states].
        // (FEATURE)
        // ===============================
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Show/hide back button on first page
                btnBack.setVisibility(position == 0 ? ViewPager.INVISIBLE : ViewPager.VISIBLE);

                // Cast layout params properly for ConstraintLayout
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) btnNext.getLayoutParams();

                // On last page, Change Next to Done on last page
                if (position == pages.size() - 1) {
                    btnNext.setImageResource(R.drawable.done_button);

                    // Make it larger
                    params.width = dpToPx(70); // your custom size
                    params.height = dpToPx(70);
                    params.bottomMargin = dpToPx(40);
                    params.rightMargin = dpToPx(10);

                } else {
                    btnNext.setImageResource(R.drawable.btn_next);
                    params.width = dpToPx(42); // original "Next" size
                    params.height = dpToPx(42);
                    params.bottomMargin = dpToPx(55); // default margin
                    params.rightMargin = dpToPx(0);
                }
            }
        });

        // ===============================
        // ➡️ Next Button: Handle next button click. (FEATURE)
        // ===============================
        btnNext.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos < pages.size() - 1) {
                viewPager.setCurrentItem(pos + 1);
            } else {
                if (!isManualLaunch) {
                    goToMain();
                } else {
                    finish(); // just close if launched manually.
                }
            }
        });

        // ===============================
        // ⬅️ Back Button: Handle back button click. (FEATURE)
        // ===============================
        btnBack.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current > 0) {
                viewPager.setCurrentItem(current - 1);
            }
        });

    }

    // ===============================
    // METHODS
    // ===============================

    // 🚀 'goToMain' : Go to main screen after onboarding. (METHODS)
    private void goToMain() {
        // Mark onboarding as completed in Firebase Database
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance()
                .getCurrentUser();
        if (user != null) {
            com.google.firebase.database.DatabaseReference userRef = com.google.firebase.database.FirebaseDatabase
                    .getInstance().getReference("users").child(user.getUid());
            userRef.child("onboardingCompleted").setValue(true);
        }
        startActivity(new Intent(this, LandingPage.class));
        finish();
    }

    // 📏 'dpToPx' : Utility method to convert dp to pixels. (METHODS)
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, btnNext.getResources().getDisplayMetrics());
    }
}
