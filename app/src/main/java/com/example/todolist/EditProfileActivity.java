package com.example.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName;
    private Button btnSave, btnCancel;
    private ImageView profileImageView;
    private GridView profileImagesGridView;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize the views
        editName = findViewById(R.id.edit_name);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        profileImageView = findViewById(R.id.profile_image);
        profileImagesGridView = findViewById(R.id.imageGridView);

        // Initialize the database helper
        Database dbHelper = new Database(this);

        // Pre-fill the EditText with the current username
        editName.setText(getCurrentUsername());

        // Set the current profile image
        loadProfileImage();

        // Set up the GridView to display predefined images
        imageAdapter = new ImageAdapter(this, getPredefinedImages());
        profileImagesGridView.setAdapter(imageAdapter);

        // Set the item click listener for selecting a profile image
        profileImagesGridView.setOnItemClickListener((parent, view, position, id) -> {
            int selectedImageResId = (int) imageAdapter.getItem(position);  // Cast to int
            profileImageView.setImageResource(selectedImageResId);  // Set the image
            saveProfileImage(selectedImageResId);  // Save the resource ID
        });


        // Save button logic to update username and profile image
        btnSave.setOnClickListener(v -> {
            String newUsername = editName.getText().toString().trim();

            if (!newUsername.isEmpty()) {
                saveUsername(newUsername);
                boolean updateSuccess = dbHelper.updateUsername(getCurrentUserId(), newUsername);

                if (updateSuccess) {
                    Toast.makeText(EditProfileActivity.this, "Username updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to update username.", Toast.LENGTH_SHORT).show();
                }
            }

            // Return to HomeActivity
            setResult(RESULT_OK);
            finish();
        });

        // Cancel button logic
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    // Method to save the selected profile image to SharedPreferences
    private void saveProfileImage(int imageResId) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("PROFILE_IMAGE_RESOURCE", imageResId);
        editor.apply();
    }

    // Method to load the current profile image from SharedPreferences
    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int imageResId = prefs.getInt("PROFILE_IMAGE_RESOURCE", R.drawable.base_profile);
        profileImageView.setImageResource(imageResId);
    }

    // Method to save the username to SharedPreferences
    private void saveUsername(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USERNAME", username);
        editor.apply();
    }

    // Method to retrieve the current username from SharedPreferences
    private String getCurrentUsername() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getString("USERNAME", "");
    }

    // Method to retrieve the user ID (stub for this example)
    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getInt("USER_ID", -1);
    }

    // Predefined list of profile image resources
    private List<Integer> getPredefinedImages() {
        List<Integer> imageList = new ArrayList<>();
        imageList.add(R.drawable.profile_image_1);
        imageList.add(R.drawable.profile_image_2);
        imageList.add(R.drawable.profile_image_3);
        imageList.add(R.drawable.profile_image_4);
        imageList.add(R.drawable.profile_image_5);
        imageList.add(R.drawable.profile_image_6);
        imageList.add(R.drawable.profile_image_7);
        imageList.add(R.drawable.profile_image_8);
        imageList.add(R.drawable.profile_image_9);
        imageList.add(R.drawable.profile_image_10);
        imageList.add(R.drawable.profile_image_11);
        imageList.add(R.drawable.profile_image_12);
        return imageList;
    }
}
