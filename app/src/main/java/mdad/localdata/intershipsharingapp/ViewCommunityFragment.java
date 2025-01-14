package mdad.localdata.intershipsharingapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewCommunityFragment extends Fragment {

    private static final String url_get_communities = StaffMainActivity.ipBaseAddress + "/get_all_communities.php";
    private static final String url_get_userchat = StaffMainActivity.ipBaseAddress + "/get_all_userchat.php";
    private static final String url_create_userchat = StaffMainActivity.ipBaseAddress + "/create_userchat.php";
    private static final String url_delete_userchat = StaffMainActivity.ipBaseAddress + "/delete_userchat.php";
    private RecyclerView recyclerView;
    private boolean isJoined = true;
    private CarouselAdapter adapter;

    private List<CarouselAdapter.CarouselItem> items;
    private LinearLayout lv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_community, container, false);

        recyclerView = view.findViewById(R.id.carouselRecyclerView);
        items = new ArrayList<>();
        lv = view.findViewById(R.id.list);
        adapter = new CarouselAdapter(items, item -> {
            // When an item is clicked, navigate to the detail fragment

            // Create a new instance of the CommunityDetailFragment
            ViewSelectedCommunityFragment detailFragment =ViewSelectedCommunityFragment.newInstance(item.getTitle(), item.getDescription(), item.getChatID(), item.getImageBitmap());

            // Begin the fragment transaction
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, detailFragment); // Use your container ID
            transaction.addToBackStack(null); // Add to back stack for navigation
            transaction.commit(); // Commit the transaction
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Add snapping behavior
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        // Fetch the community data from the URL
        fetchCommunityData();


        TextView textView = view.findViewById(R.id.tvshowAll);
        textView.setOnClickListener(v -> {
            // Create a new fragment
            ViewAllCommunityFragment newFragment = new ViewAllCommunityFragment(); // Replace with the fragment you want to navigate to

            // Begin the fragment transaction
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment); // Replace with the container ID of your activity
            transaction.addToBackStack(null); // Add to the back stack to allow navigation back
            transaction.commit(); // Commit the transaction
        });
        fetchuserChat();

        Button createCommunity = view.findViewById(R.id.btncreateCommunity);
        createCommunity.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateCommunityActivity.class); // Replace with the target activity
            startActivity(intent); // Start the new activity
        });


        return view;
    }

    private void fetchCommunityData() {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_get_communities,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error in retrieving community data", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] communities = response.split(":");
                    items.clear();  // Clear the existing items before adding new ones

                    for (String community : communities) {
                        if (!community.isEmpty()) {
                            String[] details = community.split(";");
                            if (details.length >= 3) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("communityId", details[0]);
                                map.put("title", details[1]);
                                map.put("description", details[2]);
                                map.put("photo", details.length > 3 ? details[3] : "");
                                Log.d("carouselDetails", "Size: " + details.length + ", Content: " + Arrays.toString(details));
                                addCommunityToCarousel(map);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                },

                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error retrieving community data", Toast.LENGTH_LONG).show();
                });

        queue.add(stringRequest);
    }

    private void addCommunityToCarousel(final HashMap<String, String> item) {
        // Create a CarouselItem for each community and add it to the list
        String imageData = item.get("photo");
        Log.d("CommunityDetails", "Photo Data: " + item.get("photo"));
        int imageResId = R.drawable.no_image; // Set default image

        if (imageData != null && !imageData.isEmpty()) {
            saveBase64ToFile(imageData, file -> {
                // Once the image is saved, decode the file to Bitmap
                Log.d("CommunityDetails", "File Path: " + file.getAbsolutePath());
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                Log.d("CommunityDetails", "Bitmap: " + bitmap);
                if (bitmap != null) {
                    // Add new CarouselItem with the image and title
                    items.add(new CarouselAdapter.CarouselItem(bitmap, item.get("title"), item.get("description"),item.get("communityId")));
                    Log.d("CommunityDetails", "Item Added: " + item.get("communityId"));
                    getActivity().runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                    });

                } else {
                    Log.e("ImageError", "Failed to decode bitmap from file.");
                }
            });
        } else {
            // Add a default item if no image is available
            items.add(new CarouselAdapter.CarouselItem( imageResId , item.get("title"), item.get("description"),item.get("communityId")));
            getActivity().runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
            });

        }
    }

    private void fetchuserChat() {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_get_userchat,
                response -> {
                    if (response.equals("Error")) {
                        Toast.makeText(requireContext(), "Error in retrieving community data", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Get the current user's ID
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", MODE_PRIVATE);
                    String currentUserId = sharedPreferences.getString("username", ""); // Replace with your key

                    if (currentUserId.isEmpty()) {
                        Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String[] communities = response.split(":");
                    items.clear(); // Clear the existing items before adding new ones

                    for (String community : communities) {
                        if (!community.isEmpty()) {
                            String[] details = community.split(";");
                            if (details.length >= 5) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put("userchatId", details[0]);
                                map.put("userId", details[1]);
                                map.put("communityId", details[2]);
                                map.put("photo", details[3]);
                                map.put("name", details[4]);
                                map.put("description", details[5]);

                                // Only add items matching the current user's ID
                                if (details[1].equals(currentUserId)) {
                                    addUserchatToLayout(map);
                                }
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                },

                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error retrieving community data", Toast.LENGTH_LONG).show();
                });

        queue.add(stringRequest);
    }

    private void addUserchatToLayout(final HashMap<String, String> item) {
        // Add a boolean to track the join state for this item
        boolean[] isJoined = {true}; // Default state (true = joined, false = not joined)

        // Inflate the custom layout
        View postView = LayoutInflater.from(requireContext()).inflate(R.layout.joined_community_item, lv, false);

        // Set data for user info
        ImageView communityPhoto = postView.findViewById(R.id.communityPhoto);
        TextView communityName = postView.findViewById(R.id.post_community_name);
        TextView communityDescription = postView.findViewById(R.id.post_community_description);
        Button btnJoin = postView.findViewById(R.id.btnJoin);

        // Populate the fields with dynamic data
        communityName.setText(item.get("name"));
        communityDescription.setText(item.get("description"));

        // Update button state based on `isJoined`
        updateJoinButtonState(btnJoin, isJoined[0]);

        // Handle Join button click
        btnJoin.setOnClickListener(v -> {
            isJoined[0] = !isJoined[0]; // Toggle the state
            updateJoinButtonState(btnJoin, isJoined[0]);

            // Get current user ID from SharedPreferences (or another source)
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", MODE_PRIVATE);
            String userId = sharedPreferences.getString("username", ""); // Replace with your method of obtaining the user ID

            if (userId != null) {
                if (isJoined[0]) {
                    Toast.makeText(requireContext(), "Joined " + item.get("name"), Toast.LENGTH_SHORT).show();
                    joinCommunity(userId, item.get("communityId"));
                } else {
                    Toast.makeText(requireContext(), "Left " + item.get("name"), Toast.LENGTH_SHORT).show();
                    leaveCommunity(userId, item.get("communityId"));
                }
            } else {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });


        // Handle the image data
        String photoData = item.get("photo");
        Bitmap[] bitmapHolder = new Bitmap[1];
        if (photoData != null && !photoData.isEmpty()) {
            saveBase64ToFile(photoData, file -> {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                bitmapHolder[0] = bitmap;
                if (bitmap != null) {
                    communityPhoto.setImageBitmap(bitmap);
                } else {
                    communityPhoto.setImageResource(R.drawable.no_image);
                }
                // Handle the post view click event and pass the file path to the next fragment
                postView.setOnClickListener(v -> {
                    String communityId = item.get("communityId");
                    String title = item.get("name");
                    String description = item.get("description");
                    Log.d("ItemClick", "Clicked on community ID: " + communityId);
                    // Serialize Bitmap to ByteArray
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    // Navigate to ViewSelectedCommunityFragment
                    ViewSelectedCommunityFragment detailFragment = new ViewSelectedCommunityFragment();
                    // Pass data to the fragment
                    Bundle args = new Bundle();
                    args.putString("communityId", communityId);
                    args.putString("title", title);
                    args.putString("description", description);
                    args.putParcelable("image_bitmap", bitmap); // Pass image as ByteArray
                    detailFragment.setArguments(args);
                    // Begin the fragment transaction
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, detailFragment);  // Replace with the appropriate container ID
                    transaction.addToBackStack(null);  // Add to back stack for navigation
                    transaction.commit();
                });
            });
        } else {
            communityPhoto.setImageResource(R.drawable.no_image);
        }
        // Prevent the disappearing of carousel items
        fetchCommunityData();

        // Add the postView to the parent layout
        lv.addView(postView);
    }


    private void updateJoinButtonState(Button btnJoin, boolean isJoined) {
        if (isJoined) {
            btnJoin.setText("Joined");
            btnJoin.setTextColor(Color.parseColor("#000000"));
            btnJoin.setBackgroundColor(Color.parseColor("#D3D3D3")); // Update to your chosen color
        } else {
            btnJoin.setText("Join");
            btnJoin.setTextColor(Color.parseColor("#FFFFFF"));
            btnJoin.setBackgroundColor(Color.parseColor("#673AB7")); // Update to your chosen color
        }
    }

    private void joinCommunity(String userId,String communityId) {
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

    private void leaveCommunity(String userId,String communityId) {
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


    private void saveBase64ToFile(String base64Data, ViewAccountFragment.OnFileSavedListener listener) {
        try {
            // Decode the base64 data into a byte array
            byte[] decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
            Log.d("Base64Decode", "Decoded bytes length: " + decodedBytes.length);

            // Create the PNG file in the cache directory
            File cacheDir = requireContext().getCacheDir();
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
