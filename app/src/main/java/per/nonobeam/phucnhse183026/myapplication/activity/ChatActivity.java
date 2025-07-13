package per.nonobeam.phucnhse183026.myapplication.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import per.nonobeam.phucnhse183026.myapplication.R;
import per.nonobeam.phucnhse183026.myapplication.adapter.ChatAdapter;
import per.nonobeam.phucnhse183026.myapplication.helpers.DatabaseHelper;
import per.nonobeam.phucnhse183026.myapplication.model.ChatMessage;

public class ChatActivity extends AppCompatActivity {
    private EditText messageInput;
    private Button sendButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private DatabaseHelper dbHelper;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new DatabaseHelper(this);
        int userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("userId", -1);
        currentUser = dbHelper.getUsernameById(userId);
        loadMessages();
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        messages = dbHelper.getAllMessages();
        chatAdapter = new ChatAdapter(messages, this, currentUser);
        chatRecyclerView.setAdapter(chatAdapter);
        scrollToBottom();
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            Log.e("ChatActivity", "Message is empty, cannot send.");
            return;
        }
        int userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getInt("userId", -1);
        Log.d("ChatActivity", "userId: " + userId);
        if (userId == -1) {
            Log.e("ChatActivity", "User ID not found.");
            return;
        }
        String sender = dbHelper.getUsernameById(userId);
        if (sender == null || sender.isEmpty()) {
            Log.e("ChatActivity", "Sender name not found.");
            return;
        }
        dbHelper.addMessage(sender, messageText); // Save to DB
        messageInput.setText("");
        long timestamp = System.currentTimeMillis();
        ChatMessage newMessage = new ChatMessage(sender, messageText, timestamp);
        messages.add(newMessage);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }


    private void scrollToBottom() {
        if (messages.size() > 0) {
            chatRecyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}