package com.example.todolist;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName;
    private Button btnSave, btnCancel, editProfilePictureButton;
    private ImageView profileImageView;
    private Database dbHelper;

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 1;

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize the views
        editName = findViewById(R.id.edit_name);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        editProfilePictureButton = findViewById(R.id.edit_profile_button_image);
        profileImageView = findViewById(R.id.profile_image);

        // Initialize the database helper
        dbHelper = new Database(this);

        // Pre-fill the EditText with the current username
        editName.setText(getCurrentUsername());

        // Set the current profile image
        loadProfileImage();

        // Save button logic with username update functionality
        btnSave.setOnClickListener(v -> {
            String newUsername = editName.getText().toString().trim();

            // Get the current user ID from SharedPreferences
            int currentUserId = getCurrentUserId();

            if (currentUserId == -1) {
                Log.d("EditProfileActivity", "Invalid userId: " + currentUserId);
                Toast.makeText(EditProfileActivity.this, "Unable to update username. Please log in again.", Toast.LENGTH_SHORT).show();
                redirectToLogin();
                return;
            }

            if (!newUsername.isEmpty()) {
                saveUsername(newUsername);
                boolean updateSuccess = dbHelper.updateUsername(currentUserId, newUsername);

                if (updateSuccess) {
                    Toast.makeText(EditProfileActivity.this, "Username updated successfully!", Toast.LENGTH_SHORT).show();
                    Log.d("EditProfileActivity", "Username updated successfully for userId: " + currentUserId);
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to update username.", Toast.LENGTH_SHORT).show();
                    Log.d("EditProfileActivity", "Failed to update username for userId: " + currentUserId);
                }
            } else {
                Toast.makeText(EditProfileActivity.this, "Please enter a valid username.", Toast.LENGTH_SHORT).show();
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedUsername", newUsername);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // Cancel button logic
        btnCancel.setOnClickListener(v -> finish());

        // Initialize gallery launcher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    loadImage(selectedImageUri);
                }
            }
        });

        // Edit profile picture button logic
        editProfilePictureButton.setOnClickListener(v -> requestStoragePermission());
    }

    // Method to save the updated username in SharedPreferences
    private void saveUsername(String newUsername) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USERNAME", newUsername);
        editor.apply();
    }

    // Method to retrieve the current user's ID from SharedPreferences
    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getInt("USER_ID", -1);
    }

    // Method to retrieve the current username from SharedPreferences
    private String getCurrentUsername() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getString("USERNAME", "");
    }

    // Method to load the profile image from SharedPreferences
    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String imageUriString = prefs.getString("PROFILE_IMAGE_URI", "");
        if (!imageUriString.isEmpty()) {
            Uri imageUri = Uri.parse(imageUriString);
            loadImage(imageUri);
        } else {
            profileImageView.setImageResource(R.drawable.base_profile);
        }
    }

    private void loadImage(Uri imageUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            loadImageWithImageDecoder(imageUri);
        } else {
            loadImageWithBitmapFactory(imageUri);
        }
    }

    private void loadImageWithImageDecoder(Uri imageUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                Bitmap bitmap = ImageDecoder.decodeBitmap(source);

                if (bitmap != null) {
                    profileImageView.setImageBitmap(bitmap);
                } else {
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadImageWithBitmapFactory(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap != null) {
                profileImageView.setImageBitmap(bitmap);
                inputStream.close();
            } else {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(EditProfileActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
