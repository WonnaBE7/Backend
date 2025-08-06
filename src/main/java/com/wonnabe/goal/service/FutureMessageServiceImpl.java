package com.wonnabe.goal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Log4j2
public class FutureMessageServiceImpl implements FutureMessageService {

    private final OpenAiService openAiService;

    @Override
    public String generateFutureMessage(String nowmeName, String nowmeDescription, String goalName, BigDecimal targetAmount) {
        try {
            String prompt = buildPrompt(nowmeName, nowmeDescription, goalName, targetAmount);
            log.info("Generated prompt: {}", prompt);

            String gptMessage = openAiService.getGptResponse(prompt);
            return gptMessage != null && !gptMessage.isEmpty()
                    ? gptMessage
                    : getDefaultSuccessMessage();
        } catch (Exception e) {
            log.error("Failed to generate future message", e);
            return getDefaultErrorMessage();
        }
    }

    private String buildPrompt(String nowmeName, String nowmeDescription, String goalName, BigDecimal targetAmount) {
        String name = nowmeName != null ? nowmeName : "나";
        String description = nowmeDescription != null ? nowmeDescription : "";

        return String.format(
                "역할: **'%s'** 목표를 통해 **%s원**을 달성한 미래의 **%s**\n" +
                        "현재 금융 성향: %s\n\n" +
                        "임무: 과거의 나에게 보내는 300자 이내 메시지 작성\n\n" +
                        "반드시 포함할 내용:\n" +
                        "- '안녕, 과거의 나야'로 시작\n" +
                        "- '%s' 목표 달성 언급\n" +
                        "- '%s원' 금액 언급 (3자리마다 콤마 포함)\n" +
                        "- 달성 후 변화된 삶\n" +
                        "- 현재의 나에게 주는 격려\n\n" +
                        "작성 스타일: 따뜻하고 구체적이며 개인적인 톤으로, 입력받은 정보를 정확히 활용하여 작성",
                goalName, targetAmount.toPlainString(), name, description, goalName, targetAmount.toPlainString()
        );
    }

    private String getDefaultSuccessMessage() {
        return "목표를 꼭 달성하고 미래의 나에게 칭찬을 아끼지 마세요!";
    }

    private String getDefaultErrorMessage() {
        return "목표를 꼭 이룰 수 있을 거에요!";
    }
}
