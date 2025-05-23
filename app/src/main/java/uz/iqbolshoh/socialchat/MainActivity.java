package uz.iqbolshoh.socialchat;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * The main activity of the SocialChat app, responsible for the chat interface.
 * Manages user input, displays messages, and interacts with the database and API.
 */
public class MainActivity extends AppCompatActivity {

    private EditText editTextMessage;   // Input field for user messages
    private Button buttonSend;          // Button to send messages
    private Button buttonClear;         // Button to clear chat history
    private LinearLayout layoutMessages;// Container for displaying chat messages
    private ScrollView scrollViewChat;  // Scrollable view for the chat area

    private MessageDatabaseHelper dbHelper; // Database helper for message storage
    private final Executor executor = Executors.newSingleThreadExecutor(); // Executor for background API calls

    /**
     * Initializes the activity, sets up UI components, database, and button listeners.
     *
     * @param savedInstanceState The saved instance state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind UI components
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonClear = findViewById(R.id.buttonClear);
        layoutMessages = findViewById(R.id.layoutMessages);
        scrollViewChat = findViewById(R.id.scrollViewChat);

        // Initialize database helper
        dbHelper = new MessageDatabaseHelper(this);

        // Apply custom styling to input and buttons
        setCustomBackgrounds();

        // Set up send button listener
        buttonSend.setOnClickListener(v -> {
            String text = editTextMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                saveUserMessageAndSendApi(text);
                editTextMessage.setText(""); // Clear input field
            } else {
                Toast.makeText(this, "Please enter a message!", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up clear button listener with confirmation dialog
        buttonClear.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("Warning")
                    .setMessage("Are you sure you want to clear all messages?")
                    .setPositiveButton("Yes, clear it!", (dialog, which) -> {
                        dbHelper.clearAllMessages(); // Clear database
                        loadMessages();             // Refresh UI
                        Toast.makeText(MainActivity.this, "Chat cleared!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Load existing messages from database
        loadMessages();
    }

    /**
     * Applies custom styling (rounded corners, colors) to the input field and buttons.
     */
    private void setCustomBackgrounds() {
        // Style the input field
        GradientDrawable editTextBg = new GradientDrawable();
        editTextBg.setCornerRadius(24f);
        editTextBg.setColor(Color.parseColor("#FFFFFF"));
        editTextBg.setStroke(2, Color.parseColor("#CCCCCC"));
        editTextMessage.setBackground(editTextBg);

        // Style the clear button (red)
        GradientDrawable redBtnBg = new GradientDrawable();
        redBtnBg.setCornerRadius(24f);
        redBtnBg.setColor(Color.parseColor("#D32F2F"));
        buttonClear.setBackground(redBtnBg);
        buttonClear.setTextColor(Color.WHITE);

        // Style the send button (green)
        GradientDrawable greenBtnBg = new GradientDrawable();
        greenBtnBg.setCornerRadius(24f);
        greenBtnBg.setColor(Color.parseColor("#388E3C"));
        buttonSend.setBackground(greenBtnBg);
        buttonSend.setTextColor(Color.WHITE);
    }

    /**
     * Saves the user's message to the database, sends it to the Gemini API,
     * and processes the bot's response.
     *
     * @param userText The user's input message.
     */
    private void saveUserMessageAndSendApi(String userText) {
        // Create and store user message
        Message userMessage = new Message();
        userMessage.content = userText;
        userMessage.isUser = true;
        userMessage.timestamp = System.currentTimeMillis();
        dbHelper.addMessage(userMessage);

        // Refresh UI to show user message
        loadMessages();

        // Send API request in background
        executor.execute(() -> {
            String apiResponse = ApiService.getGeminiResponse(userText);

            // Process response on UI thread
            runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(apiResponse);

                    if (jsonObject.has("error")) {
                        Toast.makeText(MainActivity.this, "API Error: " + jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Extract and store bot response
                    String botReply = jsonObject.optString("text", "No response from bot");
                    Message botMessage = new Message();
                    botMessage.content = botReply;
                    botMessage.isUser = false;
                    botMessage.timestamp = System.currentTimeMillis();
                    dbHelper.addMessage(botMessage);

                    // Refresh UI to show bot response
                    loadMessages();

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * Loads all messages from the database and displays them in the chat UI.
     * Styles messages differently for user (green, right-aligned) and bot (blue, left-aligned).
     */
    private void loadMessages() {
        layoutMessages.removeAllViews(); // Clear existing messages
        List<Message> messages = dbHelper.getAllMessages(); // Retrieve messages

        for (Message msg : messages) {
            // Create a TextView for the message
            TextView textView = new TextView(this);

            // Style the message background
            GradientDrawable bgDrawable = new GradientDrawable();
            bgDrawable.setCornerRadius(30f);
            bgDrawable.setColor(msg.isUser ? Color.parseColor("#A5D6A7") : Color.parseColor("#90CAF9")); // Green for user, blue for bot

            textView.setBackground(bgDrawable);

            // Set layout parameters for alignment and size
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int)(getResources().getDisplayMetrics().widthPixels * 0.75),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            params.gravity = msg.isUser ? Gravity.END : Gravity.START; // Align user messages right, bot messages left

            textView.setLayoutParams(params);
            textView.setTextColor(Color.BLACK);
            textView.setPadding(24, 16, 24, 16);
            textView.setText(msg.content);
            textView.setTextSize(16);
            textView.setMaxLines(10);

            // Add message to the chat layout
            layoutMessages.addView(textView);
        }

        // Auto-scroll to the bottom of the chat
        scrollViewChat.post(() -> scrollViewChat.fullScroll(ScrollView.FOCUS_DOWN));
    }
}