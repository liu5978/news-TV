import okhttp3.*;
import java.io.IOException;

public class DeepSeekHelper {
    private static final String API_URL = "https://openapi.coreshub.cn/v1/chat/completions";
    private static final String API_KEY = "sk-g3ZNE8YotymdVP97YyXPEmSIR3WBbWriVbHtq1tiZRGvaPuP"; // 替换为你的 API Key

    public interface ApiCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public static void callDeepSeekAPI(String prompt, ApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // 构建请求体（JSON 格式）
        String jsonBody = "{"
                + "\"model\": \"deepseek-chat\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]"
                + "}";

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("API 请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    callback.onSuccess(jsonResponse);
                } else {
                    callback.onFailure("API 错误: " + response.code());
                }
            }
        });
    }
}
