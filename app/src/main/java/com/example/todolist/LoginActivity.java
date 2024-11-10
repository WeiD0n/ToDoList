package com.example.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.DateUtils;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private CheckBox rememberMeCheckbox;
    private Database dbHelper;
    private ImageView passwordToggle;  // Declare the eye icon for password visibility toggle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dbHelper = new Database(this);
        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        TextView loginOption = findViewById(R.id.loginOption);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        passwordToggle = findViewById(R.id.eyeIcon);

        // Load saved credentials if any
        loadSavedCredentials();
        passwordToggle.setOnClickListener(v -> togglePasswordVisibility(passwordInput, passwordToggle));

        // SharedPreferences for checking saved email and user ID
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("savedEmail", null);
        int savedUserId = sharedPreferences.getInt("USER_ID", -1);

        // If both saved email and user ID are found, skip login and go directly to HomeActivity
        if (savedEmail != null && savedUserId != -1) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("USER_ID", savedUserId);
            startActivity(intent);
            finish();  // Close the login activity
        }

        // Proceed with normal login flow if no saved user
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
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        // Set up forgot password click listener
        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, PasswordResetActivity.class));
        });
    }

    private void togglePasswordVisibility(EditText passwordField, ImageView toggleIcon) {
        if (passwordField.getTransformationMethod() instanceof android.text.method.PasswordTransformationMethod) {
            passwordField.setTransformationMethod(null);
            toggleIcon.setImageResource(R.drawable.baseline_remove_red_eye_24);  // Show the open eye icon
        } else {
            passwordField.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
            toggleIcon.setImageResource(R.drawable.baseline_visibility_off_24);  // Show the closed eye icon
        }
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

        // Check Firebase Authentication for any updates
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                        // Compare Firebase password with the local password
                        if (!hashedInputPassword.equals(storedHashedPassword)) {
                            // Generate new salt and hash password
                            String newSalt = PasswordUtility.generateSalt();
                            String newHashedPassword = PasswordUtility.hashPassword(password, newSalt);

                            // Update the local database
                            dbHelper.updatePassword(email, newHashedPassword, newSalt);

                            Toast.makeText(this, "Local password updated to match Firebase.", Toast.LENGTH_SHORT).show();
                        }

                        // Retrieve the user ID and proceed with login
                        int userId = dbHelper.getUserIdByEmail(email);
                        proceedToHome(userId, email);
                    } else {
                        passwordInput.setError("Incorrect password");
                    }
                });
    }

    private void proceedToHome(int userId, String email) {
        // Retrieve the updated username from the database
        String username = dbHelper.getUsernameById(userId);
        saveUsername(username); // Save the updated username
        saveUserId(userId);
        saveLoginTime();

        if (rememberMeCheckbox.isChecked()) {
            saveCredentials(email, userId); // Save the email
        } else {
            clearSavedCredentials(); // Clear the saved email
        }

        // Proceed to the next activity
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("USER_ID", userId); // Pass the user ID to the next activity
        startActivity(intent);
        finish(); // Close this activity
    }


    private void saveLoginTime() {
        // Get the current timestamp and format it
        String currentTime = DateUtils.formatDate(new Date());

        // Save the formatted login time in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("LAST_LOGIN_TIME", currentTime);
        editor.apply();
    }

    private void saveUserId(int userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);  // Use the same SharedPreferences name
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("USER_ID", userId);
        editor.apply();
    }


    private void saveUsername(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USERNAME", username); // Save the updated username
        editor.apply();
        Log.d("LoginActivity", "Updated username saved: " + username);
    }

    private void loadSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("savedEmail", null);
        int savedUserId = sharedPreferences.getInt("USER_ID", -1);

        Log.d("LoginActivity", "Loaded saved email: " + savedEmail);
        Log.d("LoginActivity", "Loaded saved user ID: " + savedUserId);

        // If email is saved, populate the UI
        if (savedEmail != null) {
            emailInput.setText(savedEmail);
            rememberMeCheckbox.setChecked(true);
        } else {
            Log.d("LoginActivity", "No saved email found.");
        }

        if (savedUserId == -1) {
            Log.e("LoginActivity", "No valid saved user ID found. It may not have been saved correctly.");
        }

        // Fetch and load the username
        String username = sharedPreferences.getString("USERNAME", ""); // Fetch the stored updated username
        Log.d("LoginActivity", "Loaded saved username: " + username);
        // You can now use the loaded username for UI or other purposes
    }


    private void saveCredentials(String email, int userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save email and user ID to SharedPreferences
        editor.putString("savedEmail", email);
        editor.putInt("USER_ID", userId);  // Saving the user ID
        editor.apply();
    }

    private void clearSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("savedEmail");
        editor.apply();
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
