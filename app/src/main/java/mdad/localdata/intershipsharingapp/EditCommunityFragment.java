package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditCommunityFragment extends Fragment {
    private static String url_edit_community = StaffMainActivity.ipBaseAddress + "/edit_community.php";
    private static String url_delete_community = StaffMainActivity.ipBaseAddress + "/delete_community.php";
    private static final int GALLERY_REQUEST_CODE = 1;
    private Bitmap selectedImageBitmap = null;
    private EditText inputName, inputDescription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_community, container, false);

        // Retrieve the data from the arguments passed to the fragment
        String title = getArguments().getString("title");
        String description = getArguments().getString("description");
        String communityId = getArguments().getString("communityId");

        // Set the data to the respective views
        inputName = view.findViewById(R.id.inputName);
        inputDescription = view.findViewById(R.id.inputDescription);
        ImageView uploadIcon = view.findViewById(R.id.uploadIcon);
        Button btnBrowseFile = view.findViewById(R.id.btnBrowseFile);

        inputName.setText(title);
        inputDescription.setText(description);

        // Set up the browse file button to open the gallery
        btnBrowseFile.setOnClickListener(v -> openGallery());

        // If you want to save the updated data when a save button is clicked:
        Button btnSave = view.findViewById(R.id.btnEditCommunity);
        btnSave.setOnClickListener(v -> editCommunity(communityId));

        Button btnDelete = view.findViewById(R.id.btnDeleteCommunity);
        btnDelete.setOnClickListener(v -> {
            RequestQueue queue = Volley.newRequestQueue(getContext());
            deleteCommunity(communityId, queue);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    // Opens the gallery for selecting an image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    // Handles the result of the gallery selection
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    // Get the bitmap from the selected URI
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                    ImageView uploadIcon = getView().findViewById(R.id.uploadIcon);
                    uploadIcon.setImageBitmap(selectedImageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void editCommunity(String communityId) {
        String name = inputName.getText().toString().trim();
        String description = inputDescription.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Name and description are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", getContext().MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", null);

        String photoBase64 = (selectedImageBitmap != null) ? bitmapToBase64(selectedImageBitmap) : "";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_edit_community,
                response -> {
                    if (response.trim().equalsIgnoreCase("success")) {
                        Toast.makeText(getContext(), "Community created successfully!", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack(); // Return to the previous fragment
                    } else {
                        Toast.makeText(getContext(), "Error: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error creating community: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {

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
        Volley.newRequestQueue(getContext()).add(stringRequest);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        int desiredWidth = 140;
        int desiredHeight = 78;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, true);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void deleteCommunity(String communityID, RequestQueue queue) {
        StringRequest deleteCommunityRequest = new StringRequest(Request.Method.POST, url_delete_community,
                response -> {
                    if (response.equals("Success")) {
                        navigateToCommunityListFragment();
                        Toast.makeText(getContext(), "Community deleted and navigated to list!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete community", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error while deleting community", Toast.LENGTH_LONG).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("chatId", communityID);
                return params;
            }
        };
        queue.add(deleteCommunityRequest);
    }

    private void navigateToCommunityListFragment() {
        ViewCommunityFragment fragment = new ViewCommunityFragment(); // Replace with the desired fragment

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment) // Replace with the actual container ID in your activity layout
                .addToBackStack(null)
                .commit();
    }
}
