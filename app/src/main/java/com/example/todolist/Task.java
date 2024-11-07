package com.example.todolist;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task {
    private int id;
    private String title;
    private String description;
    private String startDate; // Consider changing this to Date for better date handling
    private String endDate;   // Consider changing this to Date
    private String group;
    private boolean isCompleted;
    private String taskProgress; // Added task progress
    private String timeToComplete; // Added time to complete


    // Constructor with all fields
    public Task(int id, String title, String description, String startDate, String endDate,
                String group, boolean isCompleted, String taskProgress, String timeToComplete) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.group = group;
        this.isCompleted = isCompleted;
        this.taskProgress = taskProgress; // Initialize task progress
        this.timeToComplete = timeToComplete; // Initialize time to complete
    }

    // No-argument constructor
    public Task() {
    }

    // Additional constructor for tasks with minimal information (for example, title and progress)
    public Task(int id, String title, String taskProgress) {
        this.id = id;
        this.title = title;
        this.taskProgress = taskProgress;
        this.description = "";
        this.startDate = "";
        this.endDate = "";
        this.group = "";
        this.isCompleted = false;
        this.timeToComplete = "";
    }

    // New constructor for your use case in getTasksByDateAndCategory method
    public Task(int id, String title, String description, String timeToComplete, String taskProgress,
                long startDate, long endDate, String taskGroup, String taskCategory) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timeToComplete = timeToComplete;
        this.taskProgress = taskProgress;
        this.startDate = convertLongToDate(startDate);
        this.endDate = convertLongToDate(endDate);
        this.group = taskGroup;
        // Assuming taskCategory is the same as taskGroup, no need to store it separately
        this.isCompleted = false; // default value, change if needed
    }

    // Helper method to convert long (timestamp) to Date string
    private String convertLongToDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // Adjust format as needed
        return sdf.format(new Date(timestamp));
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getTaskProgress() {
        return taskProgress;
    }

    public void setTaskProgress(String taskProgress) {
        this.taskProgress = taskProgress;
    }

    public String getTimeToComplete() {
        return timeToComplete;
    }

    public void setTimeToComplete(String timeToComplete) {
        this.timeToComplete = timeToComplete;
    }

    // Method to get the time left to complete the task
    public long getRemainingTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Adjust format as needed
        try {
            Date end = sdf.parse(endDate);
            Date now = new Date();
            return end.getTime() - now.getTime(); // Returns time in milliseconds
        } catch (ParseException e) {
            e.printStackTrace();
            return 0; // Return 0 in case of an error
        }
    }

    // Method to get a formatted string for display
    public String getFormattedTask() {
        return title + " - " + description + " (Progress: " + taskProgress + ")";
    }

    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", group='" + group + '\'' +
                ", isCompleted=" + isCompleted +
                ", taskProgress='" + taskProgress + '\'' +
                ", timeToComplete='" + timeToComplete + '\'' +
                '}';
    }

    // Method to get the end date in milliseconds
    public long getEndDateInMillis() {
        try {
            // Define the format of the endDate string (it should match the format used in your database)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            // Parse the endDate string into a Date object
            Date date = sdf.parse(this.endDate);

            // If date parsing is successful, return the time in milliseconds
            if (date != null) {
                return date.getTime();
            } else {
                // Handle the case where the date parsing failed
                Log.e("Task", "Failed to parse endDate: " + this.endDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();  // Handle parsing error
            Log.e("Task", "Error parsing endDate: " + this.endDate);
        }

        // Return current time if parsing fails (you can adjust this return value if needed)
        return System.currentTimeMillis();
    }


}
