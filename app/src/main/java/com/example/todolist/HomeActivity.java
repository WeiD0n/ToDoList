package com.example.todolist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.Database;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private int userId; // Declare userId at the class level
    private LinearLayout taskContainer; // Container to hold the task views for In Progress tasks
    private RecyclerView recyclerView; // RecyclerView for tasks grouped by categories
    private TaskGroupAdapter taskGroupAdapter; // Adapter for RecyclerView (task groups with counts)
    private ImageView profileImageView;  // Declare ImageView for profile picture

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1;
    private static final int IMAGE_PICK_REQUEST_CODE = 2; // Code for picking an image
    private static final int EDIT_PROFILE_REQUEST_CODE = 2;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Ensure this is set first

        // Initialize profile image view
        profileImageView = findViewById(R.id.profile_image);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Retrieve the saved username and userId from SharedPreferences
        String username = sharedPreferences.getString("USERNAME", "User");
        userId = sharedPreferences.getInt("USER_ID", -1); // Retrieve user ID

        // Log the values for debugging purposes
        Log.d("HomeActivity", "Retrieved username: " + username);
        Log.d("HomeActivity", "Retrieved userId: " + userId);

        // Check if userId is valid, if not, show a log and redirect to login
        if (userId == -1) {
            Log.d("HomeActivity", "User is not logged in. Redirecting to login.");
            redirectToLogin();
        } else {
            Log.d("HomeActivity", "Logged-in userId: " + userId);
        }

        // Ensure the correct username is displayed in the TextView
        Log.d("HomeActivity", "Username to display: " + username);
        TextView usernameTextView = findViewById(R.id.username);
        usernameTextView.setText(username);  // This should display the correct username

        // Load profile image
        loadProfileImage();

        // Initialize the task container for in-progress tasks
        taskContainer = findViewById(R.id.task_container);

        // Inside onCreate() method
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Database database = new Database(this);
        // Check if userId is valid
        if (userId == -1) {
            Toast.makeText(HomeActivity.this, "User not logged in. Please log in again.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return; // Stop further execution if user is not logged in
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!ReminderManager.canScheduleExactAlarm(this)) {
                // Redirect the user to settings to allow the permission
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return; // Don't proceed with app initialization until permission is granted
            }
        }


        // Fetch and display tasks in progress and tasks grouped by categories
        List<Task> tasksInProgress = getTasksInProgress();
        displayInProgressTasks(tasksInProgress);

        // Fetch task groups with their counts and set RecyclerView
        List<TaskGroup> taskGroupsWithCounts = database.getTaskGroupsWithCount(userId);  // Pass userId here
        setupRecyclerView(taskGroupsWithCounts);

        // Logout Button (logs the user out)
        ImageView logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("USER_ID");
            editor.remove("USERNAME");
            editor.apply();
            redirectToLogin();
            clearSharedPreferences(); // Call this method on logout

        });

        // Add Task Button
        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(HomeActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // View Task Button
        Button viewTaskButton = findViewById(R.id.viewTaskButton);
        viewTaskButton.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(HomeActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(HomeActivity.this, ViewTaskActivity.class);
            startActivity(intent);
        });

        // Home Button ImageView
        ImageView homeButton = findViewById(R.id.home_button_image);
        homeButton.setOnClickListener(v -> recreate());  // Handle home button click

        // Edit Profile Button ImageView
        ImageView editProfileButton = findViewById(R.id.edit_profile_button_image);
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        ImageView editTaskButton = findViewById(R.id.edit_task_image);
        editTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EditTask.class);
            startActivity(intent);
        });

        // Check for storage permission to load profile image
        checkStoragePermission();
    }

    // Fetch tasks that are in progress (horizontal scroll view)
    public List<Task> getTasksInProgress() {
        Database database = new Database(this);
        return database.getTasksInProgress(userId); // Fetch tasks in progress
    }

    // Fetch task groups with counts from the database
    public List<TaskGroup> getTaskGroupsWithCount() {
        Database database = new Database(this);
        return database.getTaskGroupsWithCount(userId); // Fetch task groups with count of tasks
    }

    // Display in-progress tasks in the horizontal section
    private void displayInProgressTasks(List<Task> tasksInProgress) {
        if (tasksInProgress.isEmpty()) {
            // Show a message if no tasks are in progress
            TextView noTasksMessage = new TextView(this);
            noTasksMessage.setText("No tasks in progress.");
            noTasksMessage.setTextSize(18f);
            noTasksMessage.setPadding(16, 8, 16, 8);
            taskContainer.addView(noTasksMessage);
        } else {
            // Dynamically add tasks to the LinearLayout for horizontal view
            for (Task task : tasksInProgress) {
                TextView taskTextView = new TextView(this);
                taskTextView.setText(task.getTitle()); // Display only the title of the task
                taskTextView.setPadding(16, 8, 16, 8);
                taskTextView.setBackgroundResource(R.drawable.task_background); // Optional background
                taskTextView.setTextSize(16f);

                // Add the TextView (task) to the task container (horizontal view)
                taskContainer.addView(taskTextView);
            }
        }
    }

    // Setup RecyclerView method
    private void setupRecyclerView(List<TaskGroup> taskGroups) {
        if (!taskGroups.isEmpty()) {
            taskGroupAdapter = new TaskGroupAdapter(taskGroups); // Use TaskGroupAdapter
            recyclerView.setAdapter(taskGroupAdapter);
        } else {
            // Handle empty list scenario if needed
            taskGroupAdapter = new TaskGroupAdapter(taskGroups); // Empty list
            recyclerView.setAdapter(taskGroupAdapter);
        }
    }

    // Redirect to login if user is not logged in
    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Method to load the profile image from SharedPreferences and set it in the ImageView
    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String imageUriString = prefs.getString("PROFILE_IMAGE_URI", "");

        if (!imageUriString.isEmpty()) {
            Uri imageUri = Uri.parse(imageUriString);
            profileImageView.setImageURI(imageUri);
        } else {
            // Set default image if no image URI exists
            profileImageView.setImageResource(R.drawable.base_profile);
        }
    }

    // Request permissions if needed (for external storage access)
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    // Handle the result of permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with profile image loading or any other functionality
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void clearSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();  // Clears all saved preferences
        editor.apply();
    }

}
