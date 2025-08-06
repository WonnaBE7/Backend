package com.wonnabe.goal.service;

import java.math.BigDecimal;

public interface FutureMessageService {

    /**
     * 목표 달성 후 미래의 자신이 과거의 자신에게 보내는 격려 메시지 생성
     * 사용자의 성향과 목표 정보를 바탕으로 개인화된 메시지를 AI로 생성
     * 생성에 실패할 경우 기본 메시지를 반환
     *
     * @param nowmeName        사용자의 현재 금옹 성향(Nowme), null인 경우 "나"로 대체
     * @param nowmeDescription 사용자의 현재 금융 성향 설명
     * @param goalName         달성할 목표의 이름
     * @param targetAmount     목표 금액 (원)
     * @return 생성된 미래 메시지 (최대 300자), 생성 실패 시 기본 격려 메시지
     */
    public String generateFutureMessage(String nowmeName, String nowmeDescription, String goalName, BigDecimal targetAmount);
}
