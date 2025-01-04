package mdad.localdata.intershipsharingapp;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfilePictureFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImageView; // ImageView to show the selected image
    private static String url_profile_pic = StaffMainActivity.ipBaseAddress + "/profile_picture.php";

    public UpdateProfilePictureFragment() {
        // Required empty public constructor
    }

    public static UpdateProfilePictureFragment newInstance(String param1, String param2) {
        UpdateProfilePictureFragment fragment = new UpdateProfilePictureFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_update_profile_picture, container, false);

        profileImageView = rootView.findViewById(R.id.profileImageView);
        Button selectImageButton = rootView.findViewById(R.id.selectImageButton);
        Button previewButton = rootView.findViewById(R.id.previewImageButton);
        Button updateButton = rootView.findViewById(R.id.updateButton);

        selectImageButton.setOnClickListener(v -> openGallery());
        previewButton.setOnClickListener(v -> showPreviewDialog());
        updateButton.setOnClickListener(v -> uploadImage());

        return rootView;
    }

    private void showPreviewDialog() {
        if (profileImageView.getDrawable() == null) {
            return;
        }

        profileImageView.setDrawingCacheEnabled(true);
        Bitmap sourceBitmap = Bitmap.createBitmap(profileImageView.getDrawingCache());
        profileImageView.setDrawingCacheEnabled(false);

        HoleView holeView = getView().findViewById(R.id.holeView);
        Bitmap croppedBitmap = holeView.getCroppedImage(sourceBitmap);

        if (croppedBitmap == null) {
            return;
        }

        android.app.Dialog previewDialog = new android.app.Dialog(getActivity());
        previewDialog.setContentView(R.layout.dialog_preview);
        previewDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView previewImageView = previewDialog.findViewById(R.id.previewImageView);
        previewImageView.setImageBitmap(croppedBitmap);

        previewDialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);

            HoleView holeView = getView().findViewById(R.id.holeView);
            holeView.setVisibility(View.VISIBLE);
        }
    }

    private void uploadImage() {
        if (profileImageView.getDrawable() == null) {
            return;
        }

        // Enable drawing cache and create a bitmap from the ImageView
        profileImageView.setDrawingCacheEnabled(true);
        Bitmap sourceBitmap = Bitmap.createBitmap(profileImageView.getDrawingCache());
        profileImageView.setDrawingCacheEnabled(false);

        // Get the cropped image from the HoleView
        HoleView holeView = getView().findViewById(R.id.holeView);
        Bitmap croppedBitmap = holeView.getCroppedImage(sourceBitmap);

        if (croppedBitmap == null) {
            return;
        }

        // Create a new Bitmap with a white background
        Bitmap whiteBackgroundBitmap = Bitmap.createBitmap(croppedBitmap.getWidth(), croppedBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(whiteBackgroundBitmap);
        canvas.drawColor(Color.WHITE);  // Set the background color to white

        // Draw the cropped image on top of the white background
        canvas.drawBitmap(croppedBitmap, 0, 0, null);

        // Resize the image if necessary (resize to 60x60 max)
        Bitmap resizedBitmap = resizeImage(whiteBackgroundBitmap, 60, 60);  // Resize to 60x60 max
        String base64String = encodeBitmapToBase64(resizedBitmap);

        Log.d("Base64", "Base64 string size: " + base64String.length());  // Log Base64 string size

        // Upload the image to the server
        uploadToServer(base64String);
    }


    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);  // PNG format with 100% quality
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
    }

    private Bitmap resizeImage(Bitmap original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width > maxWidth || height > maxHeight) {
            float aspectRatio = (float) width / (float) height;
            if (aspectRatio > 1) {
                width = maxWidth;
                height = (int) (maxWidth / aspectRatio);
            } else {
                height = maxHeight;
                width = (int) (maxHeight * aspectRatio);
            }
        }

        return Bitmap.createScaledBitmap(original, width, height, true);
    }

    private void uploadToServer(String base64Image) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        Map<String, String> params = new HashMap<>();
        params.put("userId", sharedPreferences.getString("username", ""));  // User ID
        Log.d("EditUser", "User ID: " + sharedPreferences.getString("username", ""));
        params.put("photo", base64Image);  // Send Base64-encoded image string

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_profile_pic,
                response -> {
                    Log.d("EditUser", "Server Response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.optString("status");  // Get the "status" field

                        if ("Success".equals(status)) {
                            Toast.makeText(getContext(), "User details updated successfully", Toast.LENGTH_SHORT).show();
                            showCreateSuccessfulAlert();

                        } else {
                            Toast.makeText(getContext(), "Failed to update user details", Toast.LENGTH_LONG).show();
                            showCreateFailedAlert();
                        }

                    } catch (JSONException e) {
                        Log.e("JSONError", "Error parsing response: " + e.getMessage());
                        Toast.makeText(getContext(), "Error parsing server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(getContext(), "Error updating user details", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Log.d("EditUser", "Request Params: " + params);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    public void showCreateSuccessfulAlert() {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(requireContext());
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.success);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(requireContext());
        messageTextView.setText("Updated Profile Picture Successfully!");
        messageTextView.setTypeface(null, Typeface.BOLD);
        messageTextView.setTextSize(18);
        messageTextView.setTextColor(Color.parseColor("#228B22"));
        messageTextView.setGravity(Gravity.CENTER);

        // Apply a fade-in animation to the TextView
        messageTextView.setAlpha(0f);  // Start fully transparent
        ObjectAnimator animator = ObjectAnimator.ofFloat(messageTextView, "alpha", 0f, 1f);
        animator.setDuration(1000);  // Duration of the fade-in effect (1 second)
        animator.start();

        // Create a vertical LinearLayout to hold Lottie and TextView
        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 50, 50, 50);  // Add padding
        dialogLayout.setGravity(Gravity.CENTER);
        dialogLayout.addView(lottieSuccess);
        dialogLayout.addView(messageTextView);



        // Build and show the dialog with the custom view
        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    requireActivity().onBackPressed();
                })
                .show();
    }

    public void showCreateFailedAlert() {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(requireContext());
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.denied);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(requireContext());
        messageTextView.setText("Fail to Update Profile Picture. Please try again!");
        messageTextView.setTypeface(null, Typeface.BOLD);
        messageTextView.setTextSize(18);
        messageTextView.setTextColor(Color.parseColor("#800000"));
        messageTextView.setGravity(Gravity.CENTER);

        // Apply a fade-in animation to the TextView
        messageTextView.setAlpha(0f);  // Start fully transparent
        ObjectAnimator animator = ObjectAnimator.ofFloat(messageTextView, "alpha", 0f, 1f);
        animator.setDuration(1000);  // Duration of the fade-in effect (1 second)
        animator.start();

        // Create a vertical LinearLayout to hold Lottie and TextView
        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 50, 50, 50);  // Add padding
        dialogLayout.setGravity(Gravity.CENTER);
        dialogLayout.addView(lottieSuccess);
        dialogLayout.addView(messageTextView);

        // Build and show the dialog with the custom view
        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogLayout)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
