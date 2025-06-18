package com.example.bentory_app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bentory_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Transparent status bar
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.bentory_splash);

        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                // User is signed in, check onboarding status
                FirebaseDatabase.getInstance().getReference("users")
                        .child(currentUser.getUid())
                        .child("onboardingCompleted")
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            Boolean completed = snapshot.getValue(Boolean.class);
                            if (completed != null && completed) {
                                startActivity(new Intent(this, LandingPage.class));
                            } else {
                                startActivity(new Intent(this, Onboarding.class));
                            }
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Fallback if error happens
                            startActivity(new Intent(this, LandingPage.class));
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        });
            } else {
                // Not logged in
                startActivity(new Intent(this, LoginActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }, 2000);
    }
}
