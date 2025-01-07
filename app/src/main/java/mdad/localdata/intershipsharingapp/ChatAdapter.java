package mdad.localdata.intershipsharingapp;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<Message> messages;
    private final String currentUserId;

    public ChatAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
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
