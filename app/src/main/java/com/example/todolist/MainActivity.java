package com.example.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 100; // Code for permission result

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the app has permission to schedule exact alarms (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!ReminderManager.canScheduleExactAlarm(this)) {
                // Redirect the user to settings to allow the permission
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivityForResult(intent, REQUEST_CODE_PERMISSION);  // Start activity for result
                return; // Don't proceed with app initialization until permission is granted
            }
        }

        // Check if the user is already logged in (via SharedPreferences)
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1); // Get the saved user ID
        String savedEmail = prefs.getString("EMAIL", null); // Get the saved email

        if (userId != -1) {  // Ensure only scheduling reminders for a valid user
            ReminderManager.scheduleReminders(getApplicationContext(), userId);
        }
        // If both userId and email are valid, check if the user exists in the database
        if (userId != -1 && savedEmail != null) {
            Database dbHelper = new Database(this);
            boolean userExists = dbHelper.checkUserExists(userId, savedEmail); //method to check if the user exists in DB

            if (userExists) {
                // User is already logged in and exists, proceed to the home screen
                navigateToHome();
                return;
            } else {
                // User does not exist, clear saved data and proceed to login screen
                clearLoginData();
            }
        }

        // If no valid user found or no user logged in, go to the Login screen
        navigateToLogin();
    }

    // Method to clear any saved login data
    private void clearLoginData() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("USER_ID");  // Remove saved USER_ID
        editor.remove("EMAIL");    // Remove saved EMAIL
        editor.apply();            // Apply changes
    }

    // Method to navigate to the Home screen
    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);  // Or whichever activity is the home screen
        startActivity(intent);
        finish(); // Close MainActivity to prevent returning to it
    }

    // Method to navigate to the Login screen
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);  // Go to login screen
        startActivity(intent);
        finish(); // Close MainActivity to prevent returning to it
    }

    // Handle the result from requesting permissions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PERMISSION) {
            // Handle exact alarm permission result
            if (ReminderManager.canScheduleExactAlarm(this)) {
                // Permission granted, proceed with checking login state
                checkLoginState();
            } else {
                // Permission denied, show a message and exit
                Toast.makeText(this, "Permission to schedule exact alarms is required.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // Helper method to check login state and navigate accordingly
    private void checkLoginState() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);  // Get the saved user ID
        String savedEmail = prefs.getString("EMAIL", null); // Get the saved email

        // If both userId and email are valid, check if the user exists in the database
        if (userId != -1 && savedEmail != null) {
            Database dbHelper = new Database(this);
            boolean userExists = dbHelper.checkUserExists(userId, savedEmail);

            if (userExists) {
                navigateToHome();
            } else {
                clearLoginData();
                navigateToLogin();
            }
        } else {
            navigateToLogin();
        }
    }

}
