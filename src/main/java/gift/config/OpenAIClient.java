package gift.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

@Component
public class OpenAIClient implements LLMClient {

    private final ChatClient chatClient;

    public OpenAIClient(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public ChatResponse sendMessage(String message) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(message)
                .call()
                .chatResponse();
    }
}
