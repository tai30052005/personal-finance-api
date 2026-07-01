package com.example.financeapi.dto.request;

/** Một lượt trong hội thoại chat. role: "user" (người dùng) hoặc "assistant" (AI). */
public record ChatMessage(
        String role,
        String text
) {
}
