package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditCommunityActivity extends AppCompatActivity {
    private static final String url_edit_community = StaffMainActivity.ipBaseAddress + "/edit_community.php";
    private static final String url_delete_community = StaffMainActivity.ipBaseAddress + "/delete_community.php";
    private static String url_delete_photo = StaffMainActivity.ipBaseAddress + "/delete_community_picture.php";
    private static final int GALLERY_REQUEST_CODE = 1;
    private Bitmap selectedImageBitmap = null;
    private EditText inputName, inputDescription;
    private String communityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_community);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Retrieve data from intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        communityId = intent.getStringExtra("communityId");

        inputName = findViewById(R.id.inputName);
        inputDescription = findViewById(R.id.inputDescription);
        ImageView uploadIcon = findViewById(R.id.uploadIcon);
        Button btnBrowseFile = findViewById(R.id.btnBrowseFile);
        Button btnSave = findViewById(R.id.btnEditCommunity);
        Button btnDelete = findViewById(R.id.btnDeleteCommunity);

        inputName.setText(title);
        inputDescription.setText(description);

        btnBrowseFile.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> editCommunity());
        btnDelete.setOnClickListener(v -> deleteCommunity());
        Button btnRemovePic = findViewById(R.id.btnDeletePic);
        btnRemovePic.setOnClickListener(v -> deleteCommunityPicture());
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
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    ImageView uploadIcon = findViewById(R.id.uploadIcon);
                    uploadIcon.setImageBitmap(selectedImageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void editCommunity() {
        String name = inputName.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Name and description are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", null);

        String photoBase64 = (selectedImageBitmap != null) ? bitmapToBase64(selectedImageBitmap) : "";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_edit_community,
                response -> {
                    if (response.trim().equalsIgnoreCase("success")) {
                        Toast.makeText(this, "Community updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error updating community: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("chatId", communityId);
                params.put("name", name);
                params.put("description", description);
                params.put("photo", photoBase64);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }
    private void deleteCommunityPicture() {

        String photoBase64 = drawableToBase64(R.drawable.no_image); // Replace with your actual drawable resource

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_delete_photo,
                response -> {
                    if (response.trim().equalsIgnoreCase("success")) {
                        Toast.makeText(this, "Community created successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + response, Toast.LENGTH_SHORT).show();
                        Log.e("EditCommunityFragment", "Error: " + response);
                    }
                },
                error -> Toast.makeText(this, "Error creating community: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("chatId", communityId);
                params.put("photo", photoBase64);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private String drawableToBase64(int drawableResId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableResId);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 140, 78, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    private void deleteCommunity() {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest deleteCommunityRequest = new StringRequest(Request.Method.POST, url_delete_community,
                response -> {
                    if (response.equals("Success")) {
                        Toast.makeText(this, "Community deleted!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete community", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error while deleting community", Toast.LENGTH_LONG).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("chatId", communityId);
                return params;
            }
        };
        queue.add(deleteCommunityRequest);
    }
}
