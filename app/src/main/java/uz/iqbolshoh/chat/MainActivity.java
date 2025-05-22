package uz.iqbolshoh.chat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements GeminiApiClient.ApiCallback {

    private EditText promptInput;
    private TextView responseText;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        promptInput = findViewById(R.id.promptInput);
        responseText = findViewById(R.id.responseText);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {
            String prompt = promptInput.getText().toString().trim();
            if (!prompt.isEmpty()) {
                new GeminiApiClient(this).execute(prompt);
                responseText.setText("Loading...");
            } else {
                responseText.setText("Please enter a prompt");
            }
        });
    }

    @Override
    public void onSuccess(String response) {
        runOnUiThread(() -> responseText.setText(response));
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> responseText.setText(error));
    }
}