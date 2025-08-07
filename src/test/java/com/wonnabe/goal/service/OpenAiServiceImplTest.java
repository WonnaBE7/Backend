package com.wonnabe.goal.service;

import com.wonnabe.common.config.RootConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
@WebAppConfiguration
@Log4j2
class OpenAiServiceImplTest {

    @Autowired
    private OpenAiService openAiService;

    @Test
    @DisplayName("실제 GPT API 프롬프트 확인 테스트 (수동 실행용)")
    @Disabled("과금 방지를 위해 기본적으로 비활성화")
    void getGptResponse() {
        String aiResponse = openAiService.getGptResponse("안녕?");

        log.info(aiResponse);
    }
}