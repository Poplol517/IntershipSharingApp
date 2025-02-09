package mdad.localdata.intershipsharingapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewCommunityDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewCommunityDetailFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private List<HashMap<String, String>> memberList = new ArrayList<>();
    private static final String url_get_userchat = StaffMainActivity.ipBaseAddress + "/get_all_userchat.php";
    private LinearLayout lv;

    private String mParam1;
    private String mParam2;

    public ViewCommunityDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewCommunityDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewCommunityDetailFragment newInstance(String param1, String param2) {
        ViewCommunityDetailFragment fragment = new ViewCommunityDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_community_detail, container, false);

        // Find views by their IDs
        ImageView communityPhoto = view.findViewById(R.id.communityPhoto);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        lv = view.findViewById(R.id.list);
        // Get arguments passed from the previous fragment
        if (getArguments() != null) {
            String name = getArguments().getString("title", "Default Name");
            String description = getArguments().getString("description", "Default Description");
            String communityId = getArguments().getString("communityId", "Default ID");
            Bitmap imageBitmap = getArguments().getParcelable("image_bitmap");

            // Set the values to the views
            tvName.setText(name);
            tvDescription.setText(description);

            if (imageBitmap != null) {
                communityPhoto.setImageBitmap(imageBitmap);
            }
        }
        fetchuserChat();
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

    @Override
    public void onResume() {
        super.onResume();
        fetchuserChat(); // Refresh the user chat data
    }

    private void checkAndShowOwnerButtons(String ownerId) {
        // Get the current user's ID
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("username", ""); // Replace with your key
        String roleId=sharedPreferences.getString("roleId","");
        // Ensure the currentUserId and ownerId are both valid before comparing
        if (currentUserId.isEmpty() || ownerId.isEmpty()) {
            return;
        }

        // Get references to the buttons
        View view = getView();  // Get the fragment's view
        if (view == null) return; // Ensure view is not null
        // Get references to the buttons
        Button btnKickCommunity = getView().findViewById(R.id.btnkickCommunity);
        Button btnEditCommunity = view.findViewById(R.id.btneditCommunity);
        ImageView viewGraph = view.findViewById(R.id.viewGraph);

        // If the current user is the owner, make the buttons visible
        if (ownerId.equals(currentUserId)||roleId.equals("3")) {
            btnEditCommunity.setVisibility(View.VISIBLE);
            btnEditCommunity.setOnClickListener(v -> {
                // Get the arguments passed to the fragment
                if (getArguments() != null) {
                    String name = getArguments().getString("title", "Default Name");
                    String description = getArguments().getString("description", "Default Description");
                    String communityId = getArguments().getString("communityId", "Default ID");

                    // Create a new instance of EditCommunityFragment
                    EditCommunityFragment editCommunityFragment = new EditCommunityFragment();

                    // Pass the data to the EditCommunityFragment using Bundle
                    Bundle bundle = new Bundle();
                    bundle.putString("title", name);
                    bundle.putString("description", description);
                    bundle.putString("communityId", communityId);
                    editCommunityFragment.setArguments(bundle);

                    // Begin the fragment transaction
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, editCommunityFragment) // Replace with your fragment container ID
                            .addToBackStack(null) // Add to back stack to enable back navigation
                            .commit(); // Commit the transaction
                }
            });

            btnKickCommunity.setVisibility(View.VISIBLE); // Make the button visible for the owner
            btnKickCommunity.setOnClickListener(v -> showKickMemberDialog());
            viewGraph.setVisibility(View.VISIBLE);
            viewGraph.setVisibility(View.VISIBLE);
            viewGraph.setOnClickListener(v -> {
                ViewGraphFragment fragment = new ViewGraphFragment();

                // Get the communityId from the arguments
                String communityId = getArguments().getString("communityId", "Default ID");

                // Pass the communityId to the ViewGraphFragment
                Bundle bundle = new Bundle();
                bundle.putString("ownerId",ownerId);
                bundle.putString("communityId", communityId);
                fragment.setArguments(bundle);

                // Use the FragmentManager to begin a transaction
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

                // Replace the existing container with the new fragment
                transaction.replace(R.id.fragment_container, fragment);

                // Add the transaction to the back stack, so the user can navigate back
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            });
        }
        else {
            // Otherwise, keep the buttons hidden
            btnEditCommunity.setVisibility(View.GONE);
            btnKickCommunity.setVisibility(View.GONE); // Hide the button for non-owners
            viewGraph.setVisibility(View.GONE);
        }
    }

    private void showKickMemberDialog() {
        String communityId = getArguments().getString("communityId", "Default ID");
        // Check if there are members to display
        if (memberList.isEmpty()) {
            Toast.makeText(requireContext(), "No members available to kick", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an AlertDialog
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_member_list, null);

        // Initialize the ListView from the dialog's layout
        ListView listView = dialogView.findViewById(R.id.member_list_view);

        // Create the adapter and set it to the ListView
        MemberAdapter adapter = new MemberAdapter(requireContext(), memberList, communityId); // Pass the chatId to the adapter
        listView.setAdapter(adapter);

        // Create the AlertDialog and set its custom view
        new AlertDialog.Builder(requireContext())
                .setTitle("Kick Member")
                .setMessage("Select a member to remove from the community:")
                .setView(dialogView)  // Set the custom view with the ListView
                .setPositiveButton("Confirm", (dialog, which) -> {
                    lv.removeAllViews();
                    memberList.clear();
                    // Call the deleteSelectedMembers method to process the removal
                    adapter.deleteSelectedMembers(); // Trigger the deletion request in the adapter
                    fetchuserChat();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Dismiss the dialog
                    dialog.dismiss();
                })
                .create()
                .show();
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

                    // Retrieve the communityId from the arguments
                    String targetCommunityId = getArguments() != null ? getArguments().getString("communityId", "") : "";

                    String[] communities = response.split(":");

                    for (String community : communities) {
                        if (!community.isEmpty()) {
                            String[] details = community.split(";");
                            if (details.length >= 7) {
                                // Extract the communityId and ownerId from the response
                                String userchatCommunityId = details[2];
                                String ownerId = details[12]; // The ownerId is in index 12 of the response

                                // Check if the communityId matches the target communityId
                                if (userchatCommunityId.equals(targetCommunityId)) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("userchatId", details[0]);
                                    map.put("userId", details[1]);
                                    map.put("communityId", details[2]);
                                    map.put("user_name", details[6]);
                                    map.put("course", details[7]);
                                    map.put("role", details[11]);
                                    map.put("user_photo", details[10]);
                                    map.put("ownerid", ownerId); // Store the ownerId

                                    addUserchatToLayout(map);

                                    // Now that we have the ownerId, check if the logged-in user is the owner
                                    checkAndShowOwnerButtons(ownerId);
                                }
                            }
                        }
                    }
                },

                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(requireContext(), "Error retrieving community data", Toast.LENGTH_LONG).show();
                });

        queue.add(stringRequest);
    }

    private void addUserchatToLayout(final HashMap<String, String> item) {
        Log.d("UserChatDetails", "Item: " + item);

        // Add the new item to the memberList for later use in the dialog
        memberList.add(item);

        // Inflate the custom layout for displaying user chat info
        View postView = LayoutInflater.from(requireContext()).inflate(R.layout.user_item, lv, false);

        // Set data for user info
        ImageView profilePhoto = postView.findViewById(R.id.profile_icon);
        TextView user_Name = postView.findViewById(R.id.post_user_name);
        TextView user_Role = postView.findViewById(R.id.post_user_role);
        TextView roleTag = postView.findViewById(R.id.role_tag);

        // Populate the fields with dynamic data
        user_Name.setText(item.get("user_name"));
        user_Role.setText(item.get("course"));

        // Handle the image data for profile photo
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

        // Get details[1] and details[12] from the item
        String userid = item.get("userId");
        String ownerid = item.get("ownerid");

        // Check if details[1] matches details[12]
        if (userid != null && userid.equals(ownerid)) {
            Log.d("UserChatDetails", "User is the owner based on details[1] and details[12]");
            // If they match, display the "Owner" tag
            roleTag.setText("Owner");
            roleTag.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            roleTag.setBackgroundResource(R.drawable.owner_tag_background);
        } else {
            // Otherwise, assign role tags based on the role in the item
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

        // Add the postView to the parent layout (list of users)
        lv.addView(postView);
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