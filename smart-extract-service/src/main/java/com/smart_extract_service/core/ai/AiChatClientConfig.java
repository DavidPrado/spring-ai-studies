package com.smart_extract_service.core.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatClientConfig {

    private static final String OLLAMA_BASE_URL = "http://localhost:11434";

    @Bean("qwenChatClient")
    public ChatClient qwenChatClient(){
        OllamaApi ollamaApi = new OllamaApi(OLLAMA_BASE_URL);

        OllamaOptions qwenOptions = OllamaOptions.create()
                .withModel("qwen2.5:7b")
                .withTemperature(0.3f)
                .withNumCtx(4096);

        OllamaChatModel qwenModel = new OllamaChatModel(ollamaApi, qwenOptions);

        return ChatClient.builder(qwenModel).build();
    }

    @Bean("phiChatClient")
    public  ChatClient phiChatClient(){
        OllamaApi ollamaApi = new OllamaApi(OLLAMA_BASE_URL);

        OllamaOptions phiOptions = OllamaOptions.create()
                .withModel("phi3.5:3.8b")
                .withTemperature(0.1f)
                .withNumCtx(2048);

        OllamaChatModel phiModel = new OllamaChatModel(ollamaApi, phiOptions);

        return  ChatClient.builder(phiModel).build();
    }
}
