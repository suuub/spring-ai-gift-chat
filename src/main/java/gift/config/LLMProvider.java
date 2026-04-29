package gift.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMProvider {

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
    public OpenAIClient openAIChatClient(ChatClient.Builder builder) {
        return new OpenAIClient(builder);
    }
}
