package mdad.localdata.intershipsharingapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewSelectedCommunityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewSelectedCommunityFragment extends Fragment {
    private String lastFetchedTime = "2025-01-01 00:00:00";
    private static final String ARG_TITLE = "title";
    private static final String ARG_CHATID = "communityId";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_IMAGE_BITMAP = "image_bitmap";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private boolean isToggled = false; // Initial state
    private static final String url_get_message = StaffMainActivity.ipBaseAddress + "/get_all_message.php";
    private static final String url_get_userchat = StaffMainActivity.ipBaseAddress + "/get_all_userchat.php";
    private static final String url_create_message = StaffMainActivity.ipBaseAddress + "/create_message.php";
    private static final String url_create_userchat = StaffMainActivity.ipBaseAddress + "/create_userchat.php";
    private static final String url_delete_userchat = StaffMainActivity.ipBaseAddress + "/delete_userchat.php";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    private SharedPreferences sharedPreferences;

    public ViewSelectedCommunityFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters
     * @return A new instance of fragment ViewSelectedCommunityFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewSelectedCommunityFragment newInstance(String title,String description, String communityId, Bitmap imageBitmap) {
        ViewSelectedCommunityFragment fragment = new ViewSelectedCommunityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CHATID, communityId);
        Log.d("CommunityDetails", "Community ID: " + communityId);
        args.putString(ARG_DESCRIPTION, description);
        args.putParcelable(ARG_IMAGE_BITMAP, imageBitmap); // Passing Bitmap
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_selected_community, container, false);

        LinearLayout communityDescription = view.findViewById(R.id.communityDetails);
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        TextView titleTextView = view.findViewById(R.id.tvName);
        TextView descriptionTextView = view.findViewById(R.id.tvDescription);
        ImageView imageView = view.findViewById(R.id.communityPhoto);
        EditText editText = view.findViewById(R.id.editMessage);
        Button sendButton = view.findViewById(R.id.btnSendMessage);
        Button joinButton = view.findViewById(R.id.btnJoin);

        sharedPreferences = getActivity().getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("username", "");

        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString(ARG_TITLE);
            String description = args.getString(ARG_DESCRIPTION);
            Bitmap imageBitmap = args.getParcelable(ARG_IMAGE_BITMAP);

            titleTextView.setText(title);
            descriptionTextView.setText(description);

            if (imageBitmap != null) {
                imageView.setImageBitmap(imageBitmap);
            }
            String communityId = args.getString(ARG_CHATID);
            if (communityId != null && !communityId.isEmpty()) {
                // Use checkUserChatStatus to set the initial state of the button
                checkUserChatStatus(currentUserId, communityId, joinButton);
            } else {
                Log.e("CommunityIDError", "Community ID is missing.");
            }
        }

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, currentUserId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(false);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        communityDescription.setOnClickListener(v -> {
            ViewCommunityDetailFragment newFragment = new ViewCommunityDetailFragment();
            newFragment.setArguments(getArguments());

            // Begin the fragment transaction
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment); // Replace with the container ID of your activity
            transaction.addToBackStack(null); // Add to the back stack to allow navigation back
            transaction.commit();
        });

        sendButton.setOnClickListener(v -> {
            String messageText = editText.getText().toString().trim();
            if (!messageText.isEmpty()) {
                String communityId = getArguments() != null ? getArguments().getString(ARG_CHATID) : null;
                if (communityId != null && !communityId.isEmpty()) {
                    createMessage(currentUserId, communityId, messageText);
                    editText.setText(""); // Clear the message input field
                } else {
                    Toast.makeText(getContext(), "Community ID is missing", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });


        joinButton.setOnClickListener(v -> {
            isToggled = !isToggled; // Toggle the state
            if (isToggled) {
                joinButton.setText("Joined");
                joinButton.setTextColor(Color.parseColor("#000000"));
                joinButton.setBackgroundColor(Color.parseColor("#D3D3D3"));
                String communityId = getArguments() != null ? getArguments().getString(ARG_CHATID) : null;
                if (communityId != null && !communityId.isEmpty()) {
                    createUserchat(currentUserId, communityId);
                } else {
                    Toast.makeText(getContext(), "Community ID is missing", Toast.LENGTH_SHORT).show();
                }
            } else {
                joinButton.setText("Join");
                joinButton.setTextColor(Color.parseColor("#FFFFFF"));
                joinButton.setBackgroundColor(Color.parseColor("#673AB7"));
                String communityId = getArguments() != null ? getArguments().getString(ARG_CHATID) : null;
                if (communityId != null && !communityId.isEmpty()) {
                    deleteUserchat(currentUserId, communityId);
                } else {
                    Toast.makeText(getContext(), "Community ID is missing", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Start the polling for new messages
        startPolling();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
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
        if (getContext() != null) {
            // Retrieve the communityId passed to the fragment
            String communityId = getArguments() != null ? getArguments().getString(ARG_CHATID) : null;

            if (communityId == null || communityId.isEmpty()) {
                Log.e("FetchMessagesError", "Community ID is null or empty.");
                return;
            }

            // API endpoint for fetching messages
            String url = url_get_message;

            // Define the DateTimeFormatter for parsing the timestamp
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Use Volley to make the POST request
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        Log.d("VolleyResponse", response);

                        if (response.trim().equals("Error")) {
                            Toast.makeText(getContext(), "Error in retrieving messages", Toast.LENGTH_LONG).show();
                            return;
                        }

                        try {
                            // Process the response and parse the messages
                            String[] messageData = response.split("/");  // Split each message
                            messages.clear(); // Clear old messages

                            for (String messageString : messageData) {
                                if (!messageString.isEmpty()) {
                                    String[] messageDetails = messageString.split(";");

                                    // Ensure there are at least 6 details (chatId, userId, communityId, text, timestamp, name)
                                    if (messageDetails.length >= 6) {
                                        String messageId = messageDetails[0];
                                        String chatId = messageDetails[2];
                                        String userId = messageDetails[1];  // userId
                                        String text = messageDetails[3];    // text
                                        String timestampStr = messageDetails[4]; // timestamp
                                        String name = messageDetails[5];     // name

                                        // Display the message only if chatId matches the current communityId
                                        if (chatId.equals(communityId)) {
                                            // Parse the timestamp string to LocalDateTime
                                            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);

                                            // Add the message with the parsed timestamp
                                            messages.add(new Message(messageId,userId, text, timestampStr, name));

                                            // Update last fetched time to the latest message timestamp
                                            if (timestamp.isAfter(LocalDateTime.parse(lastFetchedTime, formatter))) {
                                                lastFetchedTime = timestampStr;
                                            }
                                        }
                                    } else {
                                        Log.e("MessageError", "Invalid message format: " + messageString);
                                    }
                                }
                            }

                            // Sort the messages by timestamp (oldest to newest)
                            messages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));

                            // Notify the adapter that the data has changed
                            chatAdapter.notifyDataSetChanged();

                        } catch (Exception e) {
                            Log.e("ParseError", "Error parsing message response: " + e.getMessage());
                        }
                    },
                    error -> {
                        Log.e("VolleyError", "Error retrieving messages: " + error.getMessage());
                        Toast.makeText(getContext(), "Error retrieving messages", Toast.LENGTH_LONG).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Add POST parameters to the request
                    Map<String, String> params = new HashMap<>();
                    params.put("last_fetched_time", lastFetchedTime);
                    params.put("chat_id", communityId); // Pass the communityId to the server
                    return params;
                }
            };

            // Add the request to the Volley request queue
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            queue.add(stringRequest);
        } else {
            Log.w("FragmentError", "Fragment is not attached to a context, skipping fetchMessages call.");
        }
    }

    private void checkUserChatStatus(String userId, String communityId, Button joinButton) {
        if (getContext() != null) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url_get_userchat,
                    response -> {
                        Log.d("CheckUserChatResponse", response);

                        if (!response.trim().equals("Error")) {
                            try {
                                // Response is a list of user-community relationships
                                boolean isMember = false;
                                String[] userChatData = response.split(":");

                                for (String userChat : userChatData) {
                                    if (!userChat.isEmpty()) {
                                        String[] details = userChat.split(";");

                                        // Ensure there are two details (userId and chatId)
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

                                // Update the button state based on membership
                                if (isMember) {
                                    isToggled = true; // User is a member
                                    joinButton.setText("Joined");
                                    joinButton.setTextColor(Color.parseColor("#000000"));
                                    joinButton.setBackgroundColor(Color.parseColor("#D3D3D3"));
                                } else {
                                    isToggled = false; // User is not a member
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
                        Toast.makeText(getContext(), "Error checking user chat status", Toast.LENGTH_LONG).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Add POST parameters to the request
                    Map<String, String> params = new HashMap<>();
                    params.put("userId", userId);
                    params.put("chatId", communityId);
                    return params;
                }
            };

            // Add the request to the Volley request queue
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            queue.add(stringRequest);
        } else {
            Log.w("FragmentError", "Fragment is not attached to a context, skipping checkUserChatStatus call.");
        }
    }

    // Add this method in your ViewSelectedCommunityFragment class
    private void createMessage(String userId, String communityId, String messageText) {
        if (getContext() != null) {
            // API endpoint for creating a message

            // Use Volley to make the POST request
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url_create_message,
                    response -> {
                        Log.d("CreateMessageResponse", response);

                        if (response.trim().equals("Error")) {
                            Toast.makeText(getContext(), "Error sending message", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Message sent successfully", Toast.LENGTH_SHORT).show();
                            // Optionally, refresh messages after sending
                            fetchMessages();
                        }
                    },
                    error -> {
                        Log.e("VolleyError", "Error sending message: " + error.getMessage());
                        Toast.makeText(getContext(), "Error sending message", Toast.LENGTH_LONG).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Add POST parameters to the request
                    Map<String, String> params = new HashMap<>();
                    params.put("userId", userId);
                    params.put("chatId", communityId);
                    params.put("description", messageText);
                    return params;
                }
            };

            // Add the request to the Volley request queue
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            queue.add(stringRequest);
        } else {
            Log.w("FragmentError", "Fragment is not attached to a context, skipping createMessage call.");
        }
    }

    private void createUserchat(String userId, String communityId) {
        if (getContext() != null) {
            // API endpoint for creating a message

            // Use Volley to make the POST request
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url_create_userchat,
                    response -> {
                        Log.d("CreateUserchatResponse", response);

                        if (response.trim().equals("Error")) {
                            Toast.makeText(getContext(), "Error joining this community", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "You have succesfully joined this community", Toast.LENGTH_SHORT).show();
                            // Optionally, refresh messages after sending
                            fetchMessages();
                        }
                    },
                    error -> {
                        Log.e("VolleyError", "Error joining community: " + error.getMessage());
                        Toast.makeText(getContext(), "Error joining coummnity", Toast.LENGTH_LONG).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Add POST parameters to the request
                    Map<String, String> params = new HashMap<>();
                    params.put("userId", userId);
                    params.put("chatId", communityId);
                    return params;
                }
            };

            // Add the request to the Volley request queue
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            queue.add(stringRequest);
        } else {
            Log.w("FragmentError", "Fragment is not attached to a context, skipping createMessage call.");
        }
    }
    private void deleteUserchat(String userId, String communityId) {
        if (getContext() != null) {
            // API endpoint for creating a message

            // Use Volley to make the POST request
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url_delete_userchat,
                    response -> {
                        Log.d("CreateUserchatResponse", response);

                        if (response.trim().equals("Error")) {
                            Toast.makeText(getContext(), "Error leaving this community", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "You have succesfully leave this community", Toast.LENGTH_SHORT).show();
                            // Optionally, refresh messages after sending
                            fetchMessages();
                        }
                    },
                    error -> {
                        Log.e("VolleyError", "Error leaving community: " + error.getMessage());
                        Toast.makeText(getContext(), "Error leaving coummnity", Toast.LENGTH_LONG).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Add POST parameters to the request
                    Map<String, String> params = new HashMap<>();
                    params.put("userId", userId);
                    params.put("chatId", communityId);
                    return params;
                }
            };

            // Add the request to the Volley request queue
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            queue.add(stringRequest);
        } else {
            Log.w("FragmentError", "Fragment is not attached to a context, skipping createMessage call.");
        }
    }
}