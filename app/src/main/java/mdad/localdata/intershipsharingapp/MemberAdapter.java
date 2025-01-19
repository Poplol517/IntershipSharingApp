package mdad.localdata.intershipsharingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MemberAdapter extends ArrayAdapter<HashMap<String, String>> {

    private final Context context;
    private final List<HashMap<String, String>> memberList;
    private final HashMap<Integer, HashMap<String, String>> selectedMembers = new HashMap<>();
    private static final String url_delete_userchat = StaffMainActivity.ipBaseAddress + "/delete_user_chat.php";
    private final String chatId; // Assuming chatId is passed from the calling activity

    public MemberAdapter(Context context, List<HashMap<String, String>> members, String chatId) {
        super(context, R.layout.dialog_member_list_item, members);
        this.context = context;
        this.memberList = members;
        this.chatId = chatId; // Set the chatId for use in delete request
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.dialog_member_list_item, parent, false);
        }

        // Get member data
        HashMap<String, String> member = memberList.get(position);

        // Retrieve user ID and chat ID
        String userId = member.get("userId");
        String userchatId = member.get("userchatId");

        // Log or process the user and chat IDs
        Log.d("MemberAdapter", "User ID: " + userId + ", Chat ID: " + userchatId);

        CheckBox checkBox = convertView.findViewById(R.id.member_checkbox);
        checkBox.setOnCheckedChangeListener(null); // Avoid triggering on recycled views
        checkBox.setChecked(selectedMembers.get(position) != null);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // When checked, add userId and userchatId to selectedMembers map
                HashMap<String, String> selectedMember = new HashMap<>();
                selectedMember.put("userId", userId);
                selectedMember.put("userchatId", userchatId);
                selectedMembers.put(position, selectedMember);

                // Log the user details if the checkbox is checked
                String userName = member.get("user_name");
                String role = member.get("role");
                String userRoleText = member.get("course");
                Log.d("UserDetails", "User Name: " + userName + ", Role: " + role + ", Course: " + userRoleText);
            } else {
                // When unchecked, remove from selectedMembers map
                selectedMembers.remove(position);
            }

            // Log the current state of selectedMembers list
            logSelectedMembers();
        });

        // Bind user info to the views
        TextView roleTag = convertView.findViewById(R.id.role_tag);
        ImageView profileIcon = convertView.findViewById(R.id.profile_icon);
        TextView userName = convertView.findViewById(R.id.post_user_name);
        TextView userRole = convertView.findViewById(R.id.post_user_role);

        // Set the role tag (e.g., "Active Student")
        String role = member.get("role");
        if (role != null) {
            roleTag.setText(role);
        }

        // Set the user's name
        String name = member.get("user_name");
        if (name != null) {
            userName.setText(name);
        }

        // Set the user's role/position (e.g., "Google LLC | Year 3 Student in Computer Engineering")
        String userRoleText = member.get("course");
        if (userRoleText != null) {
            userRole.setText(userRoleText);
        }

        // Handle profile image
        String photoData = member.get("user_photo");
        if (photoData != null && !photoData.isEmpty()) {
            saveBase64ToFile(photoData, file -> {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    profileIcon.setImageBitmap(bitmap);
                } else {
                    profileIcon.setImageResource(R.drawable.account);  // Default image
                }
            });
        } else {
            profileIcon.setImageResource(R.drawable.account);  // Default image
        }

        return convertView;
    }

    private void logSelectedMembers() {
        Log.d("SelectedMembers", "Current Selected Members: " + selectedMembers.toString());
    }

    // Function to send the selected members' data to PHP script for deletion
    public void deleteSelectedMembers() {
        // Extract userIds from selectedMembers
        StringBuilder userIdsBuilder = new StringBuilder();
        for (HashMap<String, String> selectedMember : selectedMembers.values()) {
            String userId = selectedMember.get("userId");
            if (userIdsBuilder.length() > 0) {
                userIdsBuilder.append(",");
            }
            userIdsBuilder.append(userId);
        }

        String userIdsString = userIdsBuilder.toString();

        // Send the data to the PHP script (using HttpURLConnection)
        new Thread(() -> {
            try {
                URL url = new URL(url_delete_userchat);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Prepare the data to send
                String postData = "chatId=" + URLEncoder.encode(chatId, "UTF-8") +
                        "&userIds=" + URLEncoder.encode(userIdsString, "UTF-8");

                // Send the data
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(postData.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                // Get the response
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Handle success
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    String response = responseBuilder.toString();
                    // Handle the response
                    Log.d("PHP Response", response);
                } else {
                    // Handle error
                    Log.e("HTTP Error", "Error: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("Network Error", "Error sending request: " + e.getMessage());
            }
        }).start();
    }

    private void saveBase64ToFile(String base64Data, ViewAccountFragment.OnFileSavedListener listener) {
        try {
            // Decode the base64 data into a byte array
            byte[] decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
            Log.d("Base64Decode", "Decoded bytes length: " + decodedBytes.length);

            // Create the PNG file in the cache directory
            File cacheDir = context.getCacheDir();
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
