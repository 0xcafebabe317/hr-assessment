package com.telecom.project.task.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.project.config.DeepSeekConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class DeepSeekService {

    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";
    private static final String MODEL = "deepseek-reasoner";
    private static final int TIMEOUT = 5000; // 5秒超时
    private static final int MAX_HISTORY_SIZE = 20; // 限制对话历史大小

    @Resource
    private DeepSeekConfig deepSeekConfig;

    private final List<Map<String, String>> conversationHistory = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送消息给 DeepSeek AI，并返回助手的回复
     */
    public String sendMessage(String userMessage) {
        // 记录用户消息
        addMessageToHistory("user", userMessage);

        try (CloseableHttpClient httpClient = createHttpClient()) {
            HttpPost request = new HttpPost(API_URL);
            request.setHeader("Authorization", "Bearer " + deepSeekConfig.getApiKey());
            request.setHeader("Content-Type", CONTENT_TYPE);
            request.setEntity(new StringEntity(buildRequestJson(), "UTF-8"));

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    log.error("API 请求失败，状态码：{}", statusCode);
                    return "请求失败，请稍后再试";
                }

                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                return parseResponse(responseBody);
            }
        } catch (IOException e) {
            log.error("与 DeepSeek API 交互时发生错误", e);
            return "网络错误，请稍后重试";
        }
    }

    /**
     * 创建 HTTP 客户端，配置超时时间
     */
    private CloseableHttpClient createHttpClient() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT)
                .build();
        return HttpClients.custom().setDefaultRequestConfig(config).build();
    }

    /**
     * 构建 API 请求 JSON
     */
    private String buildRequestJson() throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("model", MODEL);

        // 系统消息，确保回答使用中文
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "请使用中文回答所有问题。");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.addAll(conversationHistory);

        jsonMap.put("messages", messages);
        return objectMapper.writeValueAsString(jsonMap);
    }

    /**
     * 解析 API 响应，返回助手的回答
     */
    private String parseResponse(String responseBody) throws IOException {
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode choices = responseJson.path("choices");

        if (!choices.isArray() || choices.isEmpty()) {
            log.error("API 响应无效：{}", responseBody);
            return "抱歉，我无法处理您的请求。";
        }

        JsonNode firstChoice = choices.get(0);
        String assistantMessage = firstChoice.path("message").path("content").asText(null);

        if (assistantMessage == null) {
            log.error("API 响应格式错误：{}", responseBody);
            return "抱歉，我无法理解您的请求。";
        }

        addMessageToHistory("assistant", assistantMessage);
        return assistantMessage;
    }

    /**
     * 添加消息到对话历史，并控制历史长度
     */
    private void addMessageToHistory(String role, String content) {
        Map<String, String> entry = new HashMap<>();
        entry.put("role", role);
        entry.put("content", content);
        conversationHistory.add(entry);

        // 控制对话历史长度
        if (conversationHistory.size() > MAX_HISTORY_SIZE) {
            conversationHistory.remove(0);
        }
    }
}
