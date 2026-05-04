package gift.chat.controller;

import gift.chat.service.ChatService;
import gift.chat.service.MessageRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/chat")
@RestController
public record ChatController(ChatService chatService) {

  @PostMapping
  public MessageResponse sendMessage(
      @Valid @RequestBody ChatMessage message
  ) {
    MessageRequest response = chatService.sendMessage(
        message.sessionId(),
        message.message()
    );
    return new MessageResponse(
        response.requestId(),
        response.message(),
        response.durationMs()
    );
  }
}
