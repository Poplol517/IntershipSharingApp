package mdad.localdata.intershipsharingapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewSelectedAccountQuestionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewSelectedAccountQuestionFragment extends Fragment {

    private LinearLayout lv;
    private SwipeRefreshLayout swipeRefreshLayout;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String url_delete_question = StaffMainActivity.ipBaseAddress + "/delete_question.php";
    private static final String url_question_industry = StaffMainActivity.ipBaseAddress + "/delete_question_industry.php";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ViewSelectedAccountQuestionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewSelectedAccountQuestionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewSelectedAccountQuestionFragment newInstance(String param1, String param2) {
        ViewSelectedAccountQuestionFragment fragment = new ViewSelectedAccountQuestionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_account_question, container, false);
        lv = view.findViewById(R.id.list);
        // Optionally, you can also fetch internships here if you want an initial fetch when the fragment is created
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Set up the refresh listener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Handle the refresh logic here
                fetchQuestions();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch internships every time the user enters the page (fragment)
        fetchQuestions();
    }

    private void fetchQuestions() {
        String url_all_question= StaffMainActivity.ipBaseAddress + "/get_all_question.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String currentUserId = getArguments().getString("userId");
        Log.d("CurrentUserId", "Current User ID: " + currentUserId);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_all_question,
                response -> {
                    Log.d("QuestionResponse", response);
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] questions = response.split(":");
                    Log.d("QuestionArray", Arrays.toString(questions));
                    lv.removeAllViews();  // Clear the existing views before adding new ones
                    for (String question : questions) {
                        if (!question.isEmpty()) {
                            String[] details = question.split(";");
                            if (details.length >= 8 ) {
                                String postUserId = details[4];
                                Log.d("QuestionUserId", postUserId);

                                if (currentUserId.equals(postUserId)) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("QuestionID", details[0]);
                                    map.put("title", details[1]);
                                    map.put("description", details[2]);
                                    map.put("date_shared", details[3]);
                                    map.put("UserID", details.length > 4 ? details[4] : "");
                                    map.put("user_name", details[5]);
                                    map.put("course", details.length > 6 ? details[6] : "");
                                    map.put("role", details.length > 7 ? details[7] : "");
                                    map.put("photo", details.length > 8 ? details[8] : "");
                                    Log.d("QuestionDetails", map.toString());
                                    addQuestionToLayout(map);
                                    Log.d("QuestionDetails", map.toString());
                                }
                            }
                        }
                    }
                    swipeRefreshLayout.setRefreshing(false);
                },

                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error retrieving database", Toast.LENGTH_LONG).show();
                });

        queue.add(stringRequest);
    }

    private void addQuestionToLayout(final HashMap<String, String> item) {
        // Inflate the custom layout
        View postView = LayoutInflater.from(requireContext()).inflate(R.layout.question_post_item, lv, false);

        // Set data for user info
        ImageView profile_icon = postView.findViewById(R.id.profile_icon);
        TextView userName = postView.findViewById(R.id.post_user_name);
        TextView userRole = postView.findViewById(R.id.post_user_role);
        TextView postTitle = postView.findViewById(R.id.post_title);
        TextView postContent = postView.findViewById(R.id.post_content);
        TextView postHashtags = postView.findViewById(R.id.post_hashtags);
        Button commentButton = postView.findViewById(R.id.comment_button);
        ImageView optionsMenu = postView.findViewById(R.id.options_menu);
        optionsMenu.setVisibility(View.VISIBLE);
        // Populate the fields with dynamic data
        userName.setText(item.get("user_name"));
        postTitle.setText(item.get("title"));
        userRole.setText(item.get("role") + " | " + item.get("course"));
        postContent.setText(item.get("description"));

        // Check if description contains a hashtag
        String description = item.get("description");
        if (description != null && description.contains("#")) {
            postHashtags.setVisibility(View.VISIBLE);  // Ensure it's visible
        } else {
            postHashtags.setVisibility(View.GONE);  // Hide it if no hashtags are present
        }

        String photoData = item.get("photo");
        Log.d("UserDetails", "Photo Data: " + item.get("photo"));
        if (photoData != null && !photoData.isEmpty()) {
            saveBase64ToFile(photoData, file -> {
                // Once the image is saved, decode the file to Bitmap
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    profile_icon.setImageBitmap(bitmap);
                } else {
                    Log.e("ImageError", "Failed to decode bitmap from file.");
                    profile_icon.setImageResource(R.drawable.account); // Default image
                }
            });
        } else {
            profile_icon.setImageResource(R.drawable.account); // Default image
        }

        // Set up the options menu for each post
        optionsMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), optionsMenu);
            popupMenu.inflate(R.menu.post_option_menu); // Inflate menu resource file

            // Set click listeners for menu items
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.edit_post) {
                    editPost(item);
                    return true;
                } else if (menuItem.getItemId() == R.id.delete_post) {
                    confirmDelete(item);
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show();
        });

        // Comment button click listener
        commentButton.setOnClickListener(v -> openCommentDialog(item));

        // Add the postView to the parent layout
        lv.addView(postView);
    }

    private void saveBase64ToFile(String base64Data, ViewAccountFragment.OnFileSavedListener listener) {
        try {
            byte[] decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);

            // Save the decoded bytes to a file in cache directory
            File cacheDir = requireContext().getCacheDir();
            File imageFile = new File(cacheDir, "profile_image.jpg");

            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(decodedBytes);
            fos.close();

            // Notify that the file has been saved
            listener.onFileSaved(imageFile);
        } catch (Exception e) {
            Log.e("FileSaveError", "Error saving Base64 to file: " + e.getMessage());
        }
    }

    private void confirmDelete(HashMap<String, String> item) {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(requireContext());
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.warning);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(requireContext());
        messageTextView.setText("Are you sure you want to delete this question?");
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
                .setPositiveButton("Delete", (dialog, which) -> deletePost(item.get("QuestionID")))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void successfulDelete() {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(requireContext());
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.success);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(requireContext());
        messageTextView.setText("Question deleted successfully!");
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
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void failedDelete() {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(requireContext());
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.denied);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(requireContext());
        messageTextView.setText("Failed to delete question!!! \n Please try again.");
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
    // delete from internshipindustrytag first due to the fk constraint
    private void deletePost(String questionID) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // First, delete from url_internship_industry
        StringRequest deleteIndustryRequest = new StringRequest(Request.Method.POST, url_question_industry,
                response -> {
                    Log.d("IndustryResponse", response);
                    if (response.equals("Success")) {
                        // Proceed to delete from url_delete_internship
                        deleteFromQuestion(questionID, queue);
                        successfulDelete();
                    } else {
                        failedDelete();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error while deleting question industry details", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                params.put("questionId", questionID);
                Log.d("QuestionParams", params.toString());
                return params;
            }
        };

        queue.add(deleteIndustryRequest);
    }

    private void deleteFromQuestion(String questionID, RequestQueue queue) {
        // Delete from url_delete_internship
        StringRequest deleteQuestionRequest = new StringRequest(Request.Method.POST, url_delete_question,
                response -> {
                    Log.d("QuestionResponse", response);
                    if (response.equals("Success")) {
                        fetchQuestions(); // Refresh the list after deletion
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete question", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error while deleting question", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("questionId", questionID);
                Log.d("QuestionParams", params.toString());
                return params;
            }
        };

        queue.add(deleteQuestionRequest);
    }


    private void editPost(HashMap<String, String> item) {
        Intent intent = new Intent(requireContext(), EditQuestionActivity.class);

        // Pass internship details to the EditInternshipActivity
        intent.putExtra("QuestionID", item.get("QuestionID"));
        intent.putExtra("title", item.get("title"));
        intent.putExtra("description", item.get("description"));
        intent.putExtra("date_shared", item.get("date_shared"));

        // Start the EditInternshipActivity
        startActivity(intent);
    }

    private void openCommentDialog(HashMap<String, String> item) {
        item.put("isInternship", "false");
        // Create an Intent to open ViewCommentActivity
        Intent intent = new Intent(requireContext(), ViewCommentActivity.class);

        // Pass the item details to the activity using Intent extras
        intent.putExtra("itemDetails", item);
        Log.d("ItemDetails", item.toString());

        // Start the ViewCommentActivity
        startActivity(intent);
    }


}