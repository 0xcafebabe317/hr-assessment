package com.telecom.project.task.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.project.config.DeepSeekConfig;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {

    @Resource
    private DeepSeekConfig deepSeekConfig;

    // 线程安全的对话历史存储
    private final ThreadLocal<List<Map<String, String>>> conversationHistoryThreadLocal = ThreadLocal.withInitial(ArrayList::new);

    public String sendMessage(String userMessage) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("https://api.deepseek.com/chat/completions");
            request.setHeader("Authorization", "Bearer " + deepSeekConfig.getApiKey());
            request.setHeader("Content-Type", "application/json; charset=UTF-8");

            List<Map<String, String>> conversationHistory = conversationHistoryThreadLocal.get();

            // 添加用户消息到对话历史
            Map<String, String> userEntry = new HashMap<>();
            userEntry.put("role", "user");
            userEntry.put("content", userMessage);
            conversationHistory.add(userEntry);

            // 构建请求体
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("model", "deepseek-reasoner");
            jsonMap.put("stream", false);  // 确保返回完整 JSON

            // 系统消息（可选）
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "请使用中文回答所有问题。");

            // 组装最终的对话列表
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(systemMessage);
            messages.addAll(conversationHistory);

            jsonMap.put("messages", messages);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(jsonMap);
            request.setEntity(new StringEntity(json, "UTF-8"));

            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            System.out.println(responseBody);
            // 解析 API 响应
            JsonNode responseJson = objectMapper.readTree(responseBody);
            if (responseJson.has("choices") && responseJson.get("choices").isArray() && responseJson.get("choices").size() > 0) {
                JsonNode choiceNode = responseJson.get("choices").get(0);

                // 确保正确获取 `content`
                JsonNode messageNode = choiceNode.has("message") ? choiceNode.get("message") : choiceNode.get("delta");
                if (messageNode != null && messageNode.has("content")) {
                    String assistantMessage = messageNode.get("content").asText();

                    // 存储助手的回复
                    Map<String, String> assistantEntry = new HashMap<>();
                    assistantEntry.put("role", "assistant");
                    assistantEntry.put("content", assistantMessage);
                    conversationHistory.add(assistantEntry);

                    return assistantMessage;
                }
            }
            return "未能获取有效回复，请稍后再试。";
        } catch (Exception e) {
            e.printStackTrace();
            return "发生错误：" + e.getMessage();
        }
    }

    /**
     * 清空当前线程的对话历史（用于新会话）
     */
    public void clearHistory() {
        conversationHistoryThreadLocal.remove();
    }
}
