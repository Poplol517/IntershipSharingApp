package mdad.localdata.intershipsharingapp;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashMap;
import java.util.Map;

public class MessageOptionBottomSheet extends BottomSheetDialogFragment {
    private static final String url_edit_message = StaffMainActivity.ipBaseAddress + "/edit_message.php";
    private String messageId;
    private String userId;
    private String urlDeleteMessage;
    private OnMessageDeletedListener listener;
    private LinearLayout optionsPage;
    private LinearLayout editMessagePage;
    private EditText editMessageInput;
    private Button saveEditedMessageButton;

    public interface OnMessageDeletedListener {
        void onMessageDeleted(String messageId);
    }

    public MessageOptionBottomSheet(String messageId, String userId, String urlDeleteMessage, OnMessageDeletedListener listener) {
        this.messageId = messageId;
        this.userId = userId;
        this.urlDeleteMessage = urlDeleteMessage;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setContentView(R.layout.message_option_menu);
        optionsPage = dialog.findViewById(R.id.optionsPage);
        Button deleteButton = dialog.findViewById(R.id.btn_delete_message);
        Button editButton = dialog.findViewById(R.id.btn_edit_message);
        editMessagePage = dialog.findViewById(R.id.editMessagePage);
        editMessageInput = dialog.findViewById(R.id.edit_message_input);
        saveEditedMessageButton = dialog.findViewById(R.id.save_edited_message);

        deleteButton.setOnClickListener(v -> deleteMessage());
        editButton.setOnClickListener(v -> showEditPage());
        saveEditedMessageButton.setOnClickListener(v -> saveEditedMessage());

        return dialog;
    }
    private void showEditPage() {
        // Hide options page, show edit message page
        optionsPage.setVisibility(View.GONE);
        editMessagePage.setVisibility(View.VISIBLE);

        // Optionally, populate the EditText with the existing message text if available
        // editMessageInput.setText(existingMessageText);

    }

    private void saveEditedMessage() {
        // Get the edited message input
        String editedMessage = editMessageInput.getText().toString().trim();

        if (editedMessage.isEmpty()) {
            Log.e("EditError", "Edited message cannot be empty");
            return;
        }

        // Create a map of parameters for the request
        Map<String, String> params = new HashMap<>();
        params.put("messageId", messageId); // the ID of the message to be updated
        params.put("description", editedMessage); // the new message text

        // Create the StringRequest for updating the message
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url_edit_message,
                response -> {
                    Log.d("Reponese",response);// Handle the server response
                    if (response.trim().equals("success")) {
                        Log.d("EditSuccess", "Message updated successfully");
                        // Optionally, you can notify the adapter or the activity to update the message list
                        // In this case, you might want to trigger a callback or refresh the message list
                        dismiss(); // Close the dialog after the update
                    } else {
                        Log.e("EditError", "Failed to update message: " + response);
                    }
                },
                error -> Log.e("VolleyError", "Error updating message: " + error.getMessage())) {

            @Override
            protected Map<String, String> getParams() {
                // Return the parameters to be sent in the POST request body
                return params;
            }
        };

        // Add the request to the request queue to be sent
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(stringRequest);
    }

    private void deleteMessage() {
        if (getContext() != null) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, urlDeleteMessage,
                    response -> {
                        if (response.trim().equals("success")) {
                            if (listener != null) {
                                listener.onMessageDeleted(messageId);
                            }
                            dismiss();
                        } else {
                            Log.e("DeleteError", "Failed to delete message: " + response);
                        }
                    },
                    error -> Log.e("VolleyError", "Error deleting message: " + error.getMessage())) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("messageId", messageId);
                    params.put("userId", userId);
                    return params;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(getContext());
            queue.add(stringRequest);
        }
    }

}
