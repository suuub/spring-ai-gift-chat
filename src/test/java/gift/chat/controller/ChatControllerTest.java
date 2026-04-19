package gift.chat.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.chat.service.ChatService;
import gift.chat.service.MessageRequest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ChatService chatService;

  @DisplayName("유효한 요청을 받으면 서비스에 위임하고 응답 본문을 반환한다")
  @Test
  void sendMessage() throws Exception {
    UUID sessionId = UUID.randomUUID();
    UUID requestId = UUID.randomUUID();
    given(chatService.sendMessage(sessionId, "친구 생일 선물 추천해 줘"))
        .willReturn(new MessageRequest(requestId, "무드등을 추천합니다.", 15L));

    mockMvc.perform(post("/api/chat")
            .contentType(APPLICATION_JSON)
            .content("""
                {
                  "message": "친구 생일 선물 추천해 줘",
                  "sessionId": "%s"
                }
                """.formatted(sessionId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(requestId.toString()))
        .andExpect(jsonPath("$.message").value("무드등을 추천합니다."))
        .andExpect(jsonPath("$.durationMs").value(15));

    verify(chatService).sendMessage(sessionId, "친구 생일 선물 추천해 줘");
  }

  @DisplayName("sessionId가 없어도 요청을 처리한다")
  @Test
  void sendMessageWithoutSessionId() throws Exception {
    UUID requestId = UUID.randomUUID();
    given(chatService.sendMessage(null, "엄마 선물 추천해 줘"))
        .willReturn(new MessageRequest(requestId, "손크림 세트를 추천합니다.", 7L));

    mockMvc.perform(post("/api/chat")
            .contentType(APPLICATION_JSON)
            .content("""
                {
                  "message": "엄마 선물 추천해 줘"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(requestId.toString()))
        .andExpect(jsonPath("$.message").value("손크림 세트를 추천합니다."))
        .andExpect(jsonPath("$.durationMs").value(7));

    verify(chatService).sendMessage(null, "엄마 선물 추천해 줘");
  }

  @DisplayName("message가 비어 있으면 400을 반환한다")
  @Test
  void sendMessageWithBlankMessage() throws Exception {
    mockMvc.perform(post("/api/chat")
            .contentType(APPLICATION_JSON)
            .content("""
                {
                  "message": " "
                }
                """))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(chatService);
  }

  @DisplayName("message가 없으면 400을 반환한다")
  @Test
  void sendMessageWithoutMessage() throws Exception {
    mockMvc.perform(post("/api/chat")
            .contentType(APPLICATION_JSON)
            .content("""
                {
                  "sessionId": "%s"
                }
                """.formatted(UUID.randomUUID())))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(chatService);
  }
}
