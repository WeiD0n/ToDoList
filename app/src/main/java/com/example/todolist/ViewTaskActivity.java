package com.example.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewTaskActivity extends AppCompatActivity {

    private TextView allTextView, todoTextView, inProgressTextView, completedTextView, currentlySelectedCategory;
    private long lastSelectedDateInMillis = -1;
    private LinearLayout calendarLayout, taskContainer;
    private Database database;
    private int userId;
    private View selectedDateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);

        userId = getUserIdFromIntentOrPrefs();
        database = new Database(this);
        calendarLayout = findViewById(R.id.calendarLayout);
        taskContainer = findViewById(R.id.taskContainer);

        allTextView = findViewById(R.id.text_all);
        todoTextView = findViewById(R.id.text_todo);
        inProgressTextView = findViewById(R.id.text_in_progress);
        completedTextView = findViewById(R.id.text_completed);

        allTextView.setOnClickListener(v -> toggleCategoryHighlight(allTextView));
        todoTextView.setOnClickListener(v -> toggleCategoryHighlight(todoTextView));
        inProgressTextView.setOnClickListener(v -> toggleCategoryHighlight(inProgressTextView));
        completedTextView.setOnClickListener(v -> toggleCategoryHighlight(completedTextView));

        populateCalendar();
        setupClickListeners();

        // Call scheduleReminders only once, not on every Activity restart
        if (userId != -1) {
            ReminderManager.scheduleReminders(getApplicationContext(), userId);
        }
    }

    private int getUserIdFromIntentOrPrefs() {
        Intent intent = getIntent();
        int userId = intent.getIntExtra("USER_ID", -1);
        if (userId == -1) {
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            userId = sharedPreferences.getInt("USER_ID", -1);
        }
        if (userId == -1) {
            Log.e("ViewTaskActivity", "User ID not found in Intent or SharedPreferences");
        }
        return userId;
    }

    private void setupClickListeners() {
        ImageView backViewTaskButton = findViewById(R.id.backViewTaskButton);
        backViewTaskButton.setOnClickListener(v -> startActivity(new Intent(ViewTaskActivity.this, HomeActivity.class)));

        ImageView homeButton = findViewById(R.id.home_button_image);
        homeButton.setOnClickListener(v -> startActivity(new Intent(ViewTaskActivity.this, HomeActivity.class)));

        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewTaskActivity.this, AddTaskActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        ImageView editProfile = findViewById(R.id.edit_profile_button_image);
        editProfile.setOnClickListener(v -> startActivity(new Intent(ViewTaskActivity.this, EditProfileActivity.class)));

        ImageView editTaskButton = findViewById(R.id.edit_task_image);
        editTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewTaskActivity.this, EditTask.class);
            startActivity(intent);
        });
    }

    private void populateCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= daysInMonth; day++) {
            calendar.set(Calendar.DAY_OF_MONTH, day);
            String monthName = new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime());
            String dayOfWeek = new SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.getTime());

            View dateView = createDateView(monthName, day, dayOfWeek, year, month);
            calendarLayout.addView(dateView);
        }

        highlightToday(currentDay, daysInMonth, month, year);
    }

    private View createDateView(String monthName, int day, String dayOfWeek, int year, int month) {
        View dateView = LayoutInflater.from(this).inflate(R.layout.date_item, null);

        // Set custom width for each date view to fit 5 on the screen
        int dateViewWidth = getResources().getDisplayMetrics().widthPixels / 5;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dateViewWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        dateView.setLayoutParams(params);

        TextView monthText = dateView.findViewById(R.id.text_month);
        TextView dateText = dateView.findViewById(R.id.text_date);
        TextView dayText = dateView.findViewById(R.id.text_day);

        monthText.setText(monthName);
        dateText.setText(String.valueOf(day));
        dayText.setText(dayOfWeek);

        dateView.setOnClickListener(v -> handleDateSelection(dateView, dateText, monthText, dayText, year, month, day));

        return dateView;
    }

    private void handleDateSelection(View dateView, TextView dateText, TextView monthText, TextView dayText, int year, int month, int day) {
        if (selectedDateView != null) {
            resetDateView(selectedDateView);
        }

        selectedDateView = dateView;
        setHighlighted(dateView, dateText, monthText, dayText);

        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(year, month, day);
        lastSelectedDateInMillis = selectedCalendar.getTimeInMillis();  // This updates the timestamp

        highlightAllCategory();
    }

    private void highlightToday(int currentDay, int daysInMonth, int month, int year) {
        if (currentDay <= daysInMonth && month == Calendar.getInstance().get(Calendar.MONTH) && year == Calendar.getInstance().get(Calendar.YEAR)) {
            int indexToHighlight = currentDay - 1;
            selectedDateView = calendarLayout.getChildAt(indexToHighlight);
            selectedDateView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dateViewBackGroundColor)));

            TextView dateText = selectedDateView.findViewById(R.id.text_date);
            TextView monthText = selectedDateView.findViewById(R.id.text_month);
            TextView dayText = selectedDateView.findViewById(R.id.text_day);

            dateText.setTextColor(Color.WHITE);
            monthText.setTextColor(Color.WHITE);
            dayText.setTextColor(Color.WHITE);

            lastSelectedDateInMillis = Calendar.getInstance().getTimeInMillis();
            highlightAllCategory();
        }
    }

    private void resetDateView(View dateView) {
        dateView.setBackgroundResource(R.drawable.date_background);
        dateView.setBackgroundTintList(null);

        TextView previousDateText = dateView.findViewById(R.id.text_date);
        TextView previousMonthText = dateView.findViewById(R.id.text_month);
        TextView previousDayText = dateView.findViewById(R.id.text_day);

        previousDateText.setTextColor(Color.BLACK);
        previousMonthText.setTextColor(Color.GRAY);
        previousDayText.setTextColor(Color.GRAY);
    }

    private void setHighlighted(View dateView, TextView dateText, TextView monthText, TextView dayText) {
        dateView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dateViewBackGroundColor)));
        dateText.setTextColor(Color.WHITE);
        monthText.setTextColor(Color.WHITE);
        dayText.setTextColor(Color.WHITE);
    }

    private void highlightAllCategory() {
        if (allTextView != currentlySelectedCategory) {
            resetCategoryHighlights();
            setCategoryHighlighted(allTextView);
            currentlySelectedCategory = allTextView;
        }
        fetchAndDisplayTasksForCategory(allTextView);
    }

    private void toggleCategoryHighlight(TextView categoryTextView) {
        if (categoryTextView != currentlySelectedCategory) {
            resetCategoryHighlights();
            setCategoryHighlighted(categoryTextView);
            currentlySelectedCategory = categoryTextView;
            fetchAndDisplayTasksForCategory(categoryTextView);
        }
    }

    private void setCategoryHighlighted(TextView categoryTextView) {
        categoryTextView.setSelected(true);
    }

    private void resetCategoryHighlights() {
        allTextView.setSelected(false);
        todoTextView.setSelected(false);
        inProgressTextView.setSelected(false);
        completedTextView.setSelected(false);
    }

    private void fetchAndDisplayTasksForCategory(TextView categoryTextView) {
        String selectedDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(lastSelectedDateInMillis));
        long endDateTimestamp = lastSelectedDateInMillis;

        List<Task> tasks = new ArrayList<>();
        String category = "All";  // Default
        String taskProgress = "All";

        if (categoryTextView == todoTextView) {
            category = "To Do";
            taskProgress = "Not Started";
        } else if (categoryTextView == inProgressTextView) {
            category = "In Progress";
            taskProgress = "In Progress";
        } else if (categoryTextView == completedTextView) {
            category = "Completed";
            taskProgress = "Completed";
        }

        Log.d("CategorySelection", "Category: " + category + ", TaskProgress: " + taskProgress);

        try {
            tasks = database.getTasksByDateAndCategory(userId, selectedDateString, taskProgress);
        } catch (Exception e) {
            Log.e("FetchTasksError", "Error fetching tasks for category: " + category + ", progress: " + taskProgress, e);
        }

        if (tasks.isEmpty()) {
            Log.d("TaskFetch", "No tasks found for category: " + category + ", progress: " + taskProgress + " on " + selectedDateString);
        }

        displayTasks(tasks);
    }

    private void displayTasks(List<Task> tasks) {
        taskContainer.removeAllViews();
        if (tasks.isEmpty()) {
            taskContainer.setVisibility(View.GONE);
        } else {
            taskContainer.setVisibility(View.VISIBLE);
            for (Task task : tasks) {
                View taskView = LayoutInflater.from(this).inflate(R.layout.task_item, null);

                // Set margin programmatically
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(50, 30, 50, 30); // Adjust bottom margin as needed
                taskView.setLayoutParams(params);

                // Find the correct views in the layout
                TextView taskGroup = taskView.findViewById(R.id.taskGroup); // This is for the task description
                TextView taskTitle = taskView.findViewById(R.id.taskTitle); // This is for the task title
                TextView taskEndTime = taskView.findViewById(R.id.timeToComplete);
                TextView taskProgress = taskView.findViewById(R.id.taskProgress);

                // Assign the correct data to the views based on naming conventions
                taskGroup.setText(task.getDescription());  // Set task description in the taskGroup view
                taskTitle.setText(task.getTitle());        // Set task title in the taskTitle view
                taskEndTime.setText(task.getTimeToComplete()); // Set time to complete
                taskProgress.setText(task.getTaskProgress());  // Set task progress

                taskContainer.addView(taskView);
            }
        }
    }
}
