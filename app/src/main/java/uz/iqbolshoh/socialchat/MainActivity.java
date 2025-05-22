package uz.iqbolshoh.socialchat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText editTextPrompt;
    private TextView textViewResponse;
    private Button buttonSend;
    private Button buttonClear;
    private ProgressBar progressBar;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        editTextPrompt = findViewById(R.id.editTextPrompt);
        textViewResponse = findViewById(R.id.textViewResponse);
        buttonSend = findViewById(R.id.buttonSend);
        buttonClear = findViewById(R.id.buttonClear);
        progressBar = findViewById(R.id.progressBar);

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean darkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        buttonSend.setOnClickListener(v -> {
            String prompt = editTextPrompt.getText().toString().trim();
            if (!prompt.isEmpty()) {
                sendRequestToGemini(prompt);
            } else {
                showToast("Please enter a prompt");
            }
        });

        buttonClear.setOnClickListener(v -> editTextPrompt.setText(""));
    }

    private void sendRequestToGemini(String prompt) {
        // Show loading state
        progressBar.setVisibility(View.VISIBLE);
        buttonSend.setEnabled(false);
        textViewResponse.setText("");

        executor.execute(() -> {
            String response = ApiService.getGeminiResponse(prompt);

            handler.post(() -> {
                progressBar.setVisibility(View.GONE);
                buttonSend.setEnabled(true);

                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("error")) {
                        String error = jsonResponse.getString("error");
                        textViewResponse.setText("Error: " + error);
                        showToast(error);
                    } else {
                        String resultText = jsonResponse.getString("text");
                        textViewResponse.setText(resultText);
                    }
                } catch (JSONException e) {
                    textViewResponse.setText("Error parsing response");
                    showToast("Failed to parse response");
                }
            });
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}