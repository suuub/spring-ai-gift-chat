package gift.chat.controller;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ChatMessage(
    @NotBlank String message,
    UUID sessionId
) {

}
