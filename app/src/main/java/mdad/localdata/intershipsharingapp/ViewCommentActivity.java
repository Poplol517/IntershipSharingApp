package mdad.localdata.intershipsharingapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
    private static String url_view_comment = StaffMainActivity.ipBaseAddress + "/get_all_comment.php";

    private HashMap<String, String> itemDetails;
    private List<HashMap<String, String>> commentList; // List to store comment data
    private LinearLayout commentsLinearLayout; // LinearLayout for comments

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

        // Populate post data
        TextView userName = findViewById(R.id.post_user_name);
        TextView userRole = findViewById(R.id.post_user_role);
        TextView postContent = findViewById(R.id.post_content);
        TextView postHashtags = findViewById(R.id.post_hashtags);

        if (itemDetails != null) {
            userName.setText(itemDetails.get("user_name"));
            userRole.setText(itemDetails.get("company") + " | " + itemDetails.get("role"));
            postContent.setText(itemDetails.get("description"));
            postHashtags.setText(itemDetails.get("hashtags"));
        }

        // Fetch comments from the server
        postData(url_view_comment, null);
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

            if (details.length >= 7) {
                HashMap<String, String> map = new HashMap<>();
                map.put("comment_id", details[0]);
                map.put("description", details[1]);
                map.put("userId", details[2]);
                map.put("internshipId", details.length > 3 ? details[3] : "");
                map.put("questionId", details.length > 4 ? details[4] : "");
                map.put("user_name", details[5]);
                map.put("role", details[6]);
                map.put("course", details[7]);


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

        for (int i = 0; i < commentList.size(); i++) {
            // Inflate the comment_layout.xml for each comment
            View commentView = inflater.inflate(R.layout.comment_layout, commentsLinearLayout, false);

            // Get references to the views inside comment_layout.xml
            TextView commentUserName = commentView.findViewById(R.id.comment_user_name);
            TextView commentRole = commentView.findViewById(R.id.comment_role);
            TextView commentContent = commentView.findViewById(R.id.comment_content);

            // Get comment data
            HashMap<String, String> commentData = commentList.get(i);
            Log.d("ViewCommentActivity", "Comment Data: " + commentData);

            // Set the comment content
            commentUserName.setText(commentData.get("user_name")); // Set user ID or name
            commentRole.setText(commentData.get("role")+" | "+commentData.get("course"));

            commentContent.setText(commentData.get("description")); // Set the comment description

            // Add the inflated view to the LinearLayout
            commentsLinearLayout.addView(commentView);
        }
    }
}

