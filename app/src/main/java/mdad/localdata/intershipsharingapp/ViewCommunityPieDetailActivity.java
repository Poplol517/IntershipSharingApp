package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class ViewCommunityPieDetailActivity extends AppCompatActivity {

    private static final String urlViewAllCommunities = StaffMainActivity.ipBaseAddress + "/get_all_communities.php";
    private LinearLayout lv;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<HashMap<String, String>> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_community_pie_detail);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchUser);
        lv = findViewById(R.id.list);

        Toolbar toolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Community Owner Distribution");
        }

        String category = getIntent().getStringExtra("category");
        TextView title = findViewById(R.id.title);
        title.setText("Community created by " + category);

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return false;
            }
        });

        fetchUser();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the back button click
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchUser() {
        swipeRefreshLayout.setRefreshing(true);
        lv.removeAllViews();
        userList.clear(); // Clear the list before adding new data
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlViewAllCommunities,
                response -> {
                    swipeRefreshLayout.setRefreshing(false);
                    if (response.equals("Error")) {
                        Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String category = getIntent().getStringExtra("category");
                    Log.d("Category", "Category: " + category);

                    String[] users = response.split(":");

                    for (String user : users) {
                        if (!user.isEmpty()) {
                            String[] details = user.split(";");
                            if (details.length >= 6) {
                                String role = details[6];
                                if (role.equals(category)) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("communityId", details[0]);
                                    map.put("name", details[1]);
                                    map.put("description", details[2]);
                                    map.put("owner", details[5]);
                                    map.put("role", role);
                                    map.put("photo", details[3]);
                                    userList.add(map); // Store all users
                                }
                            }
                        }
                    }

                    updateUserList(userList); // Display all users initially
                },

                error -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_LONG).show();
                });

        queue.add(stringRequest);
    }


    private void addUserToLayout(final HashMap<String, String> item) {
        Log.d("UserDetails", "Item: " + item);

        // Inflate the custom layout for displaying user info
        View postView = LayoutInflater.from(this).inflate(R.layout.joined_community_item, lv, false);

        // Set data for user info
        ImageView communityPhoto = postView.findViewById(R.id.communityPhoto);
        TextView title = postView.findViewById(R.id.post_community_name);
        TextView description = postView.findViewById(R.id.post_community_description);
        Button join = postView.findViewById(R.id.btnJoin);

        // Populate the fields with dynamic data
        title.setText(item.get("name"));
        description.setText("Owner: "+item.get("owner"));
        description.setTextSize(15);
        join.setVisibility(View.GONE);

        // Handle the image data for profile photo
        String photoData = item.get("photo");
        if (photoData != null && !photoData.isEmpty()) {
            saveBase64ToFile(photoData, file -> {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    communityPhoto.setImageBitmap(bitmap);
                } else {
                    communityPhoto.setImageResource(R.drawable.account);
                }
            });
        } else {
            communityPhoto.setImageResource(R.drawable.no_image);
        }


        postView.setOnClickListener(v -> {
            openCommunityDetailActivity(item);
        });

        // Add the postView to the parent layout (list of users)
        lv.addView(postView);
    }
    private void filterUsers(String query) {
        ArrayList<HashMap<String, String>> filteredList = new ArrayList<>();

        for (HashMap<String, String> user : userList) {
            String name = user.get("name").toLowerCase();
            String course = user.get("owner").toLowerCase();

            if (name.contains(query.toLowerCase()) || course.contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }

        updateUserList(filteredList);
    }
    private void updateUserList(ArrayList<HashMap<String, String>> list) {
        lv.removeAllViews(); // Clear the UI before adding new views
        for (HashMap<String, String> user : list) {
            addUserToLayout(user);
        }
    }

    private void openCommunityDetailActivity(HashMap<String, String> user) {
        Intent intent = new Intent(this, ViewSelectedCommunityActivity.class);

        intent.putExtra("userId", user.get("userId"));
        intent.putExtra("title", user.get("name"));
        intent.putExtra("communityId", user.get("communityId"));
        intent.putExtra("description", user.get("description"));

        // Convert Base64 to Bitmap
        String photoData = user.get("photo");
        if (photoData != null && !photoData.isEmpty()) {
            byte[] decodedBytes = android.util.Base64.decode(photoData, android.util.Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                intent.putExtra("image_bitmap", bitmap);
            }
        }

        startActivity(intent);
    }



    private void saveBase64ToFile(String base64Data, ViewAccountFragment.OnFileSavedListener listener) {
        try {
            // Decode the base64 data into a byte array
            byte[] decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
            Log.d("Base64Decode", "Decoded bytes length: " + decodedBytes.length);

            // Create the PNG file in the cache directory
            File cacheDir = this.getCacheDir();
            File imageFile = new File(cacheDir, "community_image.png");  // PNG extension

            // Save the decoded bytes to the file
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(decodedBytes);
                fos.flush();  // Ensure all data is written
                listener.onFileSaved(imageFile);  // Notify when file is saved
            }

        } catch (Exception e) {
            Log.e("FileSaveError", "Error saving Base64 to file: " + e.getMessage());
        }
    }
}