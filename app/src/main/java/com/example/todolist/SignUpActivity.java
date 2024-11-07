package com.example.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import android.util.Base64;

public class SignUpActivity extends AppCompatActivity {

    private ImageView passwordToggle, confirmPasswordToggle;
    private EditText emailInput, passwordInput, confirmPasswordInput;
    private TextView passwordStrengthIndicator, registerOption;
    private Button signupButton;
    private CheckBox rememberMeCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Check if the user is already logged in before setting the content view
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1); // Get the saved user ID
        String savedEmail = prefs.getString("EMAIL", null); // Get the saved email

        // If the user is already logged in, redirect to HomeActivity
        if (userId != -1 && savedEmail != null) {
            Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Close the SignUpActivity
            return; // Prevent further code execution
        }

        setContentView(R.layout.activity_signup);

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        passwordStrengthIndicator = findViewById(R.id.passwordStrengthIndicator);
        signupButton = findViewById(R.id.signUpButton);
        passwordToggle = findViewById(R.id.eyeIcon);
        confirmPasswordToggle = findViewById(R.id.confirmEyeIcon);
        registerOption = findViewById(R.id.registerOption);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox); // Initialize the CheckBox

        // Set up password toggle functionality
        passwordToggle.setOnClickListener(v -> togglePasswordVisibility(passwordInput, passwordToggle));
        confirmPasswordToggle.setOnClickListener(v -> togglePasswordVisibility(confirmPasswordInput, confirmPasswordToggle));

        // Password Strength Indicator
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrengthIndicator(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listener for sign up
        signupButton.setOnClickListener(v -> {
            // Get user input
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            // Validate user input
            if (validateInput(email, password, confirmPassword)) {
                // If valid, proceed to register user
                registerUser(email, password);
            }
        });

        // Set up click listener for register option to navigate to LoginActivity
        registerOption.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close SignUpActivity
        });
    }

    // Toggle password visibility
    private void togglePasswordVisibility(EditText passwordField, ImageView toggleIcon) {
        if (passwordField.getTransformationMethod() instanceof PasswordTransformationMethod) {
            passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.baseline_remove_red_eye_24); // Eye open icon
        } else {
            passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.baseline_visibility_off_24); // Eye closed icon
        }
        passwordField.setSelection(passwordField.length()); // Move cursor to the end
    }

    // Password strength indicator
    private void updatePasswordStrengthIndicator(String password) {
        passwordStrengthIndicator.setVisibility(View.VISIBLE);

        if (password.length() < 6) {
            passwordStrengthIndicator.setText("Weak");
            passwordStrengthIndicator.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (password.length() < 10) {
            passwordStrengthIndicator.setText("Medium");
            passwordStrengthIndicator.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            passwordStrengthIndicator.setText("Strong");
            passwordStrengthIndicator.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private String generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(salt.getBytes());
            byte[] hashedPassword = messageDigest.digest(password.getBytes());
            return Base64.encodeToString(hashedPassword, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Validate user input
    private boolean validateInput(String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            passwordInput.setError("Password must contain at least one uppercase letter");
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            passwordInput.setError("Password must contain at least one lowercase letter");
            return false;
        }

        if (!password.matches(".*[!@#$%^&*()_+{}|:;<>?,./].*")) {
            passwordInput.setError("Password must contain at least one special character");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    // Register the user
    public void registerUser(String email, String password) {
        // Hash the password and generate a salt
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);

        Database dbHelper = new Database(this);

        // Attempt to add user to the database
        boolean isUserAdded = dbHelper.addUser(email, hashedPassword, email, salt);

        if (isUserAdded) {
            // Immediately retrieve the user ID of the newly added user
            int newUserId = dbHelper.getUserIdByEmail(email); // Fetch the user ID right after adding

            if (newUserId != -1) { // Check if user ID is valid
                // Save the user ID, email, and username in SharedPreferences
                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("USER_ID", newUserId);  // Save user ID
                editor.putString("EMAIL", email);    // Save email

                // Extract the username from the email (before the @ symbol)
                String username = email.split("@")[0]; // Get everything before the '@'
                editor.putString("USERNAME", username); // Save username

                // Set default profile image URI
                Uri defaultImageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.base_profile);
                editor.putString("PROFILE_IMAGE_URI", defaultImageUri.toString()); // Save default image URI
                editor.apply();

                // Save login time
                saveLoginTime();

                // Save email if "Remember Me" is checked
                if (rememberMeCheckbox.isChecked()) {
                    saveCredentials(email); // Save the email
                } else {
                    clearSavedCredentials(); // Clear the saved email
                }

                // Show success message
                Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();

                // Redirect to HomeActivity after sign-up
                Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // Close SignUpActivity
            } else {
                Toast.makeText(SignUpActivity.this, "Failed to retrieve user ID. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SignUpActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLoginTime() {
        // Get the current timestamp and format it
        String currentTime = DateUtils.formatDate(new Date());

        // Save the formatted login time in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("LAST_LOGIN_TIME", currentTime);
        editor.apply();
        Log.d("SignUpActivity", "Login time saved: " + currentTime);
    }

    private void saveCredentials(String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedEmail", email);
        editor.apply();
        Log.d("SignUpActivity", "Saved email: " + email);
    }

    private void clearSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("savedEmail");
        editor.apply();
        Log.d("SignUpActivity", "Cleared saved email");
    }
}
