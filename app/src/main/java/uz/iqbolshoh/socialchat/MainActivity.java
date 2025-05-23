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
 * MainActivity is the core screen of the SocialChat Android app.
 * It allows users to send messages, stores them locally using SQLite (via helper),
 * and simulates chatbot responses from a custom API service.
 */
public class MainActivity extends AppCompatActivity {

    private EditText editTextMessage;
    private Button buttonSend, buttonClear;
    private LinearLayout layoutMessages;
    private ScrollView scrollViewChat;

    private MessageDatabaseHelper dbHelper;
    private final Executor executor = Executors.newSingleThreadExecutor(); // For background API calls

    /**
     * Called when the activity is starting. Sets up UI, database, and button listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View bindings
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonClear = findViewById(R.id.buttonClear);
        layoutMessages = findViewById(R.id.layoutMessages);
        scrollViewChat = findViewById(R.id.scrollViewChat);

        // Initialize database helper
        dbHelper = new MessageDatabaseHelper(this);

        // Style the input and buttons
        setCustomBackgrounds();

        // Send message button
        buttonSend.setOnClickListener(v -> {
            String text = editTextMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                saveUserMessageAndSendApi(text);
                editTextMessage.setText(""); // clear input after sending
            } else {
                Toast.makeText(this, "Please enter a message!", Toast.LENGTH_SHORT).show();
            }
        });

        // Clear chat button with confirmation dialog
        buttonClear.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("Warning")
                    .setMessage("Are you sure you want to clear all messages?")
                    .setPositiveButton("Yes, clear it!", (dialog, which) -> {
                        dbHelper.clearAllMessages(); // delete all messages
                        loadMessages();              // reload UI
                        Toast.makeText(MainActivity.this, "Chat cleared!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Load previous messages from database
        loadMessages();
    }

    /**
     * Applies modern UI designs like rounded corners and custom colors to EditText and Buttons.
     */
    private void setCustomBackgrounds() {
        GradientDrawable editTextBg = new GradientDrawable();
        editTextBg.setCornerRadius(24f);
        editTextBg.setColor(Color.parseColor("#FFFFFF"));
        editTextBg.setStroke(2, Color.parseColor("#CCCCCC"));
        editTextMessage.setBackground(editTextBg);

        GradientDrawable redBtnBg = new GradientDrawable();
        redBtnBg.setCornerRadius(24f);
        redBtnBg.setColor(Color.parseColor("#D32F2F"));
        buttonClear.setBackground(redBtnBg);
        buttonClear.setTextColor(Color.WHITE);

        GradientDrawable greenBtnBg = new GradientDrawable();
        greenBtnBg.setCornerRadius(24f);
        greenBtnBg.setColor(Color.parseColor("#388E3C"));
        buttonSend.setBackground(greenBtnBg);
        buttonSend.setTextColor(Color.WHITE);
    }

    /**
     * Saves user's message to the database, sends it to Gemini API in background,
     * then parses and stores the bot's reply.
     *
     * @param userText The user's input message.
     */
    private void saveUserMessageAndSendApi(String userText) {
        Message userMessage = new Message();
        userMessage.content = userText;
        userMessage.isUser = true;
        userMessage.timestamp = System.currentTimeMillis();
        dbHelper.addMessage(userMessage); // Store message

        loadMessages(); // Refresh UI

        executor.execute(() -> {
            String apiResponse = ApiService.getGeminiResponse(userText); // API Call

            runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(apiResponse);

                    if (jsonObject.has("error")) {
                        Toast.makeText(MainActivity.this, "API Error: " + jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    String botReply = jsonObject.optString("text", "No response from bot");

                    Message botMessage = new Message();
                    botMessage.content = botReply;
                    botMessage.isUser = false;
                    botMessage.timestamp = System.currentTimeMillis();
                    dbHelper.addMessage(botMessage); // Save bot response

                    loadMessages(); // Update chat view

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * Loads all messages from local database and displays them in the chat window.
     * Messages are styled and aligned based on who sent them (user or bot).
     */
    private void loadMessages() {
        layoutMessages.removeAllViews(); // Clear UI
        List<Message> messages = dbHelper.getAllMessages(); // Get stored messages

        for (Message msg : messages) {
            TextView textView = new TextView(this);

            GradientDrawable bgDrawable = new GradientDrawable();
            bgDrawable.setCornerRadius(30f);

            if (msg.isUser) {
                bgDrawable.setColor(Color.parseColor("#A5D6A7")); // User - green
            } else {
                bgDrawable.setColor(Color.parseColor("#90CAF9")); // Bot - blue
            }

            textView.setBackground(bgDrawable);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int)(getResources().getDisplayMetrics().widthPixels * 0.75),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            params.gravity = msg.isUser ? Gravity.END : Gravity.START;

            textView.setLayoutParams(params);
            textView.setTextColor(Color.BLACK);
            textView.setPadding(24, 16, 24, 16);
            textView.setText(msg.content);
            textView.setTextSize(16);
            textView.setMaxLines(10);

            layoutMessages.addView(textView);
        }

        // Scroll to the bottom of the chat
        scrollViewChat.post(() -> scrollViewChat.fullScroll(ScrollView.FOCUS_DOWN));
    }
}