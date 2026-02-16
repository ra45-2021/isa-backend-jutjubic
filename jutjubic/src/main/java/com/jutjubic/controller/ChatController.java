package com.jutjubic.controller;

import com.jutjubic.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;

@Controller
public class ChatController {

    @MessageMapping("/chat.send/{postId}")
    @SendTo("/topic/chat/{postId}")
    public ChatMessage send(@DestinationVariable Long postId,
                            ChatMessage message,
                            Principal principal) {
        message.setSender(principal.getName());
        message.setTimestamp(Instant.now());
        return message;
    }
}
