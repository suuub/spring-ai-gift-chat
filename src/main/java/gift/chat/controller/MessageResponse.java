package gift.chat.controller;

import java.util.UUID;

public record MessageResponse(
    UUID requestId,
    String message,
    Long durationMs
) {

}
