package com.wonnabe.nowme.service;

import com.wonnabe.nowme.domain.PersonaVector;
import com.wonnabe.nowme.domain.UserVector;
import com.wonnabe.nowme.dto.NowMeRequestDTO;
import com.wonnabe.nowme.dto.NowMeResponseDTO;
import com.wonnabe.nowme.evaluation.ActivityEvaluator;
import com.wonnabe.nowme.evaluation.PlanningEvaluator;
import com.wonnabe.nowme.evaluation.RiskEvaluator;
import com.wonnabe.nowme.evaluation.SpendingEvaluator;
import com.wonnabe.nowme.mapper.NowMeMapper;
import com.wonnabe.nowme.utils.SimilarityCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NowMe 진단 서비스
 * - 4개 축(금융활동성, 소비패턴, 계획방식, 리스크성향) 점수 계산
 * - 12개 페르소나와 유사도 비교하여 최적 페르소나 도출
 * - 진단 결과 저장 및 User_Info 업데이트
 */
@Service
public class NowMeService {

    private static final Logger log = LoggerFactory.getLogger(NowMeService.class);

    @Autowired
    private ActivityEvaluator activityEvaluator;

    @Autowired
    private SpendingEvaluator spendingEvaluator;

    @Autowired
    private PlanningEvaluator planningEvaluator;

    @Autowired
    private RiskEvaluator riskEvaluator;

    @Autowired
    private NowMeMapper nowMeMapper;

//    @Autowired
//    private ObjectMapper objectMapper;

    /**
     * 🔥 12개 페르소나 기준 벡터 정의
     * [금융활동성, 소비패턴, 계획방식, 리스크성향] 순서 (0~1 범위)
     */
    private static final List<PersonaVector> PERSONA_VECTORS = Arrays.asList(
            // 계획적 + 안정형
            new PersonaVector("가족중심형", new double[]{0.4, 0.2, 0.9, 0.1}),     // 낮은활동성, 필수소비, 매우계획적, 안전투자
            new PersonaVector("루틴러형", new double[]{0.3, 0.1, 0.8, 0.2}),      // 낮은활동성, 고정소비, 계획적, 안전투자
            new PersonaVector("미래계획형", new double[]{0.6, 0.3, 0.9, 0.4}),    // 중간활동성, 목적소비, 매우계획적, 중간투자
            new PersonaVector("자린고비형", new double[]{0.2, 0.0, 0.8, 0.1}),    // 낮은활동성, 최소소비, 계획적, 초안전투자

            // 계획적 + 적극형
            new PersonaVector("균형 성장형", new double[]{0.7, 0.5, 0.7, 0.6}),   // 높은활동성, 균형소비, 계획적, 적극투자
            new PersonaVector("새싹 투자자형", new double[]{0.5, 0.4, 0.6, 0.5}), // 중간활동성, 중간소비, 중간계획, 학습투자

            // 즉흥적 + 안정형
            new PersonaVector("느긋한 관망형", new double[]{0.1, 0.3, 0.3, 0.0}), // 최소활동성, 소극소비, 즉흥적, 현금보유
            new PersonaVector("소확행형", new double[]{0.3, 0.6, 0.2, 0.2}),      // 낮은활동성, 감성소비, 즉흥적, 안전투자

            // 즉흥적 + 적극형
            new PersonaVector("경험소비형", new double[]{0.5, 0.8, 0.4, 0.3}),    // 중간활동성, 체험소비, 중간계획, 중간투자
            new PersonaVector("YOLO형", new double[]{0.4, 0.9, 0.1, 0.4}),       // 중간활동성, 최대소비, 즉흥적, 중간투자
            new PersonaVector("무계획형", new double[]{0.2, 0.7, 0.0, 0.2}),     // 낮은활동성, 충동소비, 무계획, 안전투자

            // 고위험 추구형
            new PersonaVector("공격투자형", new double[]{0.9, 0.6, 0.7, 0.9})     // 최고활동성, 중간소비, 계획적, 고위험투자
    );

    /**
     * 🔸 페르소나명 → ID 매핑 (Financial_Tendency_Type 테이블 기준)
     */
    private static final Map<String, Integer> PERSONA_NAME_TO_ID = new HashMap<String, Integer>() {{
        put("자린고비형", 1);
        put("가족중심형", 2);
        put("루틴러형", 3);
        put("미래계획형", 4);
        put("균형 성장형", 5);
        put("새싹 투자자형", 6);
        put("느긋한 관망형", 7);
        put("소확행형", 8);
        put("경험소비형", 9);
        put("YOLO형", 10);
        put("무계획형", 11);
        put("공격투자형", 12);
    }};

    /**
     * 🔹 NowMe 페르소나 진단 수행
     * @param userId 사용자 ID
     * @param requestDTO 설문 응답 데이터
     * @return 진단 결과 (페르소나명)
     */
    public NowMeResponseDTO diagnose(Long userId, NowMeRequestDTO requestDTO) {
        try {
            log.info("🚀 NowMe 진단 시작 - userId: {}", userId);

            // 1. 4개 축별 점수 계산
            UserVector userVector = calculateUserVector(userId, requestDTO);
            log.info("📊 사용자 벡터 - {}", userVector);

            // 2. 12개 페르소나와 유사도 계산
            PersonaMatchResult matchResult = findBestMatchingPersona(userVector);
            log.info("🎯 최적 페르소나 - {} (유사도: {})", matchResult.personaName, matchResult.similarity);

            // 3. 진단 결과 저장
            saveDiagnosisHistory(userId, userVector, matchResult.personaName, matchResult.similarity);

            // 4. User_Info 업데이트
            updateUserNowmeId(userId, matchResult.personaName);

            // 5. 진단 결과 반환
            return NowMeResponseDTO.success(matchResult.personaName);

        } catch (Exception e) {
            log.error("❗ NowMe 진단 실패 - userId: {}", userId, e);
            return NowMeResponseDTO.failure();
        }
    }

