package com.telecom.project.controller;

import com.telecom.project.model.dto.ai.ChatRequest;
import com.telecom.project.task.ai.DeepSeekService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: Toys
 * @date: 2025年01月27 10:00
 **/
@RestController
@RequestMapping("/ai")
public class AIController {


    @Resource
    private DeepSeekService deepSeekService;


    @RequestMapping("/chat")
    public String chat(@RequestBody ChatRequest chatRequest) {
        try {
            // 调用 sendMessage 处理消息
            String assistantMessage = deepSeekService.sendMessage(chatRequest.getMessage());
            // 返回 assistantMessage 到前端
            return assistantMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

}
