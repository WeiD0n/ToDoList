package com.example.todolist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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

    private int userId;
    private LinearLayout taskContainer;
    private RecyclerView recyclerView;
    private TaskGroupAdapter taskGroupAdapter;
    private ImageView profileImageView;
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
        homeButton = findViewById(R.id.home_button_image);
        editProfileButton = findViewById(R.id.edit_profile_button_image);
        profileImageView = findViewById(R.id.profile_image);

        editTaskImageView = findViewById(R.id.edit_task_image);

        // Shared preferences and user ID setup
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("USER_ID", -1);
        String username = sharedPreferences.getString("USERNAME", "User");

        // Check if userId is valid
        if (userId == -1) {
            Log.d("HomeActivity", "Invalid userId: " + userId + ". Cannot proceed.");
            redirectToLogin();
            return;
        }

        // Extract username and update UI
        if (username.contains("@")) {
            username = username.split("@")[0];
        }
        TextView usernameTextView = findViewById(R.id.username);
        usernameTextView.setText(username);

        // Load profile image
        loadProfileImage();

        // Schedule reminders after login or app startup (only if the user is valid)
        if (userId != -1) {  // Ensure only scheduling reminders for a valid user
            ReminderManager.scheduleReminders(getApplicationContext(), userId);
        }

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
            Intent intent = new Intent(HomeActivity.this, EditTask.class);
            startActivity(intent);
        });

        // Set home button listener (refresh current activity)
        homeButton.setOnClickListener(v -> {
            recreate();  // Refresh the activity by calling recreate()
        });

    }

    // Method to load profile image from SharedPreferences
    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int imageResId = prefs.getInt("PROFILE_IMAGE_RESOURCE", R.drawable.base_profile);
        profileImageView.setImageResource(imageResId);
    }

    // Fetch tasks that are in progress (horizontal scroll view)
    public List<Task> getTasksInProgress() {
        Database database = new Database(this);
        return database.getTasksInProgress(userId);
    }

    // Fetch task groups with counts from the database
    public List<TaskGroup> getTaskGroupsWithCount() {
        Database database = new Database(this);
        return database.getTaskGroupsWithCount(userId);
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

                taskCountTextView.setText(title + "\n" + description);

                // Set appropriate image and background drawable based on task group
                switch (task.getGroup()) {
                    case "School":
                        groupTaskIconImageView.setImageResource(R.drawable.school);
                        taskView.setBackgroundResource(R.drawable.school_background);
                        break;
                    case "Work":
                        groupTaskIconImageView.setImageResource(R.drawable.work);
                        taskView.setBackgroundResource(R.drawable.work_background);
                        break;
                    case "Chores":
                        groupTaskIconImageView.setImageResource(R.drawable.chores);
                        taskView.setBackgroundResource(R.drawable.chores_background);
                        break;
                    default:
                        groupTaskIconImageView.setImageResource(R.drawable.category_background);
                        taskView.setBackgroundResource(R.drawable.category_background);
                        break;
                }

                // Adjusting the height and width of the task view directly
                ViewGroup.LayoutParams params = taskView.getLayoutParams();
                params.height = 300;
                params.width = 600;
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

}
