package com.example.todolist;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordResetActivity extends AppCompatActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button resetButton;
    private Database dbHelper; // SQLite helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset); // Set the layout for the activity

        // Initialize UI elements
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.edit_password);
        confirmPasswordInput = findViewById(R.id.edit_password_confirm);
        resetButton = findViewById(R.id.btn_save);

        dbHelper = new Database(this); // Initialize the SQLite database helper

        // Set up the reset button click listener
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user inputs
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                String confirmPassword = confirmPasswordInput.getText().toString();

                // Perform validation
                if (email.isEmpty()) {
                    Toast.makeText(PasswordResetActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(PasswordResetActivity.this, "Please enter a new password", Toast.LENGTH_SHORT).show();
                } else if (confirmPassword.isEmpty()) {
                    Toast.makeText(PasswordResetActivity.this, "Please confirm your password", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(PasswordResetActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // Proceed with hashing the password and updating the database
                    String salt = generateSalt();
                    String hashedPassword = hashPassword(password, salt);

                    // Update the password in the database
                    boolean isUpdated = dbHelper.updatePassword(email, hashedPassword, salt);

                    if (isUpdated) {
                        Toast.makeText(PasswordResetActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Optionally close the activity or navigate to another screen
                    } else {
                        Toast.makeText(PasswordResetActivity.this, "Error updating password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
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
}
