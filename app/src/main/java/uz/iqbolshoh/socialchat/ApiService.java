package uz.iqbolshoh.socialchat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiService {
    private static final String API_KEY = "API_KEY_HERE";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 30000;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    public static String getGeminiResponse(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return createErrorJson("Please enter a prompt");
        }

        HttpURLConnection connection = null;
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                // 1. Create connection
                URL url = new URL(GEMINI_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);

                // 2. Create JSON request matching PHP structure
                JSONObject requestBody = new JSONObject();
                JSONArray contentsArray = new JSONArray();
                JSONObject contentObject = new JSONObject();
                JSONArray partsArray = new JSONArray();
                JSONObject partObject = new JSONObject();

                partObject.put("text", prompt);
                partsArray.put(partObject);
                contentObject.put("parts", partsArray);
                contentsArray.put(contentObject);

                requestBody.put("contents", contentsArray);
                requestBody.put("generationConfig", new JSONObject()
                        .put("temperature", 0.7)
                        .put("maxOutputTokens", 512));

                // 3. Send request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // 4. Get response
                int statusCode = connection.getResponseCode();

                if (statusCode == 429) {
                    // Rate limit exceeded - wait and retry
                    retryCount++;
                    if (retryCount < MAX_RETRIES) {
                        Thread.sleep(RETRY_DELAY_MS);
                        continue;
                    }
                    return createErrorJson("Rate limit exceeded. Please try again later.");
                }

                if (statusCode != HttpURLConnection.HTTP_OK) {
                    String errorResponse = readErrorStream(connection);
                    return createErrorJson("API error (" + statusCode + "): " + errorResponse);
                }

                // 5. Parse successful response
                try (InputStream is = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return parseGeminiResponse(response.toString());
                }

            } catch (IOException | JSONException e) {
                return createErrorJson("Error: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return createErrorJson("Request interrupted");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return createErrorJson("Max retries exceeded");
    }

    private static String readErrorStream(HttpURLConnection connection) {
        try {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "No error details available";
    }

    private static String parseGeminiResponse(String jsonResponse) throws JSONException {
        JSONObject responseJson = new JSONObject(jsonResponse);
        if (!responseJson.has("candidates")) {
            return createErrorJson("Unexpected API response format");
        }

        JSONObject candidate = responseJson.getJSONArray("candidates").getJSONObject(0);
        JSONObject content = candidate.getJSONObject("content");
        JSONArray parts = content.getJSONArray("parts");
        if (parts.length() == 0) {
            return createErrorJson("No response parts received");
        }

        JSONObject part = parts.getJSONObject(0);
        String text = part.getString("text");

        JSONObject result = new JSONObject();
        result.put("text", text);
        return result.toString();
    }

    private static String createErrorJson(String message) {
        try {
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", message);
            return errorJson.toString();
        } catch (JSONException e) {
            return "{\"error\":\"Failed to create error message\"}";
        }
    }
}