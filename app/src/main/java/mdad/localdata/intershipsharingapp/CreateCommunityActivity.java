package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateCommunityActivity extends AppCompatActivity {
    private static String url_create_community = StaffMainActivity.ipBaseAddress + "/create_community.php";
    private static String url_create_userchat = StaffMainActivity.ipBaseAddress + "/create_userchat.php";
    private static final int GALLERY_REQUEST_CODE = 1;

    private ImageView uploadIcon;
    private EditText inputName, inputDescription;
    private Bitmap selectedImageBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_community);

        // Initialize views
        uploadIcon = findViewById(R.id.uploadIcon);
        inputName = findViewById(R.id.inputName);
        inputDescription = findViewById(R.id.inputDescription);
        Button btnBrowseFile = findViewById(R.id.btnBrowseFile);
        Button btnCreateCommunity = findViewById(R.id.btnCreateCommunity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set button click listener for gallery
        btnBrowseFile.setOnClickListener(v -> openGallery());

        // Set button click listener for creating community
        btnCreateCommunity.setOnClickListener(v -> createCommunity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the back button click
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    uploadIcon.setImageBitmap(selectedImageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void createCommunity() {
        String name = inputName.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Name and description are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", null); // Replace with the actual user ID from your app's session

        // If there's no photo, set photoBase64 to an empty string
        String photoBase64 = (selectedImageBitmap != null) ? bitmapToBase64(selectedImageBitmap) : "";

        // Create Volley request to create community
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_create_community,
                response -> {
                    try {
                        // Parse the response and extract the communityId
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            String communityId = jsonResponse.getString("communityId");

                            // After community creation, create a user chat
                            createUserChat(userId, communityId);

                            Toast.makeText(this, "Community created successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error: " + response, Toast.LENGTH_SHORT).show();
                            Log.d("Response", response);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error extracting community ID", Toast.LENGTH_SHORT).show();
                    }

                    finish(); // Close the activity after community is created
                },
                error -> Toast.makeText(this, "Error creating community: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId", userId);
                params.put("name", name);
                params.put("description", description);
                params.put("photo", photoBase64); // Can be empty if no photo
                return params;
            }
        };

        // Add the community creation request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void createUserChat(String userId, String communityId) {
        Log.d("community", communityId);
        // Create Volley request to create user chat
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_create_userchat,
                response -> {
                    if (response.trim().equalsIgnoreCase("success")) {
                        Toast.makeText(this, "User chat created successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error creating user chat: " + response, Toast.LENGTH_SHORT).show();
                        Log.d("Response", response);
                    }
                },
                error -> Toast.makeText(this, "Error creating user chat: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId", userId);
                params.put("chatId", communityId);
                return params;
            }
        };

        // Add the user chat creation request to the Volley queue
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        // Define the desired width and height for the resized image
        int desiredWidth = 140; // Adjust as needed
        int desiredHeight = 78; // Adjust as needed

        // Resize the bitmap
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, true);

        // Convert the resized bitmap to Base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

}
