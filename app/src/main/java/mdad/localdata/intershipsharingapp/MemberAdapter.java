package mdad.localdata.intershipsharingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

public class MemberAdapter extends ArrayAdapter<HashMap<String, String>> {

    private final Context context;
    private final List<HashMap<String, String>> memberList;

    public MemberAdapter(Context context, List<HashMap<String, String>> members) {
        super(context, R.layout.dialog_member_list_item, members);
        this.context = context;
        this.memberList = members;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.dialog_member_list_item, parent, false);
        }

        // Get member data
        HashMap<String, String> member = memberList.get(position);

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

