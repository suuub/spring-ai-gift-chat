package gift.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

@DisplayName("ChatService는")
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  private final Logger logger = (Logger) getLogger(ChatService.class);
  private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
  @Mock
  private ChatClient.Builder builder;
  @Mock
  private ChatClient chatClient;
  @Mock
  private ChatClient.ChatClientRequestSpec requestSpec;
  @Mock
  private ChatClient.CallResponseSpec responseSpec;

  @AfterEach
  void tearDown() {
    logger.detachAppender(listAppender);
  }

  @DisplayName("LLM 응답이 성공하면 추천 메시지와 메타데이터를 반환한다")
  @Test
  void sendMessage() {
    ChatService chatService = createService();
    UUID sessionId = UUID.randomUUID();

    when(responseSpec.chatResponse()).thenReturn(chatResponse("텀블러를 추천합니다.", 11, 7, 18));

    MessageRequest result = chatService.sendMessage(sessionId, "친구 생일 선물 추천해 줘");

    assertThat(result.requestId()).isNotNull();
    assertThat(result.message()).isEqualTo("텀블러를 추천합니다.");
    assertThat(result.durationMs()).isGreaterThanOrEqualTo(0L);
    verify(requestSpec).system(anyString());
    verify(requestSpec).user("친구 생일 선물 추천해 줘");
  }

  @DisplayName("LLM 호출 중 예외가 발생하면 안내 메시지를 반환한다")
  @Test
  void sendMessageWhenLlmCallFails() {
    ChatService chatService = createService();

    when(responseSpec.chatResponse()).thenThrow(new RuntimeException("boom"));

    MessageRequest result = chatService.sendMessage(UUID.randomUUID(), "아빠 선물 추천해 줘");

    assertThat(result.requestId()).isNotNull();
    assertThat(result.message()).isEqualTo("지금은 선물 추천을 준비 중입니다. 잠시 후 다시 시도해 주세요.");
    assertThat(result.durationMs()).isGreaterThanOrEqualTo(0L);
  }

  @DisplayName("요청 식별자와 응답 생성 시간, 전체 토큰 사용량을 로그로 남긴다")
  @Test
  void logRequestInformation() {
    ChatService chatService = createService();
    listAppender.start();
    logger.addAppender(listAppender);
    when(responseSpec.chatResponse()).thenReturn(chatResponse("향초를 추천합니다.", 9, 5, 14));

    MessageRequest result = chatService.sendMessage(UUID.randomUUID(), "집들이 선물 추천해 줘");

    assertThat(listAppender.list).hasSize(1);
    ILoggingEvent event = listAppender.list.getFirst();
    assertThat(event.getLevel()).isEqualTo(Level.INFO);
    assertThat(event.getFormattedMessage()).contains(result.requestId().toString());
    assertThat(event.getFormattedMessage()).contains("sessionId=");
    assertThat(event.getFormattedMessage()).contains("durationMs=");
    assertThat(event.getFormattedMessage()).contains("token=14");
    assertThat(event.getFormattedMessage()).doesNotContain("집들이 선물 추천해 줘");
  }

  @DisplayName("LLM 호출 중 예외가 발생하면 사용자 메시지를 포함한 오류 로그를 남긴다")
  @Test
  void logErrorInformationWhenLlmCallFails() {
    ChatService chatService = createService();
    listAppender.start();
    logger.addAppender(listAppender);
    when(responseSpec.chatResponse()).thenThrow(new RuntimeException("boom"));

    chatService.sendMessage(UUID.randomUUID(), "아빠 선물 추천해 줘");

    assertThat(listAppender.list).hasSize(1);
    ILoggingEvent event = listAppender.list.getFirst();
    assertThat(event.getLevel()).isEqualTo(Level.ERROR);
    assertThat(event.getFormattedMessage()).contains("durationMs=");
  }

  private ChatService createService() {
    when(builder.build()).thenReturn(chatClient);
    when(chatClient.prompt()).thenReturn(requestSpec);
    when(requestSpec.system(anyString())).thenReturn(requestSpec);
    when(requestSpec.user(anyString())).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(responseSpec);
    return new ChatService(builder);
  }

  private ChatResponse chatResponse(String content, int promptTokens, int completionTokens, int totalTokens) {
    return new ChatResponse(
        List.of(new Generation(new AssistantMessage(content))),
        ChatResponseMetadata.builder()
            .usage(new DefaultUsage(promptTokens, completionTokens, totalTokens))
            .build()
    );
  }
}