    /**
     * 🔸 사용자 벡터 계산 (4개 축 점수)
     */
    private UserVector calculateUserVector(Long userId, NowMeRequestDTO requestDTO) {
        // 각 축별 정량 + 정성 점수 계산
        double activityScore = activityEvaluator.calculateFinalScore(userId, requestDTO);
        double spendingScore = spendingEvaluator.calculateFinalScore(userId, requestDTO);
        double planningScore = planningEvaluator.calculateFinalScore(userId, requestDTO);
        double riskScore = riskEvaluator.calculateFinalScore(userId, requestDTO);

        log.debug("📈 축별 최종점수 - 활동성: {}, 소비패턴: {}, 계획방식: {}, 리스크: {}",
                activityScore, spendingScore, planningScore, riskScore);

        return new UserVector(activityScore, spendingScore, planningScore, riskScore);
    }

    /**
     * 🔸 최적 페르소나 찾기 (유사도 기반)
     */
    private PersonaMatchResult findBestMatchingPersona(UserVector userVector) {
        String bestPersona = null;
        double maxSimilarity = -1.0;

        double[] userArray = userVector.toArray();

        for (PersonaVector persona : PERSONA_VECTORS) {
            double[] personaArray = persona.toArray();

            // 코사인 유사도와 유클리드 유사도 평균으로 최종 유사도 계산
            double cosineSim = SimilarityCalculator.cosineSimilarity(userArray, personaArray);
            double euclideanSim = SimilarityCalculator.euclideanSimilarity(userArray, personaArray);
            double finalSimilarity = (cosineSim * 0.6) + (euclideanSim * 0.4); // 코사인 60%, 유클리드 40%

            log.debug("🔍 {} - 코사인: {}, 유클리드: {}, 최종: {}",
                    persona.getPersonaName(),
                    roundTo3Decimals(cosineSim),
                    roundTo3Decimals(euclideanSim),
                    roundTo3Decimals(finalSimilarity));

            if (finalSimilarity > maxSimilarity) {
                maxSimilarity = finalSimilarity;
                bestPersona = persona.getPersonaName();
            }
        }

        log.info("✨ 최고 유사도: {} ({})", roundTo3Decimals(maxSimilarity), bestPersona);
        return new PersonaMatchResult(bestPersona, maxSimilarity);
    }

    /**
     * 🔸 진단 결과 저장
     */
    private void saveDiagnosisHistory(Long userId, UserVector userVector, String personaName, double similarity) {
        try {
            // 페르소나명 → ID 변환
            Integer nowmeId = PERSONA_NAME_TO_ID.get(personaName);
            if (nowmeId == null) {
                log.warn("❗ 알 수 없는 페르소나명: {}", personaName);
                nowmeId = 1; // 기본값 (자린고비형)
            }

            // UserVector를 JSON 배열로 변환
            double[] vectorArray = userVector.toArray();
            String userVectorJson = String.format("[%.3f,%.3f,%.3f,%.3f]",
                    vectorArray[0], vectorArray[1], vectorArray[2], vectorArray[3]);


            // 진단 이력 저장
            nowMeMapper.insertDiagnosisHistory(
                    userId.toString(),
                    nowmeId,
                    similarity,
                    userVectorJson
            );

            log.info("💾 진단 이력 저장 완료 - userId: {}, nowmeId: {}, similarity: {}",
                    userId, nowmeId, roundTo3Decimals(similarity));

        } catch (Exception e) {
            log.error("❗ 진단 이력 저장 실패 - userId: {}", userId, e);
            // 저장 실패해도 진단 결과는 반환
        }
    }

    /**
     * 🔸 User_Info의 nowme_id 업데이트
     */
    private void updateUserNowmeId(Long userId, String personaName) {
        try {
            Integer nowmeId = PERSONA_NAME_TO_ID.get(personaName);
            if (nowmeId != null) {
                nowMeMapper.updateUserNowmeId(userId.toString(), nowmeId);
                log.info("🔄 User_Info 업데이트 완료 - userId: {}, nowmeId: {}", userId, nowmeId);
            }
        } catch (Exception e) {
            log.error("❗ User_Info 업데이트 실패 - userId: {}", userId, e);
            // 업데이트 실패해도 진단 결과는 반환
        }
    }

    /**
     * 🔸 소수점 3자리 반올림
     */
    private double roundTo3Decimals(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    /**
     * 🔸 페르소나 매칭 결과 내부 클래스
     */
    private static class PersonaMatchResult {
        final String personaName;
        final double similarity;

        PersonaMatchResult(String personaName, double similarity) {
            this.personaName = personaName;
            this.similarity = similarity;
        }
    }
}