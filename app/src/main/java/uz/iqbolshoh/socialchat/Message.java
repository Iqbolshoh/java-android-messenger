package uz.iqbolshoh.socialchat;

public class Message {
    public int id;
    public String content;
    public boolean isUser; // true = user, false = bot
    public long timestamp;

    public Message() {}

    public Message(int id, String content, boolean isUser, long timestamp) {
        this.id = id;
        this.content = content;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }
}
