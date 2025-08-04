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
            // 계획적 + 안전형 (Planning 높음, Risk 낮음)
            new PersonaVector("자린고비형", new double[]{0.1, 0.0, 0.9, 0.0}),    // 극도로 보수적
            new PersonaVector("가족중심형", new double[]{0.3, 0.2, 0.9, 0.1}),     // 가족 중심 계획
            new PersonaVector("루틴러형", new double[]{0.2, 0.1, 0.8, 0.1}),      // 고정 패턴
            new PersonaVector("미래계획형", new double[]{0.6, 0.3, 1.0, 0.3}),    // 장기 계획

            // 계획적 + 적극형 (Planning 높음, Risk 중간~높음)
            new PersonaVector("균형 성장형", new double[]{0.8, 0.5, 0.8, 0.7}),   // 균형잡힌 성장
            new PersonaVector("공격투자형", new double[]{1.0, 0.6, 0.8, 1.0}),    // 최고 리스크

            // 중간형 (모든 축 중간값 - 하나만 남기고 차별화)
            new PersonaVector("새싹 투자자형", new double[]{0.7, 0.3, 0.5, 0.6}), // 학습형 (중간 전체)

            // 즉흥적 + 안전형 (Planning 낮음, Risk 낮음)
            new PersonaVector("느긋한 관망형", new double[]{0.0, 0.3, 0.1, 0.0}), // 극도로 수동적
            new PersonaVector("소확행형", new double[]{0.3, 0.7, 0.2, 0.2}),      // 감성 소비 특화

            // 즉흥적 + 적극형 (Planning 낮음, Risk 중간~높음)
            new PersonaVector("무계획형", new double[]{0.1, 0.9, 0.0, 0.2}),     // 극도로 무계획
            new PersonaVector("YOLO형", new double[]{0.4, 1.0, 0.0, 0.5}),       // 최고 소비
            new PersonaVector("경험소비형", new double[]{0.6, 0.9, 0.3, 0.4})    // 체험 중심
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
     * @param userId 사용자 ID (String)
     * @param requestDTO 설문 응답 데이터
     * @return 진단 결과 (페르소나명)
     */
    public NowMeResponseDTO diagnose(String userId, NowMeRequestDTO requestDTO) {
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
//            return NowMeResponseDTO.success(matchResult.personaName);
            double[] scores = userVector.toArray();
            return NowMeResponseDTO.successWithScores(
                    matchResult.personaName,
                    scores[0], // activityScore
                    scores[1], // spendingScore
                    scores[2], // planningScore
                    scores[3], // riskScore
                    matchResult.similarity
            );

        } catch (Exception e) {
            log.error("❗ NowMe 진단 실패 - userId: {}", userId, e);
            return NowMeResponseDTO.failure();
        }
    }

    /**
     * 🔸 사용자 벡터 계산 (4개 축 점수)
     */
    private UserVector calculateUserVector(String userId, NowMeRequestDTO requestDTO) {
        // 각 축별 정량 + 정성 점수 계산
        double activityScore = activityEvaluator.calculateFinalScore(userId, requestDTO);
        double spendingScore = spendingEvaluator.calculateFinalScore(userId, requestDTO);
        double planningScore = planningEvaluator.calculateFinalScore(userId, requestDTO);
        double riskScore = riskEvaluator.calculateFinalScore(userId, requestDTO);

        log.debug("📈 축별 최종점수 - 활동성: {}, 소비패턴: {}, 계획방식: {}, 리스크: {}",
                activityScore, spendingScore, planningScore, riskScore);
        // 🔥 상세 분석 로그 추가 ‼️️️‼️️️‼️️️‼️️️‼️️️
        System.out.println("=== 축별 점수 분석 ===");
        System.out.println("Activity: " + activityScore);
        System.out.println("Spending: " + spendingScore);
        System.out.println("Planning: " + planningScore);
        System.out.println("Risk: " + riskScore);

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

            System.out.println(persona.getPersonaName() + ": " + finalSimilarity);

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
    private void saveDiagnosisHistory(String userId, UserVector userVector, String personaName, double similarity) {
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

            // 진단 이력 저장 (String userId 직접 사용)
            nowMeMapper.insertDiagnosisHistory(
                    userId,  // toString() 제거!
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
//    private void saveDiagnosisHistory(String userId, UserVector userVector, String personaName, double similarity) {
//        try {
//            System.out.println("🔥🔥🔥 진단 이력 저장 시작 - userId: " + userId);
//
//            // 페르소나명 → ID 변환
//            Integer nowmeId = PERSONA_NAME_TO_ID.get(personaName);
//            if (nowmeId == null) {
//                System.out.println("❗ 알 수 없는 페르소나명: " + personaName);
//                nowmeId = 1; // 기본값 (자린고비형)
//            }
//
//            // UserVector를 JSON 배열로 변환
//            double[] vectorArray = userVector.toArray();
//            String userVectorJson = String.format("[%.3f,%.3f,%.3f,%.3f]",
//                    vectorArray[0], vectorArray[1], vectorArray[2], vectorArray[3]);
//
//            System.out.println("🔥🔥🔥 DB 저장 시도 - nowmeId: " + nowmeId + ", vector: " + userVectorJson);
//
//            // 진단 이력 저장 (String userId 직접 사용)
//            nowMeMapper.insertDiagnosisHistory(
//                    userId,
//                    nowmeId,
//                    similarity,
//                    userVectorJson
//            );
//
//            System.out.println("🔥🔥🔥 진단 이력 저장 성공!");
//
//        } catch (Exception e) {
//            System.out.println("🔥🔥🔥 진단 이력 저장 실패: " + e.getMessage());
//            e.printStackTrace();
//            // 저장 실패해도 진단 결과는 반환
//        }
//    }

    /**
     * 🔸 User_Info의 nowme_id 업데이트
     */
    private void updateUserNowmeId(String userId, String personaName) {
        try {
            Integer nowmeId = PERSONA_NAME_TO_ID.get(personaName);
            if (nowmeId != null) {
                nowMeMapper.updateUserNowmeId(userId, nowmeId);  // toString() 제거!
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