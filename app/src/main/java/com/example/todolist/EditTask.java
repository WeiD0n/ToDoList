package com.example.todolist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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
import java.util.Locale;

public class EditTask extends AppCompatActivity {
    // Declare class-level variables for UI components
    private EditText projectDetails;
    private EditText descriptionText;
    private TextView timeText;
    private TextView eDateText; // End date
    private Button editProjectButton;
    private ImageView searchProjectButton;
    private ImageView backTaskButton;
    private TextView taskProgressText; // Task progress text
    private TextView completeDateText; // Time to complete text

    private int taskId = -1; // Task ID
    private int userId = -1; // User ID

    // Date format for consistency (YYYY-MM-DD HH:mm)
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // Initialize the UI components
        projectDetails = findViewById(R.id.projectDetails);
        descriptionText = findViewById(R.id.descriptionText);
        timeText = findViewById(R.id.endDateText);
        eDateText = findViewById(R.id.eDateText);
        taskProgressText = findViewById(R.id.taskProgressText);
        editProjectButton = findViewById(R.id.editProjectButton);
        searchProjectButton = findViewById(R.id.enterButton);
        backTaskButton = findViewById(R.id.backTaskButton);
        completeDateText = findViewById(R.id.endDateText);

        // Retrieve task details and userId from the intent
        Intent intent = getIntent();
        taskId = intent.getIntExtra("TASK_ID", -1); // Get taskId from intent
        userId = intent.getIntExtra("USER_ID", -1); // Get userId from intent

        // Debugging log to ensure userId is being retrieved correctly
        Log.d("EditTask", "Received userId from Intent: " + userId);

        // Fallback mechanism if userId is invalid
        if (userId == -1) {
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            userId = sharedPreferences.getInt("USER_ID", -1); // Retrieve userId from SharedPreferences
            Log.d("EditTask", "Fallback: userId retrieved from SharedPreferences: " + userId);
        }

        // Check if userId is still invalid
        if (userId == -1) {
            Log.e("EditTask", "Error: userId is invalid even after fallback!");
            Toast.makeText(this, "User ID is invalid", Toast.LENGTH_SHORT).show();
            finish(); // Optionally finish the activity if userId is invalid
            return;
        }

        // Set the fields with the retrieved task data
        projectDetails.setText(intent.getStringExtra("TASK_DETAILS"));
        descriptionText.setText(intent.getStringExtra("TASK_DESCRIPTION"));
        eDateText.setText(intent.getStringExtra("END_DATE"));
        taskProgressText.setText(intent.getStringExtra("TASK_PROGRESS"));
        timeText.setText(intent.getStringExtra("TIME_TO_COMPLETE"));

        // Set up buttons
        backTaskButton.setOnClickListener(v -> finish());
        searchProjectButton.setOnClickListener(v -> searchProject(projectDetails.getText().toString()));
        editProjectButton.setOnClickListener(v -> updateTask());

        // Date and time picker handlers
        findViewById(R.id.datePickerArrow).setOnClickListener(v -> showDatePicker(eDateText));
        findViewById(R.id.dropdownArrow1).setOnClickListener(v -> showTimePicker());
        findViewById(R.id.dropdownArrow2).setOnClickListener(v -> showTaskProgressDropdown(v));
    }

    private void updateTask() {
        // Retrieve updated details from the fields
        Log.d("EditTask", "Trying to update taskId: " + taskId);  // Log the taskId here
        String updatedDetails = projectDetails.getText().toString();
        String updatedDescription = descriptionText.getText().toString();
        String updatedTimeToComplete = timeText.getText().toString();
        String updatedTaskProgress = taskProgressText.getText().toString();

        String endDate = eDateText.getText().toString().trim(); // Get end date

        // Validate inputs
        if (updatedDetails.isEmpty() || updatedDescription.isEmpty() || updatedTaskProgress.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (updatedTimeToComplete.isEmpty()) {
            Toast.makeText(this, "Please enter the time to complete", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endDate.isEmpty()) {
            Toast.makeText(this, "End date is required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            dateFormat.parse(endDate);  // Validate date format
        } catch (Exception e) {
            Toast.makeText(this, "Invalid end date format. Please use yyyy-MM-dd HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure taskId and userId are valid
        if (taskId == -1 || userId == -1) {
            Toast.makeText(this, "Error: Invalid task or user ID", Toast.LENGTH_SHORT).show();
            return;
        }


        // Update the task in the database
        Database database = new Database(this);
        boolean success = database.updateTask(taskId, updatedDetails, updatedDescription, endDate, updatedTimeToComplete, updatedTaskProgress, userId);

        if (success) {
            Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
            finish();  // Close activity on success
        } else {
            Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
        }
    }


    private void showDatePicker(TextView dateTextView) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format date in yyyy-MM-dd
                    String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    // Add the default time "00:00" for consistency
                    String formattedDate = selectedDate + " 00:00";
                    dateTextView.setText(formattedDate);  // Set formatted date to TextView
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    // Format selected time as HH:mm
                    String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    timeText.setText(formattedTime);

                    // Make sure end date is updated correctly with the selected time
                    updateEndDate(selectedHour, selectedMinute);
                }, hour, minute, true); // 24-hour format
        timePickerDialog.show();
    }

    private void updateEndDate(int selectedHour, int selectedMinute) {
        if (eDateText.getText().toString().isEmpty()) return;

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

        eDateText.setText(endDateTimeString); // Set the formatted end date
    }

    private void showTaskProgressDropdown(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_layout_edit, null);
        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);

        TextView progressOption1 = popupView.findViewById(R.id.optionProgress1);
        TextView progressOption2 = popupView.findViewById(R.id.optionProgress2);
        TextView progressOption3 = popupView.findViewById(R.id.optionProgress3);

        progressOption1.setOnClickListener(v -> {
            taskProgressText.setText("Not Started");
            popupWindow.dismiss();
        });

        progressOption2.setOnClickListener(v -> {
            taskProgressText.setText("In Progress");
            popupWindow.dismiss();
        });

        progressOption3.setOnClickListener(v -> {
            taskProgressText.setText("Completed");
            popupWindow.dismiss();
        });

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(anchorView, 0, 0);
    }

    private void searchProject(String projectName) {
        Database database = new Database(this);
        Task task = database.getTaskByProjectNameAndUserId(projectName, userId);  // Pass the userId as well

        if (task != null) {
            // Populate fields with task data
            projectDetails.setText(task.getTitle());
            descriptionText.setText(task.getDescription());
            eDateText.setText(task.getEndDate());
            taskProgressText.setText(task.getTaskProgress());
            timeText.setText(task.getTimeToComplete());

            // Save the taskId so it can be used later when updating the task
            taskId = task.getId();  // Assuming task.getId() exists and returns the task ID
            Log.d("SearchProject", "Task ID retrieved: " + taskId);

        } else {
            Toast.makeText(this, "Project not found for this user", Toast.LENGTH_SHORT).show();
            clearFields();
        }
    }


    private void clearFields() {
        projectDetails.setText("");
        descriptionText.setText("");
        eDateText.setText("");
        taskProgressText.setText("");
        timeText.setText("");
    }
}
