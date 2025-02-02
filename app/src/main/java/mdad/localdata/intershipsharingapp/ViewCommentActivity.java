package mdad.localdata.intershipsharingapp;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
    private static String url_edit_question_comment = StaffMainActivity.ipBaseAddress + "/edit_question_comment.php";
    private static String url_edit_internship_comment = StaffMainActivity.ipBaseAddress + "/edit_internship_comment.php";
    private static String url_delete_question_comment = StaffMainActivity.ipBaseAddress + "/delete_question_comment.php";
    private static String url_delete_internship_comment = StaffMainActivity.ipBaseAddress + "/delete_internship_comment.php";

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
        SearchView searchView = findViewById(R.id.search_view);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle query submission (optional)
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the comments as the user types
                filterComments(newText.trim());
                return true;
            }
        });

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
                    newCommentEditText.setText("");
                }
            }
        });

        // Populate post data
        ImageView profile_icon = findViewById(R.id.profile_icon);
        TextView userName = findViewById(R.id.post_user_name);
        TextView userRole = findViewById(R.id.post_user_role);
        TextView postContent = findViewById(R.id.post_content);
        TextView postHashtags = findViewById(R.id.post_hashtags);

        if (itemDetails != null) {
            Log.d("ViewCommentActivity", "Populating post data with itemDetails: " + itemDetails);
            userName.setText(itemDetails.get("user_name"));
            postContent.setText(itemDetails.get("description"));
            postHashtags.setText(itemDetails.get("hashtags"));
        }

        // Determine the correct URL based on the "isInternship" flag
        String isInternshipFlag = itemDetails.get("isInternship");
        Log.d("ViewCommentActivity", "isInternship Flag: " + isInternshipFlag);

        String urlToUse;
        if ("true".equals(isInternshipFlag)) {
            userRole.setText(itemDetails.get("company") + " | " + itemDetails.get("role"));
            urlToUse = url_view_internship_comment;
        } else if ("false".equals(isInternshipFlag)) {
            userRole.setText(itemDetails.get("role") + " | " + itemDetails.get("course"));
            urlToUse = url_view_question_comment;
        } else {
            Log.e("ViewCommentActivity", "Invalid isInternship flag value: " + isInternshipFlag);
            Toast.makeText(this, "Invalid data received. Cannot load comments.", Toast.LENGTH_SHORT).show();
            return;
        }
        String photoData = itemDetails.get("photo");
        Log.d("UserDetails", "Photo Data: " + itemDetails.get("photo"));
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

        Log.d("ViewCommentActivity", "URL Selected: " + urlToUse);

        // Fetch comments from the server
        postData(urlToUse, null);
    }

    private void saveBase64ToFile(String base64Data, ViewAccountFragment.OnFileSavedListener listener) {
        try {
            byte[] decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);

            // Save the decoded bytes to a file in cache directory
            File cacheDir = this.getCacheDir();
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


    // Method to create a new comment and send it to the server
    private void createComment(String commentText) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", ""); // Retrieve the user ID from shared preferences
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
                            // Reload comments dynamically based on the flag
                            String reloadUrl = isInternship ? url_view_internship_comment : url_view_question_comment;
                            postData(reloadUrl, null);
                        } else {
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


        String[] comments = response.split(":");


        boolean isInternship = "true".equals(itemDetails.get("isInternship"));
        String targetId = itemDetails.get(isInternship ? "InternshipID" : "QuestionID");
        Log.d("ViewCommentActivity", "isInternshipFlag: " + isInternship + ", Target ID: " + targetId);

        for (String commentData : comments) {
            String[] details = commentData.split(";");
            Log.d("ViewCommentActivity", "Parsed Details Array: " + Arrays.toString(details));

            if (details.length >= 6) {
                HashMap<String, String> map = new HashMap<>();
                map.put("comment_id", details[0]);
                map.put("description", details[1]);
                map.put("userId", details[2]);

                // Add conditional mapping based on isInternshipFlag
                String id = "";
                if (isInternship) {
                    id = details.length > 3 ? details[3] : "";
                    map.put("internshipId", id);
                } else {
                    id = details.length > 3 ? details[3] : "";
                    map.put("questionId", id);
                }

                // Check if the comment matches the target internshipId or questionId
                if (id.equals(targetId)) {
                    map.put("user_name", details[4]);
                    map.put("role", details[5]);
                    map.put("course", details[6]);
                    map.put("date_shared", details.length > 7 ? details[7] : "");

                    commentList.add(map);
                    Log.d("ViewCommentActivity", "Comment Added: " + map);
                } else {
                    Log.d("ViewCommentActivity", "Comment Skipped (ID doesn't match): " + Arrays.toString(details));
                }
            } else {
                Log.e("ViewCommentActivity", "Invalid Comment Data: " + Arrays.toString(details));
            }
        }
        Log.d("ViewCommentActivity", "Final Comment List: " + commentList);

        Collections.sort(commentList, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> comment1, HashMap<String, String> comment2) {
                String date1 = comment1.get("date_shared");
                String date2 = comment2.get("date_shared");

                // If date1 and date2 are empty, treat them as the earliest (in case some comments don't have a date)
                if (date1.isEmpty() && date2.isEmpty()) {
                    return 0;
                }
                if (date1.isEmpty()) {
                    return 1;
                }
                if (date2.isEmpty()) {
                    return -1;
                }

                // Compare dates in reverse order (latest first)
                return date2.compareTo(date1);
            }
        });
    }



    // Method to generate TextViews for each comment dynamically using the custom layout
    private void generateCommentViews(LayoutInflater inflater) {
        commentsLinearLayout.removeAllViews(); // Clear existing comments

        // Retrieve the logged-in user's ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String loggedInUserId = sharedPreferences.getString("username", null); // Replace "user_id" with the actual key
        String roleId = sharedPreferences.getString("roleId", null);
        // Determine the ID key and value dynamically
        boolean isInternship = "true".equals(itemDetails.get("isInternship"));
        String idKey = isInternship ? "internshipId" : "questionId";
        String currentId = itemDetails.get(isInternship ? "InternshipID" : "QuestionID");

        // Filter the comments based on the ID
        List<HashMap<String, String>> filteredComments = new ArrayList<>();
        for (HashMap<String, String> commentData : commentList) {
            String commentId = commentData.get(idKey);

            if (currentId != null && currentId.equals(commentId)) {
                filteredComments.add(commentData);
            }
        }

        // Generate views for the filtered comments
        for (HashMap<String, String> commentData : filteredComments) {
            View commentView = inflater.inflate(R.layout.comment_layout, commentsLinearLayout, false);

            TextView commentUserName = commentView.findViewById(R.id.comment_user_name);
            TextView commentRole = commentView.findViewById(R.id.comment_role);
            TextView commentContent = commentView.findViewById(R.id.comment_content);

            // Set the comment content
            commentUserName.setText(commentData.get("user_name"));
            commentRole.setText(commentData.get("role") + " | " + commentData.get("course"));
            commentContent.setText(commentData.get("description"));

            // Check if the logged-in user ID matches the comment's user ID
            if (loggedInUserId != null && loggedInUserId.equals(commentData.get("userId")) || roleId.equals("3")) {
                // Set up long-click listener for the comment view
                commentView.setOnLongClickListener(view -> {
                    // Create and show a BottomSheetDialog
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
                    View sheetView = inflater.inflate(R.layout.comment_option_menu, null);
                    bottomSheetDialog.setContentView(sheetView);

                    // Set up button actions
                    LinearLayout optionsPage = sheetView.findViewById(R.id.optionsPage);
                    LinearLayout editCommentPage = sheetView.findViewById(R.id.editCommentPage);
                    Button editCommentButton = sheetView.findViewById(R.id.btn_edit_comment);
                    Button deleteCommentButton = sheetView.findViewById(R.id.btn_delete_comment);
                    Button saveEditButton = sheetView.findViewById(R.id.save_edited_comment);

                    // Find the EditText where the comment will be edited
                    EditText editCommentInput = sheetView.findViewById(R.id.edit_comment_input);

                    // When Edit Comment button is clicked
                    editCommentButton.setOnClickListener(v -> {
                        // Hide the options page and show the edit comment page
                        optionsPage.setVisibility(View.GONE);
                        editCommentPage.setVisibility(View.VISIBLE);

                        // Set the current description in the EditText
                        editCommentInput.setText(commentData.get("description"));
                    });

                    deleteCommentButton.setOnClickListener(v -> {
                        bottomSheetDialog.dismiss();
                        deleteComment(commentData); // Handle delete action
                    });

                    saveEditButton.setOnClickListener(v -> {
                        // Get the edited comment from the EditText field
                        String editedComment = editCommentInput.getText().toString().trim();

                        if (editedComment.isEmpty()) {
                            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get the comment ID from commentData
                        String commentId = commentData.get("comment_id");

                        if (commentId == null) {
                            Toast.makeText(this, "Comment ID not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Update the comment data with the new description
                        commentData.put("description", editedComment);

                        // Pass the comment ID and updated description to the editComment function
                        editComment(commentId, editedComment);  // Call the editComment method to update the comment on the server

                        bottomSheetDialog.dismiss(); // Dismiss the dialog after saving
                    });

                    bottomSheetDialog.show();
                    return true; // Indicate the long press was handled
                });
            }

            // Add the inflated view to the LinearLayout
            commentsLinearLayout.addView(commentView);
        }
    }


    private void editComment(String commentId, String editedDescription) {
        // Determine whether the comment is related to an internship or a question
        boolean isInternship = "true".equals(itemDetails.get("isInternship"));
        String urlToUse = isInternship ? url_edit_internship_comment : url_edit_question_comment;

        // Create a map for the parameters to send to the server
        Map<String, String> params = new HashMap<>();
        params.put("commentId", commentId);
        params.put("description", editedDescription);

        // Send the updated comment data to the server using Volley
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlToUse,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ViewCommentActivity", "Edit Comment Response: " + response);

                        try {
                            // Parse the JSON response from the server
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                // If the comment is edited successfully, show a success message
                                String commentId = jsonResponse.getString("commentId");
                                Toast.makeText(getApplicationContext(), "Comment updated successfully. Comment ID: " + commentId, Toast.LENGTH_SHORT).show();

                                // Reload the comments after updating
                                boolean isInternship = "true".equals(itemDetails.get("isInternship"));
                                String reloadUrl = isInternship ? url_view_internship_comment : url_view_question_comment;
                                postData(reloadUrl, null);
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to update comment", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error updating comment", Toast.LENGTH_SHORT).show();
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

    private void deleteComment(HashMap<String, String> commentData) {
        String commentId = commentData.get("comment_id");

        // Determine which URL to use based on the "isInternship" flag
        boolean isInternship = "true".equals(itemDetails.get("isInternship"));
        String deleteUrl = isInternship ? url_delete_internship_comment : url_delete_question_comment;
        // Create a map of parameters to send for deleting the comment
        Map<String, String> params = new HashMap<>();
        params.put("commentId", commentId);

        // Send a POST request to delete the comment
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, deleteUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ViewCommentActivity", "Delete Comment Response: " + response);
                        if ("success".equals(response)) {
                            Toast.makeText(getApplicationContext(), "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                            // After deleting, reload the comments
                            String urlToUse = isInternship ? url_view_internship_comment : url_view_question_comment;
                            postData(urlToUse, null);
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to delete comment", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error deleting comment", Toast.LENGTH_SHORT).show();
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

    private void filterComments(String query) {
        // If the query is empty, show all comments
        if (query.isEmpty()) {
            generateCommentViews(LayoutInflater.from(this)); // Show all comments again
            return;
        }

        // Create a filtered list based on the query
        List<HashMap<String, String>> filteredList = new ArrayList<>();
        for (HashMap<String, String> comment : commentList) {
            Log.d("ViewCommentActivity", "Processing Comment: " + comment);
            String description = comment.get("description");
            String userName = comment.get("user_name");
            String role = comment.get("role");
            String companyOrCourse = comment.get("company"); // For internships, company name
            if (companyOrCourse == null) {
                companyOrCourse = comment.get("course"); // For questions, course name
            }

            // Check if the description, username, role, or company/course matches the query (case-insensitive)
            if ((description != null && description.toLowerCase().contains(query.toLowerCase())) ||
                    (userName != null && userName.toLowerCase().contains(query.toLowerCase())) ||
                    (role != null && role.toLowerCase().contains(query.toLowerCase())) ||
                    (companyOrCourse != null && companyOrCourse.toLowerCase().contains(query.toLowerCase()))) {
                filteredList.add(comment);
            }
        }

        // Clear the comment section and add filtered comments
        commentsLinearLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (HashMap<String, String> comment : filteredList) {
            View commentView = inflater.inflate(R.layout.comment_layout, commentsLinearLayout, false);

            TextView commentUserName = commentView.findViewById(R.id.comment_user_name);
            TextView commentRole = commentView.findViewById(R.id.comment_role);
            TextView commentContent = commentView.findViewById(R.id.comment_content);

            commentUserName.setText(comment.get("user_name"));
            commentRole.setText(comment.get("role") + " | " + comment.get("course"));
            commentContent.setText(comment.get("description"));

            commentsLinearLayout.addView(commentView);
        }
    }

}