package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.HashMap;

public class HomeFragment extends Fragment {

    private LinearLayout lv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        lv = view.findViewById(R.id.list);
        fetchInternships();
        fetchQuestions();
        return view;
    }

    private void fetchInternships() {
        String url_all_internship = StaffMainActivity.ipBaseAddress + "/get_all_internship.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_all_internship,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] internships = response.split(":");
                    for (String internship : internships) {
                        if (!internship.isEmpty()) {
                            String[] details = internship.split(";");
                            if (details.length >= 10) {
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
        TextView postTitle = postView.findViewById(R.id.post_title);
        TextView postContent = postView.findViewById(R.id.post_content);
        TextView postHashtags = postView.findViewById(R.id.post_hashtags);
        Button commentButton = postView.findViewById(R.id.comment_button);

        // Populate the fields with dynamic data
        userName.setText(item.get("user_name"));
        postTitle.setText(item.get("title"));
        userRole.setText(item.get("company") + " | " + item.get("role"));
        postContent.setText(item.get("description"));

        // Check if description contains a hashtag
        String description = item.get("description");
        if (description != null && description.contains("#")) {
            postHashtags.setVisibility(View.VISIBLE);  // Ensure it's visible
        } else {
            postHashtags.setVisibility(View.GONE);  // Hide it if no hashtags are present
        }
        item.put("isInternship", "true");
        Log.d("ItemDetails", item.toString());
        // Comment button click listener
        commentButton.setOnClickListener(v -> openCommentDialog(item));

        // Add the postView to the parent layout
        lv.addView(postView);
    }

    private void fetchQuestions() {
        String url_all_question = StaffMainActivity.ipBaseAddress + "/get_all_question.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_all_question,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] questions = response.split(":");
                    for (String question : questions) {
                        if (!question.isEmpty()) {
                            String[] details = question.split(";");
                            if (details.length >= 7) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("QuestionID", details[0]);
                                map.put("title", details[1]);
                                map.put("description", details[2]);
                                map.put("company", details[3]);
                                map.put("UserID", details[4]);
                                map.put("user_name", details.length > 5 ? details[5] : "");
                                map.put("course", details.length > 6 ? details[6] : "");
                                map.put("role", details[7]);
                                Log.d("DetailsArray", "Size: " + details.length + ", Content: " + Arrays.toString(details));
                                addQuestionToLayout(map);
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
    private void addQuestionToLayout(final HashMap<String, String> item) {
        // Inflate the custom layout
        View postView = LayoutInflater.from(requireContext()).inflate(R.layout.question_post_item, lv, false);

        // Set data for user info
        TextView userName = postView.findViewById(R.id.post_user_name);
        TextView userRole = postView.findViewById(R.id.post_user_role);
        TextView postTitle = postView.findViewById(R.id.post_title);
        TextView postContent = postView.findViewById(R.id.post_content);
        TextView postHashtags = postView.findViewById(R.id.post_hashtags);
        Button commentButton = postView.findViewById(R.id.comment_button);

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
        item.put("isInternship", "false");
        Log.d("ItemDetails", item.toString());
        // Comment button click listener
        commentButton.setOnClickListener(v -> openCommentDialog(item));

        // Add the postView to the parent layout
        lv.addView(postView);
    }


    private void openCommentDialog(HashMap<String, String> item) {
        // Add an internship flag to the HashMap
         // You can set this value based on your condition

        // Create an Intent to open ViewCommentActivity
        Intent intent = new Intent(requireContext(), ViewCommentActivity.class);

        // Pass the item details to the activity using Intent extras
        intent.putExtra("itemDetails", item);
        Log.d("ItemDetails", item.toString());

        // Start the ViewCommentActivity
        startActivity(intent);
    }
}
