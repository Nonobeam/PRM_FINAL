package per.nonobeam.phucnhse183026.myapplication.model;

public class ChatMessage {
    public int id;
    public String sender;
    public String message;
    public long timestamp;

    public ChatMessage(String sender, String message, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSender() { return sender; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}