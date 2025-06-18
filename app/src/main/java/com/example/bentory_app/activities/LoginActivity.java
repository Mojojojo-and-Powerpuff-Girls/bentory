package com.example.bentory_app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bentory_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

// ===============================
// Login Activity
//
// Purpose:
// - Authenticates users using Firebase authentication.
// - Redirects to LandingPage upon successful login.
// - Provides links for signing up and resetting password.
// - Includes error handling, UI feedback, and auto-redirect for already signed-in users.
// ===============================
public class LoginActivity extends AppCompatActivity {

    // UI Components
    private static final String EMAIL_DOMAIN = "@bentory.app";
    private EditText editTextEmail;
    private EditText editTextPassword;
    private ImageButton buttonLogin;
    private TextView textViewSignUp;
    private TextView textViewForgotPassword;
    private TextView textViewSignUpLink;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_login);
            Log.d("LoginActivity", "Starting onCreate");

            // ⬛ Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();
            Log.d("LoginActivity", "Firebase Auth initialized");

            // ⬛ UI Setup
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            // ⬛ Bind Views
            Log.d("LoginActivity", "Finding views");
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPassword = findViewById(R.id.editTextPassword);
            buttonLogin = findViewById(R.id.buttonLogin);
            textViewSignUp = findViewById(R.id.textViewSignUp);
            textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
            textViewSignUpLink = findViewById(R.id.textViewSignUpLink);
            Log.d("LoginActivity", "Views found successfully");

            // Enable HTML formatting for the sign-up link
            if (textViewSignUpLink != null) {
                textViewSignUpLink.setText(android.text.Html.fromHtml("Don't have an account? <b><u>click here</u></b>",
                        android.text.Html.FROM_HTML_MODE_COMPACT));
                textViewSignUpLink.setOnClickListener(v -> {
                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivity(intent);
                });
            }

            // Handle login logic.
            buttonLogin.setOnClickListener(view -> {
                String username = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Basic validation
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create full email address
                String email = username + EMAIL_DOMAIN;

                // Show loading state
                buttonLogin.setEnabled(false);

                // Sign in with email and password
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    com.google.firebase.database.DatabaseReference userRef = FirebaseDatabase
                                            .getInstance().getReference("users").child(user.getUid());
                                    userRef.child("onboardingCompleted").addListenerForSingleValueEvent(
                                            new com.google.firebase.database.ValueEventListener() {
                                                @Override
                                                public void onDataChange(
                                                        @androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                                                    Boolean completed = snapshot.getValue(Boolean.class);
                                                    android.util.Log.d("LoginActivity",
                                                            "onboardingCompleted value: " + completed);
                                                    if (completed == null || !completed) {
                                                        // Go to onboarding
                                                        Intent intent = new Intent(LoginActivity.this,
                                                                Onboarding.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        // Go to landing page
                                                        Toast.makeText(LoginActivity.this, "Login Successful",
                                                                Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(LoginActivity.this,
                                                                LandingPage.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(
                                                        @androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                                                    // Handle error, maybe go to landing page as fallback
                                                    android.util.Log.e("LoginActivity",
                                                            "onboardingCompleted check failed: " + error.getMessage());
                                                    Intent intent = new Intent(LoginActivity.this, LandingPage.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(LoginActivity.this, "Login failed: " +
                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                buttonLogin.setEnabled(true);
                            }
                        });
            });

            // Sign-up redirection.
            textViewSignUp.setOnClickListener(view -> {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            });

            // Forgot password logic.
            textViewForgotPassword.setOnClickListener(view -> {
                String username = editTextEmail.getText().toString().trim();
                if (username.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter your username first", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create full email address for password reset
                String email = username + EMAIL_DOMAIN;

                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this,
                                        "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        "Failed to send reset email: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        } catch (Exception e) {
            Log.e("LoginActivity", "Error in onCreate", e);
            Toast.makeText(this, "An error occurred. Please try again later.", Toast.LENGTH_LONG).show();
        }
    }

    // 🚪 Auto-redirect if user is already signed in.
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            com.google.firebase.database.DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(currentUser.getUid());
            userRef.child("onboardingCompleted")
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(
                                @androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            Boolean completed = snapshot.getValue(Boolean.class);
                            android.util.Log.d("LoginActivity", "onboardingCompleted value (onStart): " + completed);
                            if (completed == null || !completed) {
                                // Go to onboarding
                                Intent intent = new Intent(LoginActivity.this, Onboarding.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Go to landing page
                                Intent intent = new Intent(LoginActivity.this, LandingPage.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(
                                @androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                            android.util.Log.e("LoginActivity",
                                    "onboardingCompleted check failed (onStart): " + error.getMessage());
                            Intent intent = new Intent(LoginActivity.this, LandingPage.class);
                            startActivity(intent);
                            finish();
                        }
                    });
        }
    }
}