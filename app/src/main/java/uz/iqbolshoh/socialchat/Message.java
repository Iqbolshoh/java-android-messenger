package uz.iqbolshoh.socialchat;

/**
 * Represents a chat message with its properties, such as content, sender type, and timestamp.
 * Used for storing and retrieving messages in the database and displaying them in the UI.
 */
public class Message {
    public int id;          // Unique identifier for the message
    public String content;  // The text content of the message
    public boolean isUser;  // Indicates if the message is from the user (true) or bot (false)
    public long timestamp;  // Timestamp of when the message was created

    /**
     * Default constructor for creating an empty Message object.
     */
    public Message() {}

    /**
     * Constructor for creating a Message object with specified values.
     *
     * @param id        The unique identifier for the message.
     * @param content   The text content of the message.
     * @param isUser    True if the message is from the user, false if from the bot.
     * @param timestamp The timestamp of when the message was created.
     */
    public Message(int id, String content, boolean isUser, long timestamp) {
        this.id = id;
        this.content = content;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }
}