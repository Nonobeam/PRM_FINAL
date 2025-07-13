package per.nonobeam.phucnhse183026.myapplication.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import per.nonobeam.phucnhse183026.myapplication.R;
import per.nonobeam.phucnhse183026.myapplication.model.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;
    private Context context;
    private String currentUser;

    public ChatAdapter(List<ChatMessage> messages, Context context, String currentUser) {
        this.messages = messages;
        this.context = context;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.messageTextView.setText(message.getMessage());
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        holder.timeTextView.setText(sdf.format(new Date(message.getTimestamp())));
        holder.senderTextView.setText(message.getSender());
        if (message.getSender().equals(currentUser)) {
            holder.messageLayout.setGravity(Gravity.END); // Align to right
            holder.messageTextView.setBackgroundResource(R.drawable.message_bubble_right);
        } else {
            holder.messageLayout.setGravity(Gravity.START); // Align to left
            holder.messageTextView.setBackgroundResource(R.drawable.message_bubble_left);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, messageTextView, timeTextView;
        LinearLayout messageLayout;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.senderTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }
    }
}