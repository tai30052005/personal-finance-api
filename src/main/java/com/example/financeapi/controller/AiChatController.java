package com.example.financeapi.controller;

import com.example.financeapi.dto.request.ChatRequest;
import com.example.financeapi.dto.response.ChatResponse;
import com.example.financeapi.service.FinancialChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Trợ lý AI phân tích chi tiêu (chat). */
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final FinancialChatService chatService;

    public AiChatController(FinancialChatService chatService) {
        this.chatService = chatService;
    }

    /** POST /api/ai/chat — hỏi trợ lý về chi tiêu của kỳ đang xem. */
    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request.month(), request.year(), request.messages(), request.persona());
    }
}
