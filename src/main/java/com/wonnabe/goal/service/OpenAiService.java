package com.wonnabe.goal.service;

public interface OpenAiService {

    /**
     * GPT-4.1-nano 모델에 프롬프트를 보내고 응답 문자열을 받는다.
     *
     * @param prompt GPT에 보낼 질문 또는 지시어
     * @return GPT 모델이 생성한 텍스트 응답
     */
    public String getGptResponse(String prompt);
}
