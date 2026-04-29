package gift.chat.service;

import gift.config.LLMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import static java.util.UUID.randomUUID;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final String FALLBACK_MESSAGE = "지금은 선물 추천을 준비 중입니다. 잠시 후 다시 시도해 주세요.";
    private final LLMClient llmClient;

    public ChatService(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    public MessageRequest sendMessage(UUID sessionId, String message) {
        UUID requestId = randomUUID();
        long startTime = System.nanoTime();

        try {
            var response = llmClient.sendMessage(message);
            return createResponse(sessionId, requestId, Objects.requireNonNull(response), startTime);
        } catch (RuntimeException exception) {
            return createErrorResponse(sessionId, requestId, startTime);
        }
    }

    private MessageRequest createResponse(
            UUID sessionId,
            UUID requestId,
            ChatResponse response,
            long startTime
    ) {
        long durationMs = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
        log.info(
                "requestId={}, sessionId={}, token={}, durationMs={}",
                requestId,
                sessionId,
                response.getMetadata().getUsage().getTotalTokens(),
                durationMs
        );
        return new MessageRequest(requestId, response.getResult().getOutput().getText(), durationMs);
    }

    private MessageRequest createErrorResponse(
            UUID sessionId,
            UUID requestId,
            long startTime
    ) {
        long durationMs = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
        log.error(
                "requestId={}, sessionId={}, durationMs={}",
                requestId,
                sessionId,
                durationMs
        );
        return new MessageRequest(requestId, FALLBACK_MESSAGE, durationMs);
    }
}
