package gift.chat.service;

import java.util.UUID;

public record MessageRequest(
    UUID requestId,
    String message,
    Long durationMs
) {

}
