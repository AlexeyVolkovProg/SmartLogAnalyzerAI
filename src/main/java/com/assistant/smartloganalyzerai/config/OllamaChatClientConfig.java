package com.assistant.smartloganalyzerai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are an expert DevOps incident analyst specializing in log analysis.
                        You analyze system logs to identify root causes of incidents,
                        detect anomalies, and provide actionable recommendations.
                        Always base your analysis on the provided log evidence.
                        Respond in a structured format.
                        """)
                .build();
    }
}
