package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewCommunityDetailActivity extends AppCompatActivity {

    private static final String url_get_userchat = StaffMainActivity.ipBaseAddress + "/get_all_userchat.php";
    private LinearLayout lv;
    private List<HashMap<String, String>> memberList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_community_detail);  // Replace with your activity layout

        // Find views by their IDs
        ImageView communityPhoto = findViewById(R.id.communityPhoto);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvDescription = findViewById(R.id.tvDescription);
        lv = findViewById(R.id.list);

        // Get arguments passed from the previous activity or fragment
        Intent intent = getIntent();
        String name = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String communityId = intent.getStringExtra("communityId");
        Bitmap imageBitmap = intent.getParcelableExtra("image_bitmap");

        // Set the values to the views
        tvName.setText(name);
        tvDescription.setText(description);

        if (imageBitmap != null) {
            communityPhoto.setImageBitmap(imageBitmap);
        }

        fetchuserChat();

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void checkAndShowOwnerButtons(String ownerId) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("username", "");
        String roleId = sharedPreferences.getString("roleId", "");

        if (currentUserId.isEmpty() || ownerId.isEmpty()) {
            return;
        }

        Button btnKickCommunity = findViewById(R.id.btnkickCommunity);
        Button btnEditCommunity = findViewById(R.id.btneditCommunity);
        ImageView viewGraph = findViewById(R.id.viewGraph);

        if (ownerId.equals(currentUserId) || roleId.equals("3")) {
            btnEditCommunity.setVisibility(View.VISIBLE);
            btnEditCommunity.setOnClickListener(v -> {
                Intent editIntent = new Intent(ViewCommunityDetailActivity.this, EditCommunityActivity.class);
                editIntent.putExtra("title", getIntent().getStringExtra("title"));
                editIntent.putExtra("description", getIntent().getStringExtra("description"));
                editIntent.putExtra("communityId", getIntent().getStringExtra("communityId"));
                startActivity(editIntent);
            });

            btnKickCommunity.setVisibility(View.VISIBLE);
            btnKickCommunity.setOnClickListener(v -> showKickMemberDialog());
            viewGraph.setVisibility(View.VISIBLE);
            viewGraph.setOnClickListener(v -> {
                Intent graphIntent = new Intent(ViewCommunityDetailActivity.this, ViewGraphActivity.class);
                graphIntent.putExtra("communityId", getIntent().getStringExtra("communityId"));
                startActivity(graphIntent);});
        } else {
            btnEditCommunity.setVisibility(View.GONE);
            btnKickCommunity.setVisibility(View.GONE);
            viewGraph.setVisibility(View.GONE);
        }
    }

    private void showKickMemberDialog() {
        String communityId = getIntent().getStringExtra("communityId");
        if (memberList.isEmpty()) {
            Toast.makeText(this, "No members available to kick", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_member_list, null);
        ListView listView = dialogView.findViewById(R.id.member_list_view);

        MemberAdapter adapter = new MemberAdapter(this, memberList, communityId);
        listView.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Kick Member")
                .setMessage("Select a member to remove from the community:")
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    lv.removeAllViews();
                    memberList.clear();
                    adapter.deleteSelectedMembers();
                    fetchuserChat();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void fetchuserChat() {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_get_userchat,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(this, "Error in retrieving community data", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String currentUserId = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", "");
                    if (currentUserId.isEmpty()) {
                        Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String targetCommunityId = getIntent().getStringExtra("communityId");

                    String[] communities = response.split(":");
                    for (String community : communities) {
                        if (!community.isEmpty()) {
                            String[] details = community.split(";");
                            if (details.length >= 7) {
                                String userchatCommunityId = details[2];
                                String ownerId = details[12];
                                if (userchatCommunityId.equals(targetCommunityId)) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("userchatId", details[0]);
                                    map.put("userId", details[1]);
                                    map.put("communityId", details[2]);
                                    map.put("user_name", details[6]);
                                    map.put("course", details[7]);
                                    map.put("role", details[11]);
                                    map.put("user_photo", details[10]);
                                    map.put("ownerid", ownerId);

                                    addUserchatToLayout(map);
                                    checkAndShowOwnerButtons(ownerId);
                                }
                            }
                        }
                    }
                },

                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(this, "Error retrieving community data", Toast.LENGTH_LONG).show();
                });

        queue.add(stringRequest);
    }

    private void addUserchatToLayout(final HashMap<String, String> item) {
        memberList.add(item);
        View postView = LayoutInflater.from(this).inflate(R.layout.user_item, lv, false);

        ImageView profilePhoto = postView.findViewById(R.id.profile_icon);
        TextView user_Name = postView.findViewById(R.id.post_user_name);
        TextView user_Role = postView.findViewById(R.id.post_user_role);
        TextView roleTag = postView.findViewById(R.id.role_tag);

        user_Name.setText(item.get("user_name"));
        user_Role.setText(item.get("course"));

        String photoData = item.get("user_photo");
        if (photoData != null && !photoData.isEmpty()) {
            saveBase64ToFile(photoData, file -> {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    profilePhoto.setImageBitmap(bitmap);
                } else {
                    profilePhoto.setImageResource(R.drawable.no_image);
                }
            });
        } else {
            profilePhoto.setImageResource(R.drawable.no_image);
        }

        String userid = item.get("userId");
        String ownerid = item.get("ownerid");

        if (userid != null && userid.equals(ownerid)) {
            roleTag.setText("Owner");
            roleTag.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            roleTag.setBackgroundResource(R.drawable.owner_tag_background);
        } else {
            String role = item.get("role");
            if (role != null) {
                switch (role) {
                    case "Student":
                        roleTag.setText("Active Student");
                        roleTag.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                        roleTag.setBackgroundResource(R.drawable.internship_tag_background);
                        break;
                    case "Alumni":
                        roleTag.setText("Alumni");
                        roleTag.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                        roleTag.setBackgroundResource(R.drawable.question_tag_background);
                        break;
                }
            }
        }

        lv.addView(postView);
    }

    private void saveBase64ToFile(String base64Data, ViewAccountFragment.OnFileSavedListener listener) {
        try {
            byte[] decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
            File cacheDir = getCacheDir();
            File imageFile = new File(cacheDir, "community_image.png");
            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(decodedBytes);
                fos.flush();
                listener.onFileSaved(imageFile);
            }
        } catch (Exception e) {
            Log.e("FileSaveError", "Error saving Base64 to file: " + e.getMessage());
        }
    }
}
