package mdad.localdata.intershipsharingapp;


import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<Message> messages;
    private final String currentUserId;
    private final Integer roleId;
    private static final String url_delete_message = StaffMainActivity.ipBaseAddress + "/delete_message.php";

    public ChatAdapter(List<Message> messages, String currentUserId, int roleId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.roleId = roleId;
        Log.d("ChatAdapter", "Constructor called with message: " + messages);
        Log.d("ChatAdapter", "Constructor called with currentUserId: " + currentUserId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageTextView.setText(message.getText());
        holder.nameTextView.setText(message.getName());

        // Check if the message belongs to the current user to set alignment
        if (message.getUserId().equals(currentUserId)) {
            holder.messageContainer.setGravity(Gravity.END); // Align right for the whole container
            holder.nameTextView.setGravity(Gravity.END);  // Align the name to the right
            holder.messageTextView.setGravity(Gravity.START); // Align message text to the right
            holder.messageTextView.setBackgroundResource(R.drawable.message_background);
        } else {
            holder.messageContainer.setGravity(Gravity.START); // Align left for the whole container
            holder.nameTextView.setGravity(Gravity.START); // Align the name to the left
            holder.messageTextView.setGravity(Gravity.START); // Align message text to the left
            holder.messageTextView.setBackgroundResource(R.drawable.other_message_background);
        }

        // Only show the bottom sheet if the current user has roleId 3
        if (message.getUserId().equals(currentUserId) || roleId == 3) {
            holder.itemView.setOnLongClickListener(v -> {
                MessageOptionBottomSheet bottomSheet = new MessageOptionBottomSheet(
                        message.getId(),
                        currentUserId,
                        url_delete_message,
                        messageId -> {
                            // Remove the message from the list and notify the adapter
                            messages.removeIf(msg -> msg.getId().equals(messageId));
                            notifyDataSetChanged();
                        }
                );
                bottomSheet.show(((FragmentActivity) v.getContext()).getSupportFragmentManager(), bottomSheet.getTag());
                return true;
            });
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView,nameTextView;
        LinearLayout messageContainer;

        ViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }
    }
}
