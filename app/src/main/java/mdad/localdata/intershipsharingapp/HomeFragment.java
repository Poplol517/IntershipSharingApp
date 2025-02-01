package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class HomeFragment extends Fragment {

    private LinearLayout lv;
    private ArrayList<HashMap<String, String>> allInternships = new ArrayList<>();
    private ArrayList<HashMap<String, String>> allQuestions = new ArrayList<>();
    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        lv = view.findViewById(R.id.list);
        searchView = view.findViewById(R.id.searchView);

        // Setup SearchView listener
        setupSearchView();
        fetchInternships();
        fetchQuestions();
        return view;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPosts(newText);
                return false;
            }
        });
    }

    private void filterPosts(String query) {
        lv.removeAllViews(); // Clear current posts

        String queryLower = query.toLowerCase();

        // Filter internships
        for (HashMap<String, String> internship : allInternships) {
            if (matchesQuery(internship, queryLower)) {
                addInternshipToLayout(internship);
            }
        }

        // Filter questions
        for (HashMap<String, String> question : allQuestions) {
            if (matchesQuery(question, queryLower)) {
                addQuestionToLayout(question);
            }
        }
    }

    private boolean matchesQuery(HashMap<String, String> item, String query) {
        String title = item.get("title").toLowerCase();
        //String description = item.get("description").toLowerCase();
        String company = item.get("company").toLowerCase();
        String role = item.get("role").toLowerCase();
        String userName = item.get("user_name").toLowerCase();
        String postType = item.get("isInternship") != null && item.get("isInternship").equals("true") ? "internship" : "question"; // Assuming "isInternship" determines post type

        // Check if query matches title, description, company, role, user name, or post type
        return title.contains(query) ||  company.contains(query) || role.contains(query) ||
                userName.contains(query) || postType.contains(query);

        //Inlcude if want to search by description
        //description.contains(query) ||
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
                                map.put("photo", details.length > 12 ? details[12] : "");
                                Log.d("DetailsArray", "Size: " + details.length + ", Content: " + Arrays.toString(details));
                                allInternships.add(map);  // Add to the list
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
        ImageView profile_icon = postView.findViewById(R.id.profile_icon);
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
        String photoData = item.get("photo");
        Log.d("UserDetails", "Photo Data: " + item.get("photo"));
        if (photoData != null && !photoData.isEmpty()) {
            saveBase64ToFile(photoData, file -> {
                Log.d("UserDetails", "File Path: " + file.getAbsolutePath());
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
        item.put("isInternship", "true");
        Log.d("ItemDetails", item.toString());
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
                            if (details.length >= 8) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("QuestionID", details[0]);
                                map.put("title", details[1]);
                                map.put("description", details[2]);
                                map.put("company", details[3]);
                                map.put("UserID", details[4]);
                                map.put("user_name", details.length > 5 ? details[5] : "");
                                map.put("course", details.length > 6 ? details[6] : "");
                                map.put("role", details[7]);
                                map.put("photo", details.length > 8 ? details[8] : "");
                                Log.d("DetailsArray", "Size: " + details.length + ", Content: " + Arrays.toString(details));
                                allQuestions.add(map);  // Add to the list
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
        ImageView profile_icon = postView.findViewById(R.id.profile_icon);
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
