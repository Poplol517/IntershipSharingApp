package mdad.localdata.intershipsharingapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewAccountInternshipFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewAccountInternshipFragment extends Fragment {
    private LinearLayout lv;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String url_delete_internship = StaffMainActivity.ipBaseAddress + "/delete_internship.php";
    private static final String url_internship_industry = StaffMainActivity.ipBaseAddress + "/delete_internship_industry.php";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ViewAccountInternshipFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewAccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewAccountInternshipFragment newInstance(String param1, String param2) {
        ViewAccountInternshipFragment fragment = new ViewAccountInternshipFragment();
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
        View view = inflater.inflate(R.layout.fragment_view_account_internship, container, false);
        lv = view.findViewById(R.id.list);
        // Optionally, you can also fetch internships here if you want an initial fetch when the fragment is created
        fetchInternships();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch internships every time the user enters the page (fragment)
        fetchInternships();
    }

    private void fetchInternships() {
        String url_all_internship = StaffMainActivity.ipBaseAddress + "/get_all_internship.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", getContext().MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("username", "");
        Log.d("CurrentUserId", "Current User ID: " + currentUserId);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_all_internship,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] internships = response.split(":");
                    lv.removeAllViews();  // Clear the existing views before adding new ones
                    for (String internship : internships) {
                        if (!internship.isEmpty()) {
                            String[] details = internship.split(";");
                            if (details.length >= 10) {
                                String postUserId = details[7];

                                if (currentUserId.equals(postUserId)) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("InternshipID", details[0]);
                                    map.put("title", details[1]);
                                    map.put("description", details[2]);
                                    map.put("company", details[3]);
                                    map.put("start_date", details[4]);
                                    map.put("end_date", details[5]);
                                    map.put("date_shared", details[6]);
                                    map.put("UserID", details.length > 7 ? details[7] : "");
                                    map.put("user_name", details.length > 8 ? details[8] : "");
                                    map.put("username", details.length > 9 ? details[9] : "");
                                    map.put("location_name", details.length > 10 ? details[10] : "");
                                    map.put("role", details[11]);
                                    Log.d("DetailsArray", "Size: " + details.length + ", Content: " + Arrays.toString(details));
                                    addInternshipToLayout(map);
                                }
                            }
                        }
                    }
                },

                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error retrieving database", Toast.LENGTH_LONG).show();
                });

        queue.add(stringRequest);
    }

    private void addInternshipToLayout(final HashMap<String, String> item) {
        // Inflate the custom layout
        View postView = LayoutInflater.from(requireContext()).inflate(R.layout.internship_post_item, lv, false);

        // Set data for user info
        TextView userName = postView.findViewById(R.id.post_user_name);
        TextView userRole = postView.findViewById(R.id.post_user_role);
        TextView postContent = postView.findViewById(R.id.post_content);
        TextView postHashtags = postView.findViewById(R.id.post_hashtags);
        Button commentButton = postView.findViewById(R.id.comment_button);
        ImageView optionsMenu = postView.findViewById(R.id.options_menu);
        optionsMenu.setVisibility(View.VISIBLE);

        // Populate the fields with dynamic data
        userName.setText(item.get("user_name"));
        userRole.setText(item.get("company") + " | " + item.get("role"));
        postContent.setText(item.get("description"));

        // Check if description contains a hashtag
        String description = item.get("description");
        if (description != null && description.contains("#")) {
            postHashtags.setVisibility(View.VISIBLE);  // Ensure it's visible
        } else {
            postHashtags.setVisibility(View.GONE);  // Hide it if no hashtags are present
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

    private void confirmDelete(HashMap<String, String> item) {
        // Create LottieAnimationView dynamically
        LottieAnimationView lottieSuccess = new LottieAnimationView(requireContext());
        lottieSuccess.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        lottieSuccess.setAnimation(R.raw.warning);  // Reference your Lottie JSON animation
        lottieSuccess.playAnimation();

        // Create a TextView for the success message
        TextView messageTextView = new TextView(requireContext());
        messageTextView.setText("Are you sure you want to delete this internship?");
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
                .setPositiveButton("Delete", (dialog, which) -> deletePost(item.get("InternshipID")))
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
        messageTextView.setText("Internship deleted successfully!");
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
        messageTextView.setText("Failed to delete internship!!! \n Please try again.");
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
    private void deletePost(String internshipID) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // First, delete from url_internship_industry
        StringRequest deleteIndustryRequest = new StringRequest(Request.Method.POST, url_internship_industry,
                response -> {
                    Log.d("IndustryResponse", response);
                    if (response.equals("Success")) {
                        // Proceed to delete from url_delete_internship
                        deleteFromInternship(internshipID, queue);
                        successfulDelete();
                    } else {
                        failedDelete();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error while deleting internship industry details", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("internshipId", internshipID);
                Log.d("IndustryParams", params.toString());
                return params;
            }
        };

        queue.add(deleteIndustryRequest);
    }

    private void deleteFromInternship(String internshipID, RequestQueue queue) {
        // Delete from url_delete_internship
        StringRequest deleteInternshipRequest = new StringRequest(Request.Method.POST, url_delete_internship,
                response -> {
                    Log.d("InternshipResponse", response);
                    if (response.equals("Success")) {
                        fetchInternships(); // Refresh the list after deletion
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete internship", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error while deleting internship", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("internshipId", internshipID);
                Log.d("InternshipParams", params.toString());
                return params;
            }
        };

        queue.add(deleteInternshipRequest);
    }


    private void editPost(HashMap<String, String> item) {
        Intent intent = new Intent(requireContext(), EditInternshipActivity.class);

        // Pass internship details to the EditInternshipActivity
        intent.putExtra("InternshipID", item.get("InternshipID"));
        intent.putExtra("title", item.get("title"));
        intent.putExtra("description", item.get("description"));
        intent.putExtra("company", item.get("company"));
        intent.putExtra("start_date", item.get("start_date"));
        intent.putExtra("end_date", item.get("end_date"));
        intent.putExtra("date_shared", item.get("date_shared"));
        intent.putExtra("location_name", item.get("location_name"));
        intent.putExtra("role", item.get("role"));

        // Start the EditInternshipActivity
        startActivity(intent);
    }

    private void openCommentDialog(HashMap<String, String> item) {
        // Example Dialog for Comment
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Comment")
                .setMessage("Add a comment for the internship: " + item.get("title"))
                .setPositiveButton("Submit", (dialog, which) -> {
                    // Handle comment submission here
                    Toast.makeText(requireContext(), "Comment submitted for " + item.get("title"), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}
