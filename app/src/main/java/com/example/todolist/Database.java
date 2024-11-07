package com.example.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ToDoList.db";
    private static final int DATABASE_VERSION = 34; // Incremented version number

    // User table
    public static final String TABLE_USER = "User";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_CREATED_AT = "created_at";
    public static final String COLUMN_SALT = "salt";

    // Task table
    public static final String TABLE_TASK = "Task";
    public static final String COLUMN_TASK_ID = "task_id";
    public static final String COLUMN_TASK_GROUP = "task_group"; // New column for task group
    public static final String COLUMN_TASK_TITLE = "title";
    public static final String COLUMN_TASK_DESCRIPTION = "description";
    public static final String COLUMN_TASK_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_TASK_COMPLETED = "completed";
    public static final String COLUMN_TASK_CREATED_AT = "created_at";
    public static final String COLUMN_TASK_USER_ID = "user_id";
    public static final String COLUMN_TASK_PROGRESS = "task_progress"; // New column for task progress
    public static final String COLUMN_TIME_TO_COMPLETE = "time_to_complete";// New column for time to complete



    // Create table queries
    private static final String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " (" +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT, " +
            COLUMN_EMAIL + " TEXT UNIQUE, " +
            COLUMN_PASSWORD + " TEXT, " +
            COLUMN_SALT + " TEXT, " +
            COLUMN_USER_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    private static final String CREATE_TASK_TABLE = "CREATE TABLE " + TABLE_TASK + " (" +
            COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TASK_TITLE + " TEXT NOT NULL, " +  // Title should not be null
            COLUMN_TASK_DESCRIPTION + " TEXT, " +
            COLUMN_TASK_START_DATE + " DATETIME, " +  // Consider if you want DATETIME or DATE
            COLUMN_END_DATE + " DATETIME, " +  // Same for end_date, DATETIME vs DATE
            COLUMN_TASK_GROUP + " TEXT, " +  // Task group (category)
            COLUMN_TASK_COMPLETED + " INTEGER DEFAULT 0, " +  // Default is 0 (not completed)
            COLUMN_TASK_PROGRESS + " TEXT, " +  // Progress of the task
            COLUMN_TIME_TO_COMPLETE + " TEXT, " +  // Time to complete (e.g., "2 hours")
            COLUMN_TASK_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +  // Created timestamp
            COLUMN_TASK_USER_ID + " INTEGER, " +  // Foreign key reference to user
            "FOREIGN KEY(" + COLUMN_TASK_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_ID + ")" +
            ");";


    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_TASK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 28) {  // Check if the database version is less than 27
            // Add columns to the User table
            db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " +  " TEXT;");
            db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " + " INTEGER DEFAULT 0;");
        }
        if (oldVersion < newVersion) {
            // Optionally handle any other updates here
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
            onCreate(db);
        }
    }

    public boolean checkUserExists(int userId, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE user_id = ? AND email = ?",
                new String[]{String.valueOf(userId), email});
        boolean userExists = cursor.moveToFirst();
        cursor.close();
        return userExists;
    }


    // In your Database class, add the following method:


    // Method to add a user
    public boolean addUser(String email, String hashedPassword, String username, String salt) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean emailExists = false;

        // Check if the cursor is not null and email already exists
        if (cursor != null) {
            emailExists = cursor.getCount() > 0;
            cursor.close();
        }

        if (emailExists) {
            Log.d("Database", "Email already exists: " + email);
            return false; // Email already exists
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashedPassword);
        values.put(COLUMN_SALT, salt);
        values.put(COLUMN_USERNAME, username);

        long result = db.insert(TABLE_USER, null, values);
        db.close();
        Log.d("Database", "User added: " + username + ", Result: " + (result != -1));
        return result != -1;
    }

    public String getUsernameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_USERNAME + " FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = null;
        String username = null;

        try {
            cursor = db.rawQuery(query, new String[]{email});
            if (cursor.moveToFirst()) {
                username = cursor.getString(0); // Assuming username is the first column in your query
            } else {
                Log.w("Database", "No username found for email: " + email);
            }
            Log.d("Database", "Username retrieved for email: " + email + ", Username: " + username);
        } catch (Exception e) {
            Log.e("Database", "Error querying username: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close(); // Always close the cursor
            }
        }

        return username;
    }

    // Method to check if email is registered
    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = null;
        boolean emailExists = false;

        try {
            cursor = db.rawQuery(query, new String[]{email});
            emailExists = cursor.getCount() > 0;
            Log.d("Database", "Email checked: " + email + ", Exists: " + emailExists);
        } catch (Exception e) {
            Log.e("Database", "Error querying email: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close(); // Always close the cursor
            }
        }

        return emailExists;
    }


    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = null;
        int userId = -1;

        try {
            cursor = db.rawQuery(query, new String[]{email});
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(0);
                Log.d("Database", "User ID retrieved: " + userId + " for email: " + email);
            } else {
                Log.w("Database", "No user found for email: " + email);
            }
        } catch (Exception e) {
            Log.e("Database", "Error querying user ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close(); // Always close the cursor
            }
        }

        return userId;
    }

    // Method to get user details (hashed password and salt) by email
    public String[] getUserDetails(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_PASSWORD + ", " + COLUMN_SALT + " FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            String hashedPassword = cursor.getString(0);
            String salt = cursor.getString(1);
            cursor.close();
            Log.d("Database", "User details retrieved for email: " + email);
            return new String[]{hashedPassword, salt};
        }

        if (cursor != null) cursor.close();
        Log.d("Database", "No user found for email: " + email);
        return null;
    }

    // Method to add a project (using tasks table for projects)
    public boolean addTask(String taskName, String taskDescription, String startDate, String endDate, int userId, String taskGroup, boolean isCompleted, String completionTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("Database", "Inserting task: " + taskName + ", Start Date: " + startDate);

        // Set the default progress value as "Not Started"
        String progress = "Not Started";

        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, taskName);  // taskName should be inserted into COLUMN_TASK_TITLE
        values.put(COLUMN_TASK_DESCRIPTION, taskDescription);
        values.put(COLUMN_TASK_START_DATE, startDate);  // Ensure this column exists in your DB
        values.put(COLUMN_END_DATE, endDate);
        values.put(COLUMN_TASK_USER_ID, userId);
        values.put(COLUMN_TASK_GROUP, taskGroup);  // Corrected to insert taskGroup here
        values.put(COLUMN_TASK_COMPLETED, isCompleted ? 1 : 0);  // Store as 1 for true and 0 for false
        values.put(COLUMN_TIME_TO_COMPLETE, completionTime);
        values.put(COLUMN_TASK_PROGRESS, progress);  // Default progress is "Not Started"

        // Insert the task into the database
        long result = db.insert(TABLE_TASK, null, values);

        // Return true if the insert was successful, false otherwise
        return result != -1;
    }
    public List<Task> getPendingTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch tasks where the end date is greater than or equal to the current datetime
        String query = "SELECT * FROM " + TABLE_TASK + " WHERE " + COLUMN_END_DATE + " >= datetime('now')";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task();

                // Get the column indices
                int taskIdIndex = cursor.getColumnIndex(COLUMN_TASK_ID);
                int titleIndex = cursor.getColumnIndex(COLUMN_TASK_TITLE);
                int endDateIndex = cursor.getColumnIndex(COLUMN_END_DATE);

                // Ensure the column indices are valid
                if (taskIdIndex != -1 && titleIndex != -1 && endDateIndex != -1) {
                    task.setId(cursor.getInt(taskIdIndex));   // Get the task ID
                    task.setTitle(cursor.getString(titleIndex));  // Get the task title
                    task.setEndDate(cursor.getString(endDateIndex));  // Get the end date (string format)
                    tasks.add(task);
                } else {
                    // Log an error if any of the expected columns are missing
                    Log.e("Database", "One or more columns not found in the cursor.");
                }
            } while (cursor.moveToNext());  // Loop through all rows

            cursor.close();  // Always close the cursor when done
        } else {
            // If the cursor is null or no tasks are found
            Log.e("Database", "No pending tasks found.");
        }

        db.close();  // Always close the database connection when done
        return tasks;  // Return the list of tasks
    }


    private String formatDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            Log.e("DateFormat", "Empty or null date string provided.");
            return null;  // Early exit if date is null or empty
        }

        try {
            // Define the expected input and output formats
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

            // Set the timezone to Malaysia (Asia/Kuala_Lumpur)
            TimeZone malaysiaTimeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur");
            inputFormat.setTimeZone(malaysiaTimeZone);
            outputFormat.setTimeZone(malaysiaTimeZone);

            // Try parsing the input date string
            Date parsedDate = inputFormat.parse(date);
            if (parsedDate != null) {
                // Return the formatted date in Malaysia timezone
                return outputFormat.format(parsedDate);
            } else {
                // Log the error if parsing fails
                Log.e("DateFormat", "Failed to parse date: " + date);
                return null;
            }
        } catch (ParseException e) {
            // Log the exception if parsing fails
            Log.e("DateFormat", "Error formatting date: " + e.getMessage() + " for input: " + date);
            return null;  // Return null if the date cannot be parsed
        }
    }




    public List<Task> getNotStartedTasks(String userId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch tasks that are "Not Started" or "To Do"
        String selection = COLUMN_TASK_USER_ID + " = ? AND " + COLUMN_TASK_PROGRESS + " = ?";
        String[] selectionArgs = {userId, "To Do"};  // Change to "Not Started" if that's the actual value

        Cursor cursor = db.query(TABLE_TASK, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = cursorToTask(cursor);
                tasks.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return tasks;
    }

    public List<Task> getInProgressTasks(String userId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch tasks that are "In Progress"
        String selection = COLUMN_TASK_USER_ID + " = ? AND " + COLUMN_TASK_PROGRESS + " = ?";
        String[] selectionArgs = {userId, "In Progress"};

        Cursor cursor = db.query(TABLE_TASK, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = cursorToTask(cursor);
                tasks.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return tasks;
    }



    public List<Task> getCompletedTasks(String userId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch completed tasks
        String selection = COLUMN_TASK_USER_ID + " = ? AND " + COLUMN_TASK_PROGRESS + " = ?";
        String[] selectionArgs = {userId, "Completed"};

        Cursor cursor = db.query(TABLE_TASK, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = cursorToTask(cursor);
                tasks.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return tasks;
    }
    private Task cursorToTask(Cursor cursor) {
        int id = -1;
        String title = null;
        String description = null;
        String startDate = null;
        String endDate = null;
        String group = null;
        boolean isCompleted = false;
        String taskProgress = null;
        String timeToComplete = null;

        int idIndex = cursor.getColumnIndex(COLUMN_TASK_ID);
        int titleIndex = cursor.getColumnIndex(COLUMN_TASK_TITLE);
        int descriptionIndex = cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION);
        int startDateIndex = cursor.getColumnIndex(COLUMN_TASK_START_DATE);
        int endDateIndex = cursor.getColumnIndex(COLUMN_END_DATE);
        int groupIndex = cursor.getColumnIndex(COLUMN_TASK_GROUP);
        int completedIndex = cursor.getColumnIndex(COLUMN_TASK_COMPLETED);
        int taskProgressIndex = cursor.getColumnIndex(COLUMN_TASK_PROGRESS);
        int timeToCompleteIndex = cursor.getColumnIndex(COLUMN_TIME_TO_COMPLETE);

        if (idIndex != -1) id = cursor.getInt(idIndex);
        if (titleIndex != -1) title = cursor.getString(titleIndex);
        if (descriptionIndex != -1) description = cursor.getString(descriptionIndex);
        if (startDateIndex != -1) startDate = cursor.getString(startDateIndex);
        if (endDateIndex != -1) endDate = cursor.getString(endDateIndex);
        if (groupIndex != -1) group = cursor.getString(groupIndex);
        if (completedIndex != -1) isCompleted = cursor.getInt(completedIndex) > 0;
        if (taskProgressIndex != -1) taskProgress = cursor.getString(taskProgressIndex);
        if (timeToCompleteIndex != -1) timeToComplete = cursor.getString(timeToCompleteIndex);

        return new Task(id, title, description, startDate, endDate, group, isCompleted, taskProgress, timeToComplete);
    }




    public Task getTaskByProjectNameAndUserId(String projectName, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM Task WHERE title = ? AND user_id = ?",
                new String[] { projectName, String.valueOf(userId) });

        if (cursor != null && cursor.moveToFirst()) {
            // Check and log column names
            String[] columnNames = cursor.getColumnNames();
            for (String column : columnNames) {
                Log.d("CursorColumns", column);
            }

            Task task = new Task();

            // Ensure column indices are valid before getting data
            int columnIndex = cursor.getColumnIndex(COLUMN_TASK_ID);
            if (columnIndex != -1) {
                task.setId(cursor.getInt(columnIndex));
            } else {
                Log.e("Database", "COLUMN_TASK_ID not found");
            }

            int columnIndexTitle = cursor.getColumnIndex(COLUMN_TASK_TITLE);
            if (columnIndexTitle != -1) {
                task.setTitle(cursor.getString(columnIndexTitle));
            } else {
                Log.e("Database", "COLUMN_TASK_TITLE not found");
            }

            int columnIndexDescription = cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION);
            if (columnIndexDescription != -1) {
                task.setDescription(cursor.getString(columnIndexDescription));
            } else {
                Log.e("Database", "COLUMN_TASK_DESCRIPTION not found");
            }

            int columnIndexEndDate = cursor.getColumnIndex(COLUMN_END_DATE);
            if (columnIndexEndDate != -1) {
                task.setEndDate(cursor.getString(columnIndexEndDate));
            } else {
                Log.e("Database", "COLUMN_END_DATE not found");
            }

            int columnIndexTimeToComplete = cursor.getColumnIndex(COLUMN_TIME_TO_COMPLETE);
            if (columnIndexTimeToComplete != -1) {
                task.setTimeToComplete(cursor.getString(columnIndexTimeToComplete));
            } else {
                Log.e("Database", "COLUMN_TIME_TO_COMPLETE not found");
            }

            int columnIndexProgress = cursor.getColumnIndex(COLUMN_TASK_PROGRESS);
            if (columnIndexProgress != -1) {
                task.setTaskProgress(cursor.getString(columnIndexProgress));
            } else {
                Log.e("Database", "COLUMN_TASK_PROGRESS not found");
            }

            cursor.close();
            return task;
        } else {
            cursor.close();
            return null;  // No task found for this projectName and userId
        }
    }


    public boolean taskExists(int taskId, int userId) {
        // Query to check if the task exists for the given taskId and userId
        String query = "SELECT COUNT(*) FROM Task WHERE task_id = ? AND user_id = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(taskId), String.valueOf(userId)});

        boolean exists = false;
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                exists = true;
            }
        }
        cursor.close();
        return exists;
    }

    public boolean updateTask(int taskId, String title, String description, String endDate, String timeToComplete, String taskProgress, int userId) {
        if (taskId == -1 || userId == -1) {
            Log.e("UpdateTask", "Invalid taskId or userId: taskId = " + taskId + ", userId = " + userId);
            return false;  // Exit early if either ID is invalid
        }

        // Ensure endDate is in the correct format (yyyy-MM-dd HH:mm)
        String formattedEndDate = formatDate(endDate);
        if (formattedEndDate == null) {
            Log.e("UpdateTask", "Invalid endDate format: " + endDate);
            return false;  // Exit early if the date format is invalid
        }

        // SQL query to check if the task exists and belongs to the logged-in user
        SQLiteDatabase db = this.getWritableDatabase();
        String checkQuery = "SELECT * FROM " + TABLE_TASK + " WHERE task_id = ? AND user_id = ?";
        Cursor cursor = db.rawQuery(checkQuery, new String[]{String.valueOf(taskId), String.valueOf(userId)});

        if (cursor.getCount() == 0) {
            Log.e("UpdateTask", "No task found with taskId: " + taskId + " and userId: " + userId);
            cursor.close();
            return false;  // Task not found for this user
        }
        cursor.close();  // Close cursor after use

        // If task exists, proceed to update the task
        String updateQuery = "UPDATE " + TABLE_TASK + " SET title = ?, description = ?, end_date = ?, task_progress = ?, time_to_complete = ? WHERE task_id = ? AND user_id = ?";
        SQLiteStatement statement = db.compileStatement(updateQuery);
        statement.bindString(1, title);
        statement.bindString(2, description);
        statement.bindString(3, formattedEndDate);
        statement.bindString(4, taskProgress);
        statement.bindString(5, timeToComplete);
        statement.bindLong(6, taskId);
        statement.bindLong(7, userId);

        // Execute the update query
        int rowsUpdated = statement.executeUpdateDelete();

        if (rowsUpdated > 0) {
            Log.d("UpdateTask", "Task updated successfully. Rows affected: " + rowsUpdated);
            return true;
        } else {
            Log.e("UpdateTask", "No rows updated for taskId: " + taskId + " and userId: " + userId);
            return false;  // No rows were updated
        }
    }




    private String formatEndDate(String endDate) {
        try {
            // Check if the endDate contains time. If not, we will append the default time "00:00:00"
            if (endDate.length() == 10) {
                // If the format is yyyy-MM-dd (date only), add default time 00:00:00
                endDate += " 00:00:00";
            }

            // Try to parse the endDate using the format yyyy-MM-dd HH:mm:ss
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            // Parse the input date
            Date parsedDate = inputFormat.parse(endDate);

            if (parsedDate != null) {
                // Return the formatted date in the correct format (yyyy-MM-dd HH:mm:ss)
                return outputFormat.format(parsedDate);
            } else {
                // If parsing failed, return null
                return null;
            }
        } catch (ParseException e) {
            Log.e("UpdateTask", "Error formatting endDate: " + e.getMessage());
            return null;  // Return null if parsing or formatting failed
        }
    }





    public List<Task> getTasksByStatus(int userId, String dateString, String status) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch tasks based on user ID, start date, and task progress status
        String query = "SELECT * FROM " + TABLE_TASK +
                " WHERE " + COLUMN_TASK_USER_ID + " = ? AND " +
                COLUMN_TASK_START_DATE + " LIKE ? AND " +  // Use LIKE to match date part only
                COLUMN_TASK_PROGRESS + " = ?";

        // Adjust date format to match only the date part
        String datePattern = dateString + "%"; // Match the start_date that begins with the provided date

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), datePattern, status});

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COLUMN_TASK_ID);
            int titleIndex = cursor.getColumnIndex(COLUMN_TASK_TITLE);
            int descriptionIndex = cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION);
            int startDateIndex = cursor.getColumnIndex(COLUMN_TASK_START_DATE);
            int endDateIndex = cursor.getColumnIndex(COLUMN_END_DATE);
            int groupIndex = cursor.getColumnIndex(COLUMN_TASK_GROUP);
            int completedIndex = cursor.getColumnIndex(COLUMN_TASK_COMPLETED);
            int progressIndex = cursor.getColumnIndex(COLUMN_TASK_PROGRESS);
            int timeToCompleteIndex = cursor.getColumnIndex(COLUMN_TIME_TO_COMPLETE);

            // Check if any column indices are invalid
            if (idIndex == -1 || titleIndex == -1 || startDateIndex == -1 || progressIndex == -1) {
                Log.e("Database", "One or more column names are invalid");
                cursor.close();
                return tasks; // Return empty list if there are errors
            }

            do {
                int id = cursor.getInt(idIndex);
                String title = cursor.getString(titleIndex);
                String description = cursor.getString(descriptionIndex); // Fetch the description
                String startDate = cursor.getString(startDateIndex);
                String endDate = cursor.getString(endDateIndex);
                String group = cursor.getString(groupIndex); // Fetch the task group
                boolean isCompleted = cursor.getInt(completedIndex) > 0; // Convert to boolean
                String taskProgress = cursor.getString(progressIndex);
                String timeToComplete = cursor.getString(timeToCompleteIndex); // Fetch time to complete

                // Create the Task object with all fields
                Task task = new Task(id, title, description, startDate, endDate, group, isCompleted, taskProgress, timeToComplete);
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return tasks;
    }


    public List<Task> getTasksByDateAndCategory(int userId, String date, String category, String taskProgress) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Task> tasks = new ArrayList<>();
        Cursor cursor = null;

        String formattedDate = date;  // Assuming the date is already formatted
        String query = "SELECT * FROM Task WHERE user_id = ? AND DATE(end_date) = DATE(?)";
        List<String> params = new ArrayList<>();
        params.add(String.valueOf(userId));
        params.add(formattedDate);

        if (!"All".equals(taskProgress)) {
            query += " AND task_progress = ?";
            params.add(taskProgress);
        }

        Log.d("SQLQuery", "Executing: " + query);
        Log.d("SQLParams", "Params: " + params);

        try {
            cursor = db.rawQuery(query, params.toArray(new String[0]));

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = getColumnValue(cursor, COLUMN_TASK_ID, 0);
                    String title = getColumnValue(cursor, COLUMN_TASK_TITLE, "");
                    String description = getColumnValue(cursor, COLUMN_TASK_DESCRIPTION, "");
                    String timeToComplete = getColumnValue(cursor, COLUMN_TIME_TO_COMPLETE, "");
                    String taskProgressValue = getColumnValue(cursor, COLUMN_TASK_PROGRESS, "");
                    long startDate = getColumnValue(cursor, COLUMN_TASK_START_DATE, 0L);
                    long endDate = getColumnValue(cursor, COLUMN_END_DATE, 0L);
                    String taskGroup = getColumnValue(cursor, COLUMN_TASK_GROUP, "");

                    Task task = new Task(id, title, description, timeToComplete, taskProgressValue,
                            startDate, endDate, taskGroup, taskGroup);
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error querying tasks", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tasks;
    }

    /**
     * Helper method to get a column value of type int with a default value if the column is not found.
     */
    private int getColumnValue(Cursor cursor, String columnName, int defaultValue) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return columnIndex >= 0 ? cursor.getInt(columnIndex) : defaultValue;
    }

    /**
     * Helper method to get a column value of type String with a default value if the column is not found.
     */
    private String getColumnValue(Cursor cursor, String columnName, String defaultValue) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return columnIndex >= 0 ? cursor.getString(columnIndex) : defaultValue;
    }

    /**
     * Helper method to get a column value of type long with a default value if the column is not found.
     */
    private long getColumnValue(Cursor cursor, String columnName, long defaultValue) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return columnIndex >= 0 ? cursor.getLong(columnIndex) : defaultValue;
    }

    public List<Task> getTasksInProgress(int userId) {
        List<Task> tasksInProgress = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Modify the query to filter by both user_id and task_progress = 'In Progress'
        String selectQuery = "SELECT * FROM " + TABLE_TASK + " WHERE " + COLUMN_TASK_PROGRESS + " = ? AND " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[] { "In Progress", String.valueOf(userId) });

        if (cursor != null) {
            // Debugging - Check column names
            int columnCount = cursor.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                Log.d("Database", "Column " + i + ": " + cursor.getColumnName(i));  // Check column names
            }

            if (cursor.moveToFirst()) {
                do {
                    // Log column names to ensure they are correct
                    int columnId = cursor.getColumnIndex(COLUMN_TASK_ID);
                    int columnTitle = cursor.getColumnIndex(COLUMN_TASK_TITLE);
                    int columnDescription = cursor.getColumnIndex(COLUMN_TASK_DESCRIPTION);
                    int columnStartDate = cursor.getColumnIndex(COLUMN_TASK_START_DATE);
                    int columnEndDate = cursor.getColumnIndex(COLUMN_END_DATE);
                    int columnCategory = cursor.getColumnIndex(COLUMN_TASK_GROUP);
                    int columnCompleted = cursor.getColumnIndex(COLUMN_TASK_COMPLETED);
                    int columnProgress = cursor.getColumnIndex(COLUMN_TASK_PROGRESS);
                    int columnDuration = cursor.getColumnIndex(COLUMN_TIME_TO_COMPLETE);

                    // Log the column index values
                    Log.d("Database", "COLUMN_TASK_ID index: " + columnId);
                    Log.d("Database", "COLUMN_TASK_TITLE index: " + columnTitle);
                    Log.d("Database", "COLUMN_TASK_DESCRIPTION index: " + columnDescription);
                    Log.d("Database", "COLUMN_TASK_START_DATE index: " + columnStartDate);
                    Log.d("Database", "COLUMN_END_DATE index: " + columnEndDate);
                    Log.d("Database", "COLUMN_TASK_GROUP index: " + columnCategory);
                    Log.d("Database", "COLUMN_TASK_COMPLETED index: " + columnCompleted);
                    Log.d("Database", "COLUMN_TASK_PROGRESS index: " + columnProgress);
                    Log.d("Database", "COLUMN_TIME_TO_COMPLETE index: " + columnDuration);

                    if (columnId >= 0) {
                        int id = cursor.getInt(columnId);
                        String title = cursor.getString(columnTitle);
                        String description = cursor.getString(columnDescription);
                        String startDate = cursor.getString(columnStartDate);
                        String endDate = cursor.getString(columnEndDate);
                        String category = cursor.getString(columnCategory);
                        boolean completed = cursor.getInt(columnCompleted) == 1;
                        String progress = cursor.getString(columnProgress);
                        String duration = cursor.getString(columnDuration);

                        Task task = new Task(id, title, description, startDate, endDate, category, completed, progress, duration);
                        tasksInProgress.add(task);
                    } else {
                        Log.e("Database", "One of the columns is missing or the index is invalid.");
                    }

                } while (cursor.moveToNext());
                cursor.close();
            }
        }

        db.close();
        return tasksInProgress;
    }

    public List<TaskGroup> getTaskGroupsWithCount(int userId) {
        List<TaskGroup> taskGroups = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // SQL query to get task group names and their respective counts, filtered by userId
        String selectQuery = "SELECT " + COLUMN_TASK_GROUP + ", COUNT(*) AS task_count " +
                "FROM " + TABLE_TASK +
                " WHERE " + COLUMN_TASK_USER_ID + " = ? " +  // Filter by userId
                "GROUP BY " + COLUMN_TASK_GROUP;

        // Use the userId to filter tasks for the logged-in user
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            // Get the column index for 'task_group' and 'task_count'
            int groupNameIndex = cursor.getColumnIndex(COLUMN_TASK_GROUP);
            int taskCountIndex = cursor.getColumnIndex("task_count");

            // Check if the column indices are valid
            if (groupNameIndex != -1 && taskCountIndex != -1) {
                // Iterate through the cursor to fetch all task groups for the logged-in user
                do {
                    String groupName = cursor.getString(groupNameIndex);
                    int taskCount = cursor.getInt(taskCountIndex);

                    // Create a TaskGroup object and add it to the list
                    TaskGroup taskGroup = new TaskGroup(groupName, taskCount);
                    taskGroups.add(taskGroup);
                } while (cursor.moveToNext()); // Move to the next row in the cursor
            }
        }

        cursor.close();  // Always close the cursor when you're done with it
        return taskGroups; // Return the list of task groups
    }


    public void updateUsername(String email, String newUsername) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Log to check the email and new username being passed
        Log.d("Database", "Updating username for email: " + email + " to: " + newUsername);

        // Use email to find the correct user and update their username
        ContentValues values = new ContentValues();
        values.put("username", newUsername);  // "username" is the column name in your DB

        // Make sure you're updating the row where the email matches
        String selection = "email = ?";
        String[] selectionArgs = { email };

        // Update query
        int rowsUpdated = db.update(TABLE_USER, values, selection, selectionArgs);

        if (rowsUpdated > 0) {
            Log.d("Database", "Username updated successfully for email: " + email);
        } else {
            Log.d("Database", "No rows updated for email: " + email);
        }

        db.close();
    }



}







