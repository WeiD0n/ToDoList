package com.example.todolist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private int userId; // Declare userId at the class level
    private LinearLayout taskContainer; // Container to hold the task views for In Progress tasks
    private RecyclerView recyclerView; // RecyclerView for tasks grouped by categories
    private TaskGroupAdapter taskGroupAdapter; // Adapter for RecyclerView (task groups with counts)
    private ImageView profileImageView;  // Declare ImageView for profile picture
    private ImageView homeButton;
    private ImageView editProfileButton;
    private ImageView editTaskImageView;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1;
    private static final int EDIT_PROFILE_REQUEST_CODE = 2;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize buttons and ImageViews
        homeButton = findViewById(R.id.home_button_image);  // Ensure this ID matches your XML
        editProfileButton = findViewById(R.id.edit_profile_button_image);  // Initialize the edit profile button
        profileImageView = findViewById(R.id.profile_image);
        editTaskImageView = findViewById(R.id.edit_task_image);// Initialize the profile image view
        findViewById(R.id.viewTaskButton);

        // Shared preferences and user ID setup
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("USER_ID", -1);  // Default to -1 if not found
        String username = sharedPreferences.getString("USERNAME", "User");

        // Check if userId is valid
        if (userId == -1) {
            Log.d("HomeActivity", "Invalid userId: " + userId + ". Cannot proceed.");
            redirectToLogin();  // Redirect to login if invalid
            return;
        }

        // Extract username and update UI
        if (username.contains("@")) {
            username = username.split("@")[0];  // Extract username part before '@'
        }
        TextView usernameTextView = findViewById(R.id.username);
        usernameTextView.setText(username);

        // Load profile image
        loadProfileImage();

        // Initialize task container and RecyclerView for tasks
        taskContainer = findViewById(R.id.task_container);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch and display tasks
        Database database = new Database(this);
        List<Task> tasksInProgress = getTasksInProgress();
        displayInProgressTasks(tasksInProgress);

        List<TaskGroup> taskGroupsWithCounts = database.getTaskGroupsWithCount(userId);
        setupRecyclerView(taskGroupsWithCounts);

        // Handle logout
        ImageView logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("USER_ID");
            editor.remove("USERNAME");
            editor.apply();
            redirectToLogin();
        });

        // Add new task button
        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(HomeActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        Button viewTaskButton = findViewById(R.id.viewTaskButton);
        viewTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ViewTaskActivity.class);
            startActivity(intent);
        });

        // Set the profile image edit button listener
        editProfileButton.setClickable(true);
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
        });

        editTaskImageView.setClickable(true);
        editTaskImageView.setOnClickListener(v -> {
            // Create an intent to start the EditTask activity
            Intent intent = new Intent(HomeActivity.this, EditTask.class);

            // Optional: If you want to pass additional data, e.g., a task ID
            // intent.putExtra("TASK_ID", taskId);  // Use the actual task ID you want to edit

            // Start the EditTask activity
            startActivity(intent);
        });



        // Set home button listener (refresh current activity)
        homeButton.setOnClickListener(v -> {
            recreate();  // Refresh the activity by calling recreate()
        });

        // Check for storage permission
        checkStoragePermission();
    }


    // Method to save profile image URI to SharedPreferences
    private void saveProfileImageUri(Uri imageUri) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("PROFILE_IMAGE_URI", imageUri.toString());
        editor.apply();
    }

    // Method to load profile image from SharedPreferences
    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String imageUriString = prefs.getString("PROFILE_IMAGE_URI", "");

        if (!imageUriString.isEmpty()) {
            Uri imageUri = Uri.parse(imageUriString);
            profileImageView.setImageURI(imageUri);
        } else {
            profileImageView.setImageResource(R.drawable.base_profile);
        }
    }

    // Handle Activity result for profile image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == EDIT_PROFILE_REQUEST_CODE) {
                Uri imageUri = data.getData();  // Get URI of selected image

                if (imageUri != null) {
                    // Save the URI to SharedPreferences
                    saveProfileImageUri(imageUri);
                    // Reload the profile image to reflect the change
                    loadProfileImage();
                }
            }
        }
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

    // Display tasks that are in progress
    private void displayInProgressTasks(List<Task> tasksInProgress) {
        if (tasksInProgress.isEmpty()) {
            TextView noTasksMessage = new TextView(this);
            noTasksMessage.setText("No tasks in progress.");
            noTasksMessage.setTextSize(18f);
            noTasksMessage.setPadding(16, 8, 16, 8);
            taskContainer.addView(noTasksMessage);
        } else {
            for (Task task : tasksInProgress) {
                View taskView = getLayoutInflater().inflate(R.layout.task_items, taskContainer, false);

                TextView groupNameTextView = taskView.findViewById(R.id.group_name);
                ImageView groupTaskIconImageView = taskView.findViewById(R.id.groupTaskIcon);
                TextView taskCountTextView = taskView.findViewById(R.id.task_count);

                String groupName = task.getGroup();
                groupNameTextView.setText(groupName);

                String title = task.getTitle();
                String description = task.getDescription();

                SpannableString spannableString = new SpannableString(title + "\n" + description);
                int titleEndIndex = title.length();
                spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, titleEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new ForegroundColorSpan(Color.GRAY), titleEndIndex + 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                taskCountTextView.setText(spannableString);

                // Set appropriate image based on task group
                switch (task.getGroup()) {
                    case "School":
                        groupTaskIconImageView.setImageResource(R.drawable.school);
                        break;
                    case "Work":
                        groupTaskIconImageView.setImageResource(R.drawable.work);
                        break;
                    case "Chores":
                        groupTaskIconImageView.setImageResource(R.drawable.chores);
                        break;
                    default:
                        groupTaskIconImageView.setImageResource(R.drawable.category_background);
                        break;
                }

                if (task.getGroup().equals("School")) {
                    taskView.setBackgroundResource(R.drawable.school_background);
                } else if (task.getGroup().equals("Work")) {
                    taskView.setBackgroundResource(R.drawable.work_background);
                } else if (task.getGroup().equals("Chores")) {
                    taskView.setBackgroundResource(R.drawable.chores_background);
                }

                // Set the layout params for the task view (width and height)
                ViewGroup.LayoutParams params = taskView.getLayoutParams();
                params.width = (int) (200 * getResources().getDisplayMetrics().density);
                params.height = (int) (100 * getResources().getDisplayMetrics().density);
                taskView.setLayoutParams(params);

                taskContainer.addView(taskView);
            }
        }
    }

    // Setup RecyclerView for task groups
    private void setupRecyclerView(List<TaskGroup> taskGroups) {
        if (!taskGroups.isEmpty()) {
            taskGroupAdapter = new TaskGroupAdapter(taskGroups);
            recyclerView.setAdapter(taskGroupAdapter);
        } else {
            taskGroupAdapter = new TaskGroupAdapter(taskGroups);
            recyclerView.setAdapter(taskGroupAdapter);
        }
    }

    // Redirect to login screen if user is not logged in
    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Check for storage permissions
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
