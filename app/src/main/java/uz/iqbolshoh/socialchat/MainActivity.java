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

public class MainActivity extends AppCompatActivity {

    private EditText editTextMessage;
    private Button buttonSend, buttonClear;
    private LinearLayout layoutMessages;
    private ScrollView scrollViewChat;

    private MessageDatabaseHelper dbHelper;

    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Sizning XML faylingiz

        // Viewlarni bog‘lash
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonClear = findViewById(R.id.buttonClear);
        layoutMessages = findViewById(R.id.layoutMessages);
        scrollViewChat = findViewById(R.id.scrollViewChat);

        // DB helper
        dbHelper = new MessageDatabaseHelper(this);

        // Style beramiz (background, border radius)
        setCustomBackgrounds();

        // Send tugmasi bosilganda
        buttonSend.setOnClickListener(v -> {
            String text = editTextMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                saveUserMessageAndSendApi(text);
                editTextMessage.setText("");
            } else {
                Toast.makeText(this, "Xabar kiriting, aka!", Toast.LENGTH_SHORT).show();
            }
        });

        // Clear tugmasi bosilganda
        buttonClear.setOnClickListener(v -> {
            dbHelper.clearAllMessages();
            loadMessages();
            Toast.makeText(this, "Chat tozalandi!", Toast.LENGTH_SHORT).show();
        });

        // Avvaldan xabarlarni yuklab olish
        loadMessages();
    }

    private void setCustomBackgrounds() {
        // EditText uchun gradient drawable border radius bilan
        GradientDrawable editTextBg = new GradientDrawable();
        editTextBg.setCornerRadius(24f);  // radius (px)
        editTextBg.setColor(Color.parseColor("#FFFFFF")); // Oq fon
        editTextBg.setStroke(2, Color.parseColor("#CCCCCC")); // Border rang va qalinligi
        editTextMessage.setBackground(editTextBg);

        // Clear button uchun qizil background va radius
        GradientDrawable redBtnBg = new GradientDrawable();
        redBtnBg.setCornerRadius(24f);
        redBtnBg.setColor(Color.parseColor("#D32F2F"));
        buttonClear.setBackground(redBtnBg);
        buttonClear.setTextColor(Color.WHITE);

        // Send button uchun yashil background va radius
        GradientDrawable greenBtnBg = new GradientDrawable();
        greenBtnBg.setCornerRadius(24f);
        greenBtnBg.setColor(Color.parseColor("#388E3C"));
        buttonSend.setBackground(greenBtnBg);
        buttonSend.setTextColor(Color.WHITE);
    }

    private void saveUserMessageAndSendApi(String userText) {
        Message userMessage = new Message();
        userMessage.content = userText;
        userMessage.isUser = true;
        userMessage.timestamp = System.currentTimeMillis();
        dbHelper.addMessage(userMessage);

        loadMessages();

        executor.execute(() -> {
            // Bu yerda sizning ApiService orqali javob olish jarayoni
            String apiResponse = ApiService.getGeminiResponse(userText);

            runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(apiResponse);

                    if (jsonObject.has("error")) {
                        Toast.makeText(MainActivity.this, "API Error: " + jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    String botReply = jsonObject.optString("text", "Bot javobi topilmadi");

                    Message botMessage = new Message();
                    botMessage.content = botReply;
                    botMessage.isUser = false;
                    botMessage.timestamp = System.currentTimeMillis();
                    dbHelper.addMessage(botMessage);

                    loadMessages();

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Javobni tahlil qilishda xatolik", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        });
    }

    private void loadMessages() {
        layoutMessages.removeAllViews();
        List<Message> messages = dbHelper.getAllMessages();

        for (Message msg : messages) {
            TextView textView = new TextView(this);

            // Background va border radius
            GradientDrawable bgDrawable = new GradientDrawable();
            bgDrawable.setCornerRadius(30f);
            if (msg.isUser) {
                bgDrawable.setColor(Color.parseColor("#A5D6A7")); // Yashil (user)
            } else {
                bgDrawable.setColor(Color.parseColor("#90CAF9")); // Ko‘k (bot)
            }

            textView.setBackground(bgDrawable);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int)(getResources().getDisplayMetrics().widthPixels * 0.75),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);

            if (msg.isUser) {
                params.gravity = Gravity.END;
                textView.setTextColor(Color.BLACK);
            } else {
                params.gravity = Gravity.START;
                textView.setTextColor(Color.BLACK);
            }

            textView.setLayoutParams(params);
            textView.setPadding(24, 16, 24, 16);
            textView.setText(msg.content);
            textView.setTextSize(16);
            textView.setMaxLines(10);

            layoutMessages.addView(textView);
        }

        scrollViewChat.post(() -> scrollViewChat.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
