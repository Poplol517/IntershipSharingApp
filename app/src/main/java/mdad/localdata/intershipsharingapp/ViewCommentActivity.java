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
    private static String url_view_internship_comment = StaffMainActivity.ipBaseAddress + "/get_all_internship_comment.php";

    private static String url_view_question_comment = StaffMainActivity.ipBaseAddress + "/get_all_question_comment.php";
    private static String url_create_internship_comment = StaffMainActivity.ipBaseAddress + "/create_internship_comment.php";
    private static String url_create_question_comment = StaffMainActivity.ipBaseAddress + "/create_question_comment.php";

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

        // Determine the correct URL based on the "isInternship" flag
        String isInternshipFlag = itemDetails.get("isInternship");
        Log.d("ViewCommentActivity", "isInternship Flag: " + isInternshipFlag);

        String urlToUse;
        if ("true".equals(isInternshipFlag)) {
            urlToUse = url_view_internship_comment;
        } else if ("false".equals(isInternshipFlag)) {
            urlToUse = url_view_question_comment;
        } else {
            Log.e("ViewCommentActivity", "Invalid isInternship flag value: " + isInternshipFlag);
            Toast.makeText(this, "Invalid data received. Cannot load comments.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ViewCommentActivity", "URL Selected: " + urlToUse);

        // Fetch comments from the server
        postData(urlToUse, null);
    }


    // Method to create a new comment and send it to the server
    private void createComment(String commentText) {
        String userId = itemDetails.get("UserID"); // Retrieve the user ID
        Log.d("ViewCommentActivity", "User ID: " + userId);

        // Check the flag and decide which ID to use
        boolean isInternship = "true".equals(itemDetails.get("isInternship"));
        String idKey = isInternship ? "internshipId" : "questionId";
        String itemId = itemDetails.get(isInternship ? "InternshipID" : "QuestionID"); // Retrieve the appropriate ID

        // Create a map of parameters to send to the server
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);
        params.put(idKey, itemId);
        params.put("description", commentText);

        // Use the appropriate URL for creating comments
        String url = isInternship ? url_create_internship_comment : url_create_question_comment;

        // Send a POST request using Volley to the correct endpoint
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ViewCommentActivity", "Create Comment Response: " + response);
                        if (response.equals("success")) {
                            // If the comment is created successfully, update the UI
                            Toast.makeText(getApplicationContext(), "Comment posted successfully", Toast.LENGTH_SHORT).show();
                            // Reload comments dynamically based on the flag
                            String reloadUrl = isInternship ? url_view_internship_comment : url_view_question_comment;
                            postData(reloadUrl, null);
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
        Log.d("ViewCommentActivity", "URL: " + url);

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

                        // Generate views for the comments dynamically based on the flag
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

        boolean isInternshipFlag = "true".equals(itemDetails.get("isInternship"));
        Log.d("ViewCommentActivity", "isInternshipFlag: " + isInternshipFlag);

        for (String commentData : comments) {
            String[] details = commentData.split(";");
            Log.d("ViewCommentActivity", "Parsed Details Array: " + Arrays.toString(details));

            if (details.length >= 6) {
                HashMap<String, String> map = new HashMap<>();
                map.put("comment_id", details[0]);
                map.put("description", details[1]);
                map.put("userId", details[2]);

                // Add conditional mapping based on isInternshipFlag
                if (isInternshipFlag) {
                    map.put("internshipId", details.length > 3 ? details[3] : "");
                } else {
                    map.put("questionId", details.length > 3 ? details[3] : "");
                }
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

        // Determine the ID key and value dynamically
        boolean isInternship = "true".equals(itemDetails.get("isInternship"));
        String idKey = isInternship ? "internshipId" : "questionId";
        String currentId = itemDetails.get(isInternship ? "InternshipID" : "QuestionID");
        Log.d("ViewCommentActivity", "Current ID (" + idKey + "): " + currentId);

        // Filter the comments based on the ID
        List<HashMap<String, String>> filteredComments = new ArrayList<>();
        for (HashMap<String, String> commentData : commentList) {
            String commentId = commentData.get(idKey);

            // Only add comments that match the current ID
            if (currentId != null && currentId.equals(commentId)) {
                filteredComments.add(commentData);
            }
        }

        // Generate views for the filtered comments
        for (HashMap<String, String> commentData : filteredComments) {
            // Inflate the comment_layout.xml for each filtered comment
            View commentView = inflater.inflate(R.layout.comment_layout, commentsLinearLayout, false);

            // Get references to the views inside comment_layout.xml
            TextView commentUserName = commentView.findViewById(R.id.comment_user_name);
            TextView commentRole = commentView.findViewById(R.id.comment_role);
            TextView commentContent = commentView.findViewById(R.id.comment_content);

            // Set the comment content
            commentUserName.setText(commentData.get("user_name")); // Set user ID or name
            commentRole.setText(commentData.get("role") + " | " + commentData.get("course"));
            commentContent.setText(commentData.get("description")); // Set the comment description

            // Add the inflated view to the LinearLayout
            commentsLinearLayout.addView(commentView);
        }
    }

}