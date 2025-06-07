package com.example.bentory_app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bentory_app.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make status bar transparent and draw layout behind it.
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.bentory_splash);

        // Delay the splash screen for 3 seconds before navigating to the login screen.
        new Handler().postDelayed(() -> {
            // Create an intent to move form SplashAct to LoginAct.
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);

            // Apply fade in/out transition between SplashAct and LoginAct.
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 2000); // delay in milliseconds (e.g, 2000m = 2 seconds)
    }
}

