package mdad.localdata.intershipsharingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewSelectedCommunityActivity extends AppCompatActivity {
    private String lastFetchedTime = "2025-01-01 00:00:00";
    private static final String ARG_TITLE = "title";
    private static final String ARG_CHATID = "communityId";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_IMAGE_BITMAP = "image_bitmap";
    private boolean isToggled = false; // Initial state
    private static final String url_get_message = StaffMainActivity.ipBaseAddress + "/get_all_message.php";
    private static final String url_get_userchat = StaffMainActivity.ipBaseAddress + "/get_all_userchat.php";
    private static final String url_create_message = StaffMainActivity.ipBaseAddress + "/create_message.php";
    private static final String url_create_userchat = StaffMainActivity.ipBaseAddress + "/create_userchat.php";
    private static final String url_delete_userchat = StaffMainActivity.ipBaseAddress + "/delete_userchat.php";

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_selected_community);  // Use the corresponding layout file for Activity

        LinearLayout communityDescription = findViewById(R.id.communityDetails);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        TextView titleTextView = findViewById(R.id.tvName);
        TextView descriptionTextView = findViewById(R.id.tvDescription);
        ImageView imageView = findViewById(R.id.communityPhoto);
        EditText editText = findViewById(R.id.editMessage);
        Button sendButton = findViewById(R.id.btnSendMessage);
        Button joinButton = findViewById(R.id.btnJoin);

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("username", "");

        Bundle args = getIntent().getExtras();
        if (args != null) {
            String title = args.getString(ARG_TITLE);
            String description = args.getString(ARG_DESCRIPTION);
            titleTextView.setText(title);
            descriptionTextView.setText(description);

            // First, try to load the image from the provided file path
            String imagePath = getIntent().getStringExtra("image_path");
            if (imagePath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.no_image);
                }
            } else {
                // If image_path is not available, fall back to the bitmap from the intent
                Bitmap imageBitmap = args.getParcelable(ARG_IMAGE_BITMAP);
                if (imageBitmap != null) {
                    imageView.setImageBitmap(imageBitmap);
                } else {
                    imageView.setImageResource(R.drawable.no_image); // Set default image
                }
            }}
        messages = new ArrayList<>();
        int roleId=3;
        chatAdapter = new ChatAdapter(messages, currentUserId,roleId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(false);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        communityDescription.setOnClickListener(v -> {
            // Replace with fragment transaction if necessary, or open a new activity
            Intent intent = new Intent(ViewSelectedCommunityActivity.this, ViewCommunityDetailActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
        });

        sendButton.setOnClickListener(v -> {
            String messageText = editText.getText().toString().trim();
            if (!messageText.isEmpty()) {
                String communityId = getIntent().getStringExtra(ARG_CHATID);
                if (communityId != null && !communityId.isEmpty()) {
                    createMessage(currentUserId, communityId, messageText);
                    editText.setText(""); // Clear the message input field
                } else {
                    Toast.makeText(this, "Community ID is missing", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        joinButton.setOnClickListener(v -> {
            isToggled = !isToggled; // Toggle the state
            if (isToggled) {
                joinButton.setText("Joined");
                joinButton.setTextColor(Color.parseColor("#000000"));
                joinButton.setBackgroundColor(Color.parseColor("#D3D3D3"));
                String communityId = getIntent().getStringExtra(ARG_CHATID);
                if (communityId != null && !communityId.isEmpty()) {
                    createUserchat(currentUserId, communityId);
                } else {
                    Toast.makeText(this, "Community ID is missing", Toast.LENGTH_SHORT).show();
                }
            } else {
                joinButton.setText("Join");
                joinButton.setTextColor(Color.parseColor("#FFFFFF"));
                joinButton.setBackgroundColor(Color.parseColor("#673AB7"));
                String communityId = getIntent().getStringExtra(ARG_CHATID);
                if (communityId != null && !communityId.isEmpty()) {
                    deleteUserchat(currentUserId, communityId);
                } else {
                    Toast.makeText(this, "Community ID is missing", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Start the polling for new messages
        startPolling();
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

    private void startPolling() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Fetch new messages
                fetchMessages();
                // Schedule next poll in 5 seconds
                handler.postDelayed(this, 1000);  // Poll every 5 seconds
            }
        };
        handler.post(runnable);  // Start the polling immediately
    }

    private void fetchMessages() {
        String communityId = getIntent().getStringExtra(ARG_CHATID);

        if (communityId == null || communityId.isEmpty()) {
            Log.e("FetchMessagesError", "Community ID is null or empty.");
            return;
        }

        String url = url_get_message;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("VolleyResponse", response);

                    if (response.trim().equals("Error")) {
                        return;
                    }

                    try {
                        String[] messageData = response.split("/");  // Split each message
                        messages.clear(); // Clear old messages

                        for (String messageString : messageData) {
                            if (!messageString.isEmpty()) {
                                String[] messageDetails = messageString.split(";");

                                if (messageDetails.length >= 6) {
                                    String messageId = messageDetails[0];
                                    String chatId = messageDetails[2];
                                    String userId = messageDetails[1];  // userId
                                    String text = messageDetails[3];    // text
                                    String timestampStr = messageDetails[4]; // timestamp
                                    String name = messageDetails[5];     // name

                                    if (chatId.equals(communityId)) {
                                        LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);
                                        messages.add(new Message(messageId,userId, text, timestampStr, name));

                                        if (timestamp.isAfter(LocalDateTime.parse(lastFetchedTime, formatter))) {
                                            lastFetchedTime = timestampStr;
                                        }
                                    }
                                } else {
                                    Log.e("MessageError", "Invalid message format: " + messageString);
                                }
                            }
                        }

                        messages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
                        chatAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.e("ParseError", "Error parsing message response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error retrieving messages: " + error.getMessage());
                    Toast.makeText(this, "Error retrieving messages", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("last_fetched_time", lastFetchedTime);
                params.put("chat_id", communityId);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void checkUserChatStatus(String userId, String communityId, Button joinButton) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_get_userchat,
                response -> {
                    Log.d("CheckUserChatResponse", response);

                    if (!response.trim().equals("Error")) {
                        try {
                            boolean isMember = false;
                            String[] userChatData = response.split(":");

                            for (String userChat : userChatData) {
                                if (!userChat.isEmpty()) {
                                    String[] details = userChat.split(";");

                                    if (details.length >= 3) {
                                        String fetchedUserId = details[1];
                                        String fetchedChatId = details[2];

                                        if (fetchedUserId.equals(userId) && fetchedChatId.equals(communityId)) {
                                            isMember = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (isMember) {
                                isToggled = true;
                                joinButton.setText("Joined");
                                joinButton.setTextColor(Color.parseColor("#000000"));
                                joinButton.setBackgroundColor(Color.parseColor("#D3D3D3"));
                            } else {
                                isToggled = false;
                                joinButton.setText("Join");
                                joinButton.setTextColor(Color.parseColor("#FFFFFF"));
                                joinButton.setBackgroundColor(Color.parseColor("#673AB7"));
                            }

                        } catch (Exception e) {
                            Log.e("ParseError", "Error parsing user chat response: " + e.getMessage());
                        }
                    } else {
                        Log.e("CheckUserChatError", "Error in response: User not found in community.");
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error checking user chat status: " + error.getMessage());
                    Toast.makeText(this, "Error checking user chat status", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId", userId);
                params.put("chatId", communityId);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void createMessage(String userId, String communityId, String messageText) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_create_message,
                response -> {
                    Log.d("CreateMessageResponse", response);

                    if (response.trim().equals("Error")) {
                        Toast.makeText(this, "Error sending message", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                        fetchMessages();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error sending message: " + error.getMessage());
                    Toast.makeText(this, "Error sending message", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId", userId);
                params.put("chatId", communityId);
                params.put("description", messageText);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void createUserchat(String userId, String communityId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_create_userchat,
                response -> {
                    Log.d("CreateUserchatResponse", response);

                    if (response.trim().equals("Error")) {
                        Toast.makeText(this, "Error joining this community", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "You have successfully joined this community", Toast.LENGTH_SHORT).show();
                        fetchMessages();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error joining community: " + error.getMessage());
                    Toast.makeText(this, "Error joining community", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId", userId);
                params.put("chatId", communityId);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void deleteUserchat(String userId, String communityId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_delete_userchat,
                response -> {
                    Log.d("DeleteUserchatResponse", response);

                    if (response.trim().equals("Error")) {
                        Toast.makeText(this, "Error leaving this community", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "You have successfully left this community", Toast.LENGTH_SHORT).show();
                        fetchMessages();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error leaving community: " + error.getMessage());
                    Toast.makeText(this, "Error leaving community", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId", userId);
                params.put("chatId", communityId);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}
