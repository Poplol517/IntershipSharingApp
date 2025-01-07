package mdad.localdata.intershipsharingapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private static final String url_get_message = StaffMainActivity.ipBaseAddress + "/get_all_message.php";

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

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        TextView titleTextView = view.findViewById(R.id.tvName);
        TextView descriptionTextView = view.findViewById(R.id.tvDescription);
        ImageView imageView = view.findViewById(R.id.communityPhoto);

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
        }

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, currentUserId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(false);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // Start the polling for new messages
        startPolling();

        return view;
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
                                            messages.add(new Message(userId, text, timestampStr, name));

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
}