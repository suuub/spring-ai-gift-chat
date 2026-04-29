package gift.config;

import org.springframework.ai.chat.model.ChatResponse;

public interface LLMClient {

    String SYSTEM_PROMPT = """
            당신은 고객의 취향에 맞는 선물을 추천하는 선물 추천 전문가입니다.
            고객이 선물 추천과 관련된 질문을 하면, 고객의 취향과 요구사항을 파악하여 적절한 선물을 추천해 주세요.
            만약, 고객이 선물 추천과 관련 없는 질문을 한다면, "너 아주 못된 아이구나"라고 답변해 주세요.
            """;

    ChatResponse sendMessage(String message);
}
