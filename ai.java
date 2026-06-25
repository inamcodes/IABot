import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.*;

public class ai {

    private static final String API_KEY = "PLACE_YOUR_API_KEY_HERE";
    private static final String MODEL = "gemini-2.0-flash-001";

    public static String generateText(String prompt) throws IOException {
        String endpoint = String.format(
                "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s",
                MODEL, API_KEY
        );

        String requestJson = "{\"contents\":[{\"parts\":[{\"text\":\"" +
                prompt.replace("\"", "\\\"") +
                "\"}]}]}";

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestJson.getBytes("utf-8"));
        }

        if (conn.getResponseCode() != 200) {
            InputStream err = conn.getErrorStream();
            String errMsg = err != null
                    ? new BufferedReader(new InputStreamReader(err)).lines().reduce("", String::concat)
                    : "HTTP error " + conn.getResponseCode();
            throw new IOException("Request failed: " + errMsg);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }

        // Extract only the actual text
        JSONObject obj = new JSONObject(sb.toString());
        String text = obj.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        // Handle new lines
        return text.replace("\\n", "\n");
    }

    public static String prompt(String prompt) {
    String extra_info = " Please keep response short and to the point.";
        try {
            String result = generateText(prompt + extra_info);
            return result;
        } catch (IOException e) {
            return "Sorry! There is an issue. Try Later.";
        }
    }
}

