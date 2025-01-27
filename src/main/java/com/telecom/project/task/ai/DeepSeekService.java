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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {


    @Resource
    private DeepSeekConfig deepSeekConfig;

    private List<Map<String, String>> conversationHistory = new ArrayList<>();

    public String sendMessage(String userMessage) throws Exception {
        // 添加用户消息到对话历史
        Map<String, String> userEntry = new HashMap<>();
        userEntry.put("role", "user");
        userEntry.put("content", userMessage);
        conversationHistory.add(userEntry);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("https://api.deepseek.com/chat/completions");
            request.setHeader("Authorization", "Bearer " + deepSeekConfig.getApiKey());
            request.setHeader("Content-Type", "application/json; charset=UTF-8");

            // 构建请求体
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("model", "deepseek-reasoner");

            // 添加系统消息，要求机器人使用中文
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "请使用中文回答所有问题。");

            // 添加用户消息和系统指令
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(systemMessage);
            messages.addAll(conversationHistory);  // 添加对话历史
            jsonMap.put("messages", messages);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(jsonMap);
            request.setEntity(new StringEntity(json, "UTF-8"));

            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

            // 打印响应体以便调试
            System.out.println("Response Body: " + responseBody);

            // 校验响应体并解析
            JsonNode responseJson = objectMapper.readTree(responseBody);

            // 检查 'choices' 字段是否存在并且是数组
            if (responseJson.has("choices") && responseJson.get("choices").isArray()) {
                JsonNode choices = responseJson.get("choices");
                if (choices.size() > 0) {
                    JsonNode firstChoice = choices.get(0);

                    // 确保 'message' 和 'content' 字段存在
                    if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                        String assistantMessage = firstChoice.get("message").get("content").asText();
                        System.out.println("assistantMessage：" + assistantMessage);

                        // 添加助手消息到对话历史
                        Map<String, String> assistantEntry = new HashMap<>();
                        assistantEntry.put("role", "assistant");
                        assistantEntry.put("content", assistantMessage);
                        conversationHistory.add(assistantEntry);

                        return assistantMessage;  // 返回助手消息
                    } else {
                        // 如果没有 'message' 或 'content'，抛出异常
                        throw new RuntimeException("Invalid response: 'message' or 'content' missing in 'choices'.");
                    }
                } else {
                    // 如果 'choices' 数组为空，抛出异常
                    throw new RuntimeException("Invalid response: 'choices' array is empty.");
                }
            } else {
                // 如果没有 'choices' 字段或者不是数组，抛出异常
                throw new RuntimeException("Invalid response format: 'choices' is missing or not an array.");
            }
        } catch (Exception e) {
            e.printStackTrace();  // 打印详细异常信息
            throw new RuntimeException("Error occurred while sending message: " + e.getMessage(), e);
        }
    }
}