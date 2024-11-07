package com.example.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.DateUtils;

import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private CheckBox rememberMeCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        TextView loginOption = findViewById(R.id.loginOption);
        TextView forgotPassword = findViewById(R.id.forgotPassword);

        // Load saved credentials if any
        loadSavedCredentials();

        // Set up login button click listener
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Validate the credentials before login
            if (validateCredentials(email, password)) {
                loginUser(email, password);
            }
        });

        // Set up register text click listener
        loginOption.setOnClickListener(v -> {
            // Redirect to SignUpActivity
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        // Set up forgot password click listener
        forgotPassword.setOnClickListener(v -> {
            // Redirect to PasswordResetActivity
            startActivity(new Intent(LoginActivity.this, PasswordResetActivity.class));
        });
    }

    private void loginUser(String email, String password) {
        Database dbHelper = new Database(this);

        // Check if the email is registered
        if (!dbHelper.isEmailRegistered(email)) {
            emailInput.setError("Email is not registered");
            return; // Stop the login process
        }

        // Retrieve the stored hashed password and salt from the database
        String[] userDetails = dbHelper.getUserDetails(email);
        if (userDetails == null) {
            emailInput.setError("Error retrieving user details");
            return; // Stop if user details are not found
        }

        String storedHashedPassword = userDetails[0]; // Hashed password
        String storedSalt = userDetails[1]; // Salt

        // Hash the entered password with the stored salt
        String hashedInputPassword = PasswordUtility.hashPassword(password, storedSalt);

        // Compare the hashed passwords
        if (hashedInputPassword.equals(storedHashedPassword)) {
            // Retrieve the user ID from the database
            int userId = dbHelper.getUserIdByEmail(email);
            Log.d("LoginActivity", "Retrieved userId: " + userId); // Log the retrieved userId

            // Check if the user ID is valid (not -1)
            if (userId == -1) {
                Log.e("LoginActivity", "Error: Invalid userId (-1), cannot proceed.");
                Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show();
                return; // Stop the login process if userId is invalid
            }

            // Extract only the username (before the '@' symbol)
            String username = email.contains("@") ? email.split("@")[0] : email;  // Save only the part before '@'
            Log.d("LoginActivity", "Retrieved username: " + username);

            // Clear previous user data and save new credentials
            clearUserData();
            saveUserId(userId);  // Save the user ID
            saveUsername(username); // Save only the username

            // Save login time
            saveLoginTime();

            // Save email if "Remember Me" is checked
            if (rememberMeCheckbox.isChecked()) {
                saveCredentials(email); // Save the email
            } else {
                clearSavedCredentials(); // Clear the saved email
            }

            // Proceed to the next activity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("USER_ID", userId); // Pass the user ID to the next activity
            startActivity(intent);
            finish(); // Close this activity
        } else {
            // Password does not match
            passwordInput.setError("Incorrect password");
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
        Log.d("LoginActivity", "Login time saved: " + currentTime);
    }

    private void clearUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear all user data in MyAppPrefs
        editor.apply();
        Log.d("LoginActivity", "User data cleared");
    }

    private void saveUserId(int userId) {
        if (userId != -1) {
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("USER_ID", userId); // Save the user ID
            editor.apply();
            Log.d("LoginActivity", "User ID saved: " + userId);
        } else {
            Log.e("LoginActivity", "Error: Invalid userId (-1), not saving.");
        }
    }

    private void saveUsername(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USERNAME", username); // Save the username only (no email)
        editor.apply();
        Log.d("LoginActivity", "Username saved: " + username);
    }

    private void loadSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE); // Updated to use the same prefs file
        String savedEmail = sharedPreferences.getString("savedEmail", null);
        if (savedEmail != null) {
            emailInput.setText(savedEmail);
            rememberMeCheckbox.setChecked(true);
            Log.d("LoginActivity", "Loaded saved email: " + savedEmail);
        } else {
            Log.d("LoginActivity", "No saved email found.");
        }
    }

    private void saveCredentials(String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE); // Updated to use the same prefs file
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedEmail", email);
        editor.apply();
        Log.d("LoginActivity", "Saved email: " + email);
    }

    private void clearSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE); // Updated to use the same prefs file
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("savedEmail"); // Only remove email
        editor.apply();
        Log.d("LoginActivity", "Cleared saved email");
    }

    private boolean validateCredentials(String email, String password) {
        // Trim spaces
        email = email.trim();
        password = password.trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an email address.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
