package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {
    private EditText emailInput;
    private Button resetButton;
    private FirebaseAuth auth; // Firebase Authentication
    private Database dbHelper;  // Local SQLite Database helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        // Initialize UI elements
        emailInput = findViewById(R.id.email_input);
        resetButton = findViewById(R.id.btn_save);

        ImageView backButton = findViewById(R.id.go_back);
        backButton.setOnClickListener(v -> onBackPressed()); // Back button functionality

        auth = FirebaseAuth.getInstance(); // Firebase Authentication
        dbHelper = new Database(this); // Local SQLite Database helper

        // Set up the reset button click listener
        resetButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(PasswordResetActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                // Send Firebase password reset email
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Firebase reset successful
                                Toast.makeText(PasswordResetActivity.this, "Password reset email sent. Check your inbox.", Toast.LENGTH_SHORT).show();

                                // After resetting via Firebase, update local database
                                updateLocalDatabase(email);

                                finish(); // Close the activity
                            } else {
                                // Firebase reset failed
                                Toast.makeText(PasswordResetActivity.this, "Failed to send reset email. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void updateLocalDatabase(String email) {
        // Generate a new salt and hash the new password
        String salt = PasswordUtility.generateSalt();
        String newHashedPassword = PasswordUtility.hashPassword("newPasswordFromFirebase", salt);  // Placeholder for the actual new password

        // Update the password in the local database
        boolean isUpdated = dbHelper.updatePassword(email, newHashedPassword, salt);

        if (isUpdated) {
            Toast.makeText(PasswordResetActivity.this, "Password updated in local database", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(PasswordResetActivity.this, "Error updating local database", Toast.LENGTH_SHORT).show();
        }
    }
}
