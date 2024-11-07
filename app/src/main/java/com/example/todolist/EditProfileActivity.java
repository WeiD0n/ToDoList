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
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName;
    private Button btnSave, editProfilePictureButton;
    private ImageView profileImageView;
    private Database dbHelper;

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize the views
        editName = findViewById(R.id.edit_name);
        btnSave = findViewById(R.id.btn_save);
        editProfilePictureButton = findViewById(R.id.edit_profile_button_image);
        profileImageView = findViewById(R.id.profile_image);

        // Initialize the database helper
        dbHelper = new Database(this);

        // Pre-fill the EditText with the current username
        editName.setText(getCurrentUsername());

        // Set the current profile image
        loadProfileImage();

        // Handle save button click
        btnSave.setOnClickListener(v -> {
            String newUsername = editName.getText().toString().trim();  // Get the new username

            // Update username in SharedPreferences and database
            if (!newUsername.isEmpty()) {
                saveUsername(newUsername);

                String currentEmail = getCurrentUserEmail(); // Get the email for the update
                dbHelper.updateUsername(currentEmail, newUsername);  // Assuming you have a method to update the username in the database
                Toast.makeText(EditProfileActivity.this, "Username updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditProfileActivity.this, "Please enter a valid username.", Toast.LENGTH_SHORT).show();
            }
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedProfileImageUri", getSavedProfileImageUri()); // Method to retrieve saved image URI
            setResult(RESULT_OK, resultIntent);
            finish(); // Close the activity and go back to HomeActivity
        });

        // Set up the image picker dialog to select an image from gallery or take a photo with the camera
        editProfilePictureButton.setOnClickListener(v -> requestStoragePermission());
    }
    private Uri getSavedProfileImageUri() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String imageUriString = sharedPreferences.getString("PROFILE_IMAGE_URI", "");
        return imageUriString.isEmpty() ? null : Uri.parse(imageUriString);
    }

    // Method to save the updated username in SharedPreferences
    private void saveUsername(String newUsername) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USERNAME", newUsername);  // Save the updated username
        editor.apply();
    }

    // Method to retrieve the current user's email from SharedPreferences
    private String getCurrentUserEmail() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("savedEmail", ""); // Fetch email using the correct key
    }

    // Method to retrieve the current username from SharedPreferences
    private String getCurrentUsername() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getString("USERNAME", ""); // Fetch username using the correct key
    }

    // Method to load the profile image from SharedPreferences
    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String imageUriString = prefs.getString("PROFILE_IMAGE_URI", "");
        if (!imageUriString.isEmpty()) {
            Uri imageUri = Uri.parse(imageUriString);

            // Persist URI permissions for accessing the image
            try {
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // Decode and load the image based on the API level
                loadImage(imageUri);
            } catch (SecurityException e) {
                Toast.makeText(this, "Permission error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            profileImageView.setImageResource(R.drawable.base_profile);  // Default image
        }
    }

    private void loadImage(Uri imageUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // API 28 or above
            loadImageWithImageDecoder(imageUri);
        } else { // For API levels below 28
            loadImageWithBitmapFactory(imageUri);
        }
    }

    private void loadImageWithImageDecoder(Uri imageUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {  // Check if API level is 28 or above
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                Bitmap bitmap = ImageDecoder.decodeBitmap(source);

                if (bitmap != null) {
                    profileImageView.setImageBitmap(bitmap);
                } else {
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "Invalid image format: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (SecurityException | IOException e) {
                Toast.makeText(this, "Permission error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle case when ImageDecoder is not available (API level < 28)
            loadImageWithBitmapFactory(imageUri);
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
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File not found: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Request storage permission
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
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


    // Open the gallery to select an image
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");  // Only show images
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    // Handle the result of the image picking or camera capture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                Uri imageUri = data.getData();

                if (imageUri != null) {
                    Log.d("ImageSelection", "Image URI: " + imageUri.toString());

                    // Copy the selected image to local storage
                    Uri localImageUri = copyImageToLocalStorage(imageUri);

                    if (localImageUri != null) {
                        setProfileImage(localImageUri);  // Set the image in the ImageView
                        saveProfileImageUri(localImageUri);  // Save the URI in SharedPreferences
                    } else {
                        Toast.makeText(this, "Error copying image to local storage", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("ImageSelection", "No image selected");
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.d("ImageSelection", "Result code not OK or data is null");
        }
    }




    // Method to set the profile image after selecting from the gallery
    private void setProfileImage(Uri imageUri) {
        try {
            // Load and display the image from internal storage
            loadImage(imageUri);
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    // Method to save the image URI to SharedPreferences
    private void saveProfileImageUri(Uri imageUri) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("PROFILE_IMAGE_URI", imageUri.toString());
        editor.apply();
    }

    private Uri copyImageToLocalStorage(Uri imageUri) {
        try {
            // Open an input stream from the original URI
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                // Create a file in the app's internal storage
                String fileName = "profile_image.png";
                File file = new File(getFilesDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.close();
                inputStream.close();

                // Return the URI of the copied file in internal storage
                return Uri.fromFile(file);
            }
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }


    // Handle runtime permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission is required to select an image", Toast.LENGTH_SHORT).show();

                // Check if the user denied the permission permanently
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showPermissionDialog();
                }
            }
        }
    }

    // Show a dialog when the permission is permanently denied
    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Storage permission is needed to select an image. Please enable it in app settings.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Open the app settings to manually enable permission
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
