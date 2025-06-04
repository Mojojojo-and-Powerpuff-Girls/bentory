package com.example.bentory_app.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bentory_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// ===============================
// SignUp Activity
//
// Purpose:
// - Allows new users to create an account for the app.
// - Validates username and password input fields before creating an account.
// - Registers the user using Firebase authentication.
// ===============================
public class SignUpActivity extends AppCompatActivity {

    // UI Components
    private static final String EMAIL_DOMAIN = "@bentory.app";
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private ImageButton buttonCreateAccount;
    private ImageButton backButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // ⬛ Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // ⬛ UI Setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ⬛ Bind Views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPasswordSignup);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        backButton = findViewById(R.id.backButtonSignup);

        // Back button: finish activity and return to previous screen.
        backButton.setOnClickListener(v -> finish());

        // Set up create account button.
        buttonCreateAccount.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            // Validate input fields
            if (!validateInputs(username, password, confirmPassword)) {
                return;
            }

            // Create full email address
            String email = username + EMAIL_DOMAIN;

            // Show loading state
            buttonCreateAccount.setEnabled(false);

            // Create user with email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign up success
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignUpActivity.this, "Account created successfully!\nEmail: " + email,
                                    Toast.LENGTH_LONG).show();
                            finish(); // Return to login screen
                        } else {
                            // If sign up fails, display a message to the user.
                            String errorMessage = task.getException() != null ? task.getException().getMessage()
                                    : "Sign up failed";
                            Toast.makeText(SignUpActivity.this, "Sign up failed: " +
                                    errorMessage, Toast.LENGTH_SHORT).show();
                            buttonCreateAccount.setEnabled(true);
                        }
                    });
        });
    }


    // ===============================
    //             METHODS
    // ===============================

    // ✅ Validate username and password input fields. (METHODS)
    private boolean validateInputs(String username, String password, String confirmPassword) {
        // Check for empty fields
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate username (should not contain spaces or special characters except
        // underscore and dot)
        if (!username.matches("^[a-zA-Z0-9._]+$")) {
            editTextUsername.setError("Username can only contain letters, numbers, dots, and underscores");
            editTextUsername.requestFocus();
            return false;
        }

        // Check username length
        if (username.length() < 3) {
            editTextUsername.setError("Username must be at least 3 characters long");
            editTextUsername.requestFocus();
            return false;
        }

        // Check password length
        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters long");
            editTextPassword.requestFocus();
            return false;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }
}