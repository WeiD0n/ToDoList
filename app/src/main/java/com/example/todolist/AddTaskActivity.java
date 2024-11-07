package com.example.todolist;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private TextView taskText;
    private TextView dateText; // Start date
    private TextView eDateText; // End date
    private ImageView dropdownArrow;
    private ImageView dropdownArrow1; // End date dropdown
    private ImageView taskGroupIcon;
    private EditText projectDetailsText; // Project name
    private EditText projectDescriptionText; // Project description
    private String startDateString; // For storing the start date
    private EditText timeText; // Time input

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize views
        initializeViews();

        // Set click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        taskText = findViewById(R.id.taskText);
        dateText = findViewById(R.id.dateText);
        eDateText = findViewById(R.id.eDateText);
        dropdownArrow = findViewById(R.id.dropdownArrow0);
        dropdownArrow1 = findViewById(R.id.dropdownArrow1);
        taskGroupIcon = findViewById(R.id.task_Group_icon);
        projectDetailsText = findViewById(R.id.projectDetails);
        projectDescriptionText = findViewById(R.id.descriptionText);
        timeText = findViewById(R.id.timeText);
    }

    private void setupClickListeners() {
        Button addButton = findViewById(R.id.addProjectButton);
        ImageView backTaskButton = findViewById(R.id.backTaskButton);
        ImageView datePickerArrow = findViewById(R.id.datePickerArrow);

        backTaskButton.setOnClickListener(v -> onBackPressed());
        addButton.setOnClickListener(v -> addProjectToDatabase());
        datePickerArrow.setOnClickListener(v -> showDatePicker(dateText));
        dropdownArrow1.setOnClickListener(v -> showDatePicker(eDateText));
        dropdownArrow.setOnClickListener(v -> showTaskDropdown(v));

        findViewById(R.id.dropdownArrow2).setOnClickListener(v -> showTimePicker());
    }

    private void showTaskDropdown(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_layout, null);
        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);

        setupDropdownOptions(popupView, popupWindow);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(anchorView, 0, 0);
    }

    private void setupDropdownOptions(View popupView, PopupWindow popupWindow) {
        TextView workOption = popupView.findViewById(R.id.optionWork);
        TextView schoolOption = popupView.findViewById(R.id.optionSchool);
        TextView choresOption = popupView.findViewById(R.id.optionChores);

        workOption.setOnClickListener(v -> {
            taskText.setText("Work");
            taskGroupIcon.setImageResource(R.drawable.work);
            popupWindow.dismiss();
        });

        schoolOption.setOnClickListener(v -> {
            taskText.setText("School");
            taskGroupIcon.setImageResource(R.drawable.school);
            popupWindow.dismiss();
        });

        choresOption.setOnClickListener(v -> {
            taskText.setText("Chores");
            taskGroupIcon.setImageResource(R.drawable.chores);
            popupWindow.dismiss();
        });
    }

    private void showDatePicker(TextView dateTextView) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Store date as YYYY-MM-DD format
                    String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    dateTextView.setText(selectedDate);

                    // If it's the start date, store it for comparison
                    if (dateTextView == dateText) {
                        startDateString = selectedDate ; // Add default time (00:00) for consistency
                    } else if (dateTextView == eDateText) {
                        validateEndDate(selectedDate ); // Same for end date
                    }
                }, year, month, day);
        datePickerDialog.show();
    }


    private void validateEndDate(String selectedDate) {
        if (startDateString != null && selectedDate.compareTo(startDateString) < 0) {
            Toast.makeText(this, "End date cannot be earlier than start date", Toast.LENGTH_SHORT).show();
            eDateText.setText(""); // Clear end date if invalid
        }
    }

    private int getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return sharedPreferences.getInt("USER_ID", 0);
    }

    private void addProjectToDatabase() {
        String projectName = projectDetailsText.getText().toString().trim();
        String projectDescription = projectDescriptionText.getText().toString();
        String startDate = startDateString; // This now includes time (YYYY-MM-DD HH:mm)
        String endDate = eDateText.getText().toString(); // This also includes time (YYYY-MM-DD HH:mm)
        int userId = getUserId();
        String taskGroup = taskText.getText().toString(); // This sets the task group
        String completionTime = timeText.getText().toString().trim();  // Get the completion time
        Log.d("AddTaskActivity", "Start Date: " + startDate);
        Log.d("AddTaskActivity", "End Date: " + endDate);

        // Ensure the taskName is not empty
        if (taskGroup.isEmpty()) {
            taskGroup = "Default"; // Set a default value if task group is not selected
        }

        // Validate all input fields
        if (isInputValid(projectName, projectDescription, startDate, endDate, taskGroup, completionTime)) {
            Database dbHelper = new Database(this);

            boolean isInserted = dbHelper.addTask(projectName, projectDescription, startDate, endDate, userId, taskGroup, false, completionTime);

            handleDatabaseInsertion(isInserted);
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }




    private boolean isInputValid(String projectName, String projectDescription, String startDate, String endDate, String taskGroup, String completionTime) {
        if (taskText.getText().toString().isEmpty() || taskText.getText().toString().equals("Select Task")) {
            Toast.makeText(this, "Please select a task type.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (projectName.isEmpty()) {
            Toast.makeText(this, "Please enter a project name.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (projectDescription.isEmpty()) {
            Toast.makeText(this, "Please enter project description.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (startDate == null || endDate.isEmpty()) {
            Toast.makeText(this, "Please select start and end dates.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (completionTime.isEmpty()) {
            Toast.makeText(this, "Please enter the completion time.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void handleDatabaseInsertion(boolean isInserted) {
        if (isInserted) {
            Toast.makeText(this, "Project added successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error adding project!", Toast.LENGTH_SHORT).show();
            Log.e("AddProjectActivity", "Failed to insert project");
        }
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(AddTaskActivity.this,
                (view, selectedHour, selectedMinute) -> {
                    // Format the time as HH:mm
                    String formattedTime = String.format("%02d:%02d",
                            selectedHour, selectedMinute);
                    timeText.setText(formattedTime);

                    updateEndDate(selectedHour, selectedMinute);
                }, hour, minute, true); // Use 24-hour format

        timePickerDialog.show();
    }


    private void updateEndDate(int selectedHour, int selectedMinute) {
        if (startDateString != null && !timeText.getText().toString().isEmpty()) {
            String[] dateParts = eDateText.getText().toString().split(" "); // Split date and time
            if (dateParts.length < 1) return;

            String[] date = dateParts[0].split("-");
            if (date.length < 3) return;

            int day = Integer.parseInt(date[2]);
            int month = Integer.parseInt(date[1]) - 1; // Month is zero-based
            int year = Integer.parseInt(date[0]);

            // Set the calendar to the selected date and time
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            calendar.set(Calendar.MINUTE, selectedMinute);

            // Format end date as YYYY-MM-DD HH:mm
            String endDateTimeString = String.format("%04d-%02d-%02d %02d:%02d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    selectedHour,
                    selectedMinute);

            eDateText.setText(endDateTimeString);
        }
    }
}
