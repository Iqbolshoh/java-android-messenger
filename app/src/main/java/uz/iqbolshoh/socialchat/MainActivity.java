package uz.iqbolshoh.socialchat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
    private Button buttonSend, buttonClear;  // Clear tugma qo'shildi
    private TextView textViewChat;

    private MessageDatabaseHelper dbHelper;

    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonClear = findViewById(R.id.buttonClear);  // Clear tugmasini bog'lash
        textViewChat = findViewById(R.id.textViewChat);

        dbHelper = new MessageDatabaseHelper(this);

        buttonSend.setOnClickListener(v -> {
            String text = editTextMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                saveUserMessageAndSendApi(text);
                editTextMessage.setText("");
            } else {
                Toast.makeText(this, "Xabar kiriting, aka!", Toast.LENGTH_SHORT).show();
            }
        });

        buttonClear.setOnClickListener(v -> {
            dbHelper.clearAllMessages();  // DB ichidagi barcha xabarlarni o'chirish
            loadMessages();               // UI ni yangilash
            Toast.makeText(this, "Chat tozalandi!", Toast.LENGTH_SHORT).show();
        });

        loadMessages();
    }

    private void saveUserMessageAndSendApi(String userText) {
        // User xabarini DB ga saqlash
        Message userMessage = new Message();
        userMessage.content = userText;
        userMessage.isUser = true;
        userMessage.timestamp = System.currentTimeMillis();
        dbHelper.addMessage(userMessage);

        loadMessages(); // Yangi user xabarini ko'rsatish

        // API ga so'rov yuborish, javobni olish va DB ga saqlash
        executor.execute(() -> {
            String apiResponse = ApiService.getGeminiResponse(userText);

            runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(apiResponse);

                    if (jsonObject.has("error")) {
                        Toast.makeText(MainActivity.this, "API Error: " + jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    String botReply = jsonObject.optString("text", "Bot javobi topilmadi");

                    // Bot javobini DB ga saqlash
                    Message botMessage = new Message();
                    botMessage.content = botReply;
                    botMessage.isUser = false;
                    botMessage.timestamp = System.currentTimeMillis();
                    dbHelper.addMessage(botMessage);

                    loadMessages(); // Bot javobini ko'rsatish

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Javobni tahlil qilishda xatolik", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        });
    }

    private void loadMessages() {
        List<Message> messages = dbHelper.getAllMessages();
        StringBuilder chatText = new StringBuilder();

        for (Message msg : messages) {
            if (msg.isUser) {
                chatText.append("Siz: ").append(msg.content).append("\n");
            } else {
                chatText.append("Bot: ").append(msg.content).append("\n");
            }
        }

        textViewChat.setText(chatText.toString());
    }
}
