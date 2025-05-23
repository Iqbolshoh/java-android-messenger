package uz.iqbolshoh.socialchat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText editTextMessage;
    private Button buttonSend;
    private Button buttonClear;
    private LinearLayout layoutMessages;
    private ScrollView scrollViewChat;

    private MessageDatabaseHelper dbHelper;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private boolean isFirstInput = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonClear = findViewById(R.id.buttonClear);
        layoutMessages = findViewById(R.id.layoutMessages);
        scrollViewChat = findViewById(R.id.scrollViewChat);

        // Initialize database
        dbHelper = new MessageDatabaseHelper(this);

        // Set up EditText animation
        setupEditTextAnimation();

        // Set up button listeners
        buttonSend.setOnClickListener(v -> {
            String text = editTextMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                animateSendButton();
                saveUserMessageAndSendApi(text);
                editTextMessage.setText("");
            } else {
                Toast.makeText(this, "Please enter a message!", Toast.LENGTH_SHORT).show();
            }
        });

        buttonClear.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("Clear Chat")
                    .setMessage("Are you sure you want to clear all messages?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        dbHelper.clearAllMessages();
                        loadMessages();
                        Toast.makeText(MainActivity.this, "Chat cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .setCancelable(true)
                    .show();
        });

        // Load existing messages
        loadMessages();
    }

    private void setupEditTextAnimation() {
        editTextMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && isFirstInput) {
                isFirstInput = false;
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(editTextMessage, "scaleX", 1.0f, 1.05f, 1.0f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(editTextMessage, "scaleY", 1.0f, 1.05f, 1.0f);
                ObjectAnimator alpha = ObjectAnimator.ofFloat(editTextMessage, "alpha", 0.7f, 1.0f);

                scaleX.setDuration(300);
                scaleY.setDuration(300);
                alpha.setDuration(300);

                scaleX.setInterpolator(new OvershootInterpolator());
                scaleY.setInterpolator(new OvershootInterpolator());

                scaleX.start();
                scaleY.start();
                alpha.start();
            }
        });
    }

    private void animateSendButton() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(buttonSend, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(buttonSend, "scaleY", 1.0f, 1.2f, 1.0f);

        scaleX.setDuration(200);
        scaleY.setDuration(200);

        scaleX.setInterpolator(new OvershootInterpolator());
        scaleY.setInterpolator(new OvershootInterpolator());

        scaleX.start();
        scaleY.start();
    }

    private void saveUserMessageAndSendApi(String userText) {
        // Create and save user message
        Message userMessage = new Message();
        userMessage.content = userText;
        userMessage.isUser = true;
        userMessage.timestamp = System.currentTimeMillis();
        dbHelper.addMessage(userMessage);

        // Display user message
        loadMessages();

        // Send to API in background
        executor.execute(() -> {
            String apiResponse = ApiService.getGeminiResponse(userText);

            runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(apiResponse);

                    if (jsonObject.has("error")) {
                        showErrorToast(jsonObject.getString("error"));
                        return;
                    }

                    // Process bot response
                    String botReply = jsonObject.optString("text", "No response from bot");
                    Message botMessage = new Message();
                    botMessage.content = botReply;
                    botMessage.isUser = false;
                    botMessage.timestamp = System.currentTimeMillis();
                    dbHelper.addMessage(botMessage);

                    // Display bot response
                    loadMessages();

                } catch (JSONException e) {
                    showErrorToast("Error parsing response");
                    e.printStackTrace();
                }
            });
        });
    }

    private void showErrorToast(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
    }

    private void loadMessages() {
        layoutMessages.removeAllViews();
        List<Message> messages = dbHelper.getAllMessages();

        for (Message msg : messages) {
            TextView textView = new TextView(this);

            // Create message bubble background
            GradientDrawable bgDrawable = new GradientDrawable();
            bgDrawable.setCornerRadii(msg.isUser ?
                    new float[]{30, 30, 8, 30, 30, 30, 30, 8} :
                    new float[]{8, 30, 30, 30, 30, 8, 30, 30});

            bgDrawable.setColor(msg.isUser ?
                    ContextCompat.getColor(this, R.color.user_message) :
                    ContextCompat.getColor(this, R.color.bot_message));

            textView.setBackground(bgDrawable);

            // Set layout parameters
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int)(getResources().getDisplayMetrics().widthPixels * 0.75),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            params.gravity = msg.isUser ? Gravity.END : Gravity.START;

            textView.setLayoutParams(params);
            textView.setTextColor(Color.WHITE);
            textView.setPadding(24, 16, 24, 16);
            textView.setText(msg.content);
            textView.setTextSize(16);
            textView.setMaxLines(20);

            // Add animation for new messages
            textView.setAlpha(0f);
            textView.setTranslationY(50f);
            layoutMessages.addView(textView);
            textView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start();
        }

        // Scroll to bottom
        scrollViewChat.post(() -> scrollViewChat.fullScroll(ScrollView.FOCUS_DOWN));
    }
}