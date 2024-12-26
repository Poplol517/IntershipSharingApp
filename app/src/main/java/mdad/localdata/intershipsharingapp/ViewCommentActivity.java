package mdad.localdata.intershipsharingapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link } subclass for viewing comments on a post.
 */
public class ViewCommentActivity extends AppCompatActivity {
    private static String url_view_comment = StaffMainActivity.ipBaseAddress + "/get_all_internship_comment.php";
    private static String url_create_comment = StaffMainActivity.ipBaseAddress + "/create_comment.php";

    private HashMap<String, String> itemDetails;
    private List<HashMap<String, String>> commentList; // List to store comment data
    private LinearLayout commentsLinearLayout; // LinearLayout for comments

    // UI elements for new comment
    private EditText newCommentEditText;
    private Button submitCommentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comment); // Set the layout for the activity

        // Retrieve item details from intent
        if (getIntent().getExtras() != null) {
            itemDetails = (HashMap<String, String>) getIntent().getSerializableExtra("itemDetails");
            Log.d("ViewCommentActivity", "Received itemDetails: " + itemDetails);
        }

        // Initialize variables
        commentList = new ArrayList<>();
        commentsLinearLayout = findViewById(R.id.comments_section);

        // Initialize new comment UI elements
        newCommentEditText = findViewById(R.id.new_comment_input);
        submitCommentButton = findViewById(R.id.send_comment_button);

        // Set up the button click listener to submit the new comment
        submitCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newCommentText = newCommentEditText.getText().toString().trim();
                if (newCommentText.isEmpty()) {
                    Toast.makeText(ViewCommentActivity.this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    // Send the new comment to the server
                    createComment(newCommentText);
                }
            }
        });

        // Populate post data
        TextView userName = findViewById(R.id.post_user_name);
        TextView userRole = findViewById(R.id.post_user_role);
        TextView postContent = findViewById(R.id.post_content);
        TextView postHashtags = findViewById(R.id.post_hashtags);

        if (itemDetails != null) {
            Log.d("ViewCommentActivity", "Populating post data with itemDetails: " + itemDetails);
            userName.setText(itemDetails.get("user_name"));
            userRole.setText(itemDetails.get("company") + " | " + itemDetails.get("role"));
            postContent.setText(itemDetails.get("description"));
            postHashtags.setText(itemDetails.get("hashtags"));
        }

        // Fetch comments from the server
        postData(url_view_comment, null);
    }

    // Method to create a new comment and send it to the server
    private void createComment(String commentText) {
        String userId = itemDetails.get("UserID"); // Retrieve the user ID
        Log.d("ViewCommentActivity", "User ID: " + userId);
        String internshipId = itemDetails.get("InternshipID"); // Retrieve the internship ID

        // Create a map of parameters to send to the server
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put("internshipId", internshipId);
        params.put("description", commentText);

        // Send a POST request using Volley to the create_comment.php endpoint
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_create_comment,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ViewCommentActivity", "Create Comment Response: " + response);
                        if (response.equals("Success")) {
                            // If the comment is created successfully, update the UI
                            Toast.makeText(getApplicationContext(), "Comment posted successfully", Toast.LENGTH_SHORT).show();
                            // Optionally, reload the comments
                            postData(url_view_comment, null);
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to post comment", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error posting comment", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        // Add the request to the queue
        queue.add(stringRequest);
    }

    public void postData(String url, Map params) {
        // Create a RequestQueue for Volley
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a StringRequest for HTTP Post
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ViewCommentActivity", "Response: " + response);
                        if (response.equals("Error")) {
                            Toast.makeText(getApplicationContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Parse the response and populate the commentList
                        parseComments(response);

                        // Generate views for the comments
                        generateCommentViews(LayoutInflater.from(ViewCommentActivity.this));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error in retrieving database", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // Add the StringRequest to RequestQueue
        queue.add(stringRequest);
    }

    private void parseComments(String response) {
        commentList.clear(); // Clear existing comments
        Log.d("ViewCommentActivity", "Raw Response: " + response);

        // Clean and split response
        response = response.trim();
        if (response.endsWith(":")) {
            response = response.substring(0, response.length() - 1);
        }
        Log.d("ViewCommentActivity", "Cleaned Response: " + response);

        String[] comments = response.split(":");
        Log.d("ViewCommentActivity", "Parsed Comments Array: " + Arrays.toString(comments));

        for (String commentData : comments) {
            String[] details = commentData.split(";");
            Log.d("ViewCommentActivity", "Parsed Details Array: " + Arrays.toString(details));

            if (details.length >= 6) {
                HashMap<String, String> map = new HashMap<>();
                map.put("comment_id", details[0]);
                map.put("description", details[1]);
                map.put("userId", details[2]);
                map.put("internshipId", details.length > 3 ? details[3] : "");
                map.put("user_name", details[4]);
                map.put("role", details[5]);
                map.put("course", details[6]);

                commentList.add(map);
                Log.d("ViewCommentActivity", "Comment Added: " + map);
            } else {
                Log.e("ViewCommentActivity", "Invalid Comment Data: " + Arrays.toString(details));
            }
        }

        Log.d("ViewCommentActivity", "Final Comment List: " + commentList);
    }

    // Method to generate TextViews for each comment dynamically using the custom layout
    private void generateCommentViews(LayoutInflater inflater) {
        commentsLinearLayout.removeAllViews(); // Clear existing comments

        // Retrieve the internship ID of the current post from itemDetails
        String currentInternshipId = itemDetails.get("InternshipID");
        Log.d("ViewCommentActivity", "Current Internship ID: " + currentInternshipId);

        // Filter the comments to only display those with the same internship ID
        List<HashMap<String, String>> filteredComments = new ArrayList<>();
        for (int i = 0; i < commentList.size(); i++) {
            HashMap<String, String> commentData = commentList.get(i);
            String commentInternshipId = commentData.get("internshipId");

            // Only add comments that match the internship ID
            if (currentInternshipId != null && currentInternshipId.equals(commentInternshipId)) {
                filteredComments.add(commentData);
            }
        }

        // Generate views for the filtered comments
        for (int i = 0; i < filteredComments.size(); i++) {
            // Inflate the comment_layout.xml for each filtered comment
            View commentView = inflater.inflate(R.layout.comment_layout, commentsLinearLayout, false);

            // Get references to the views inside comment_layout.xml
            TextView commentUserName = commentView.findViewById(R.id.comment_user_name);
            TextView commentRole = commentView.findViewById(R.id.comment_role);
            TextView commentContent = commentView.findViewById(R.id.comment_content);

            // Get filtered comment data
            HashMap<String, String> commentData = filteredComments.get(i);
            Log.d("ViewCommentActivity", "Filtered Comment Data: " + commentData);

            // Set the comment content
            commentUserName.setText(commentData.get("user_name")); // Set user ID or name
            commentRole.setText(commentData.get("role") + " | " + commentData.get("course"));
            commentContent.setText(commentData.get("description")); // Set the comment description

            // Add the inflated view to the LinearLayout
            commentsLinearLayout.addView(commentView);
        }
    }
}
