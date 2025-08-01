package com.wonnabe.nowme.service;

import com.wonnabe.nowme.dto.NowMeRequestDTO;
import com.wonnabe.nowme.dto.NowMeResponseDTO;
import com.wonnabe.nowme.utils.ScoreNormalizer;
import com.wonnabe.nowme.utils.SimilarityCalculator;
import com.wonnabe.nowme.utils.VectorCombiner;
import com.wonnabe.nowme.mapper.NowMeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NowMeService {

    private final NowMeMapper nowMeMapper;

    // 내부 상수: 정량/정성 가중치
    private static final double QUANT_WEIGHT = 0.6;
    private static final double QUAL_WEIGHT = 0.4;

    // 페르소나 기준 벡터 (예시 4개, 나머지 8개 추가 예정)
    private static final Map<String, double[]> PERSONA_VECTORS = Map.of(
            "자린고비형", new double[]{1.2, 4.5, 3.1, 2.0},
            "계획형", new double[]{3.8, 2.1, 4.7, 2.5},
            "투자형", new double[]{4.2, 3.0, 3.5, 4.1},
            "소비형", new double[]{2.1, 4.8, 2.2, 3.0}
            // TODO: 8개 페르소나 추가
    );

    /**
     * 메인 진단 메서드
     */
    public NowMeResponseDTO diagnose(Long userId, NowMeRequestDTO request) {

        // 1. 정량 점수 계산 (ex. 소비/저축/투자/대출 비율)
        double[] quantVector = getQuantitativeVector(userId);

        // 2. 정성 점수 계산 (설문 답변 기반)
        double[] qualVector = getQualitativeVector(request.getAnswers());

        // 3. 정규화 및 가중 평균 (6:4)
        double[] userVector = VectorCombiner.combine(quantVector, qualVector, QUANT_WEIGHT, QUAL_WEIGHT);

        // 4. 페르소나 매칭 (코사인 유사도 기반)
        String bestPersona = null;
        double maxSimilarity = -1;

        for (Map.Entry<String, double[]> entry : PERSONA_VECTORS.entrySet()) {
            double similarity = SimilarityCalculator.cosineSimilarity(userVector, entry.getValue());
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestPersona = entry.getKey();
            }
        }

        // 5. 결과 저장 (선택사항)
        saveDiagnosisHistory(userId, userVector, bestPersona, maxSimilarity);

        // 6. 응답 생성
        return NowMeResponseDTO.builder()
                .personaName(bestPersona)
                .userVector(new UserVector(userVector))
                .similarity(maxSimilarity)
                .diagnosedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 더미 정량 점수 계산 (ex. 소비/저축/투자/대출)
     */
    private double[] getQuantitativeVector(Long userId) {
        // TODO: 실제 DB 조회 후 정규화
        double[] raw = {40.0, 30.0, 20.0, 10.0};  // 예시
        return ScoreNormalizer.normalize(raw);
    }

    /**
     * 설문 답변 기반 정성 점수 계산
     */
    private double[] getQualitativeVector(List<Integer> answers) {
        double[] vector = new double[4];
        for (int i = 0; i < answers.size(); i++) {
            vector[i / 3] += answers.get(i);  // 3문항씩 묶기
        }
        for (int i = 0; i < 4; i++) {
            vector[i] /= 3.0;  // 평균
        }
        return ScoreNormalizer.normalize(vector);
    }

    /**
     * 진단 결과 저장 (선택적으로 확장 가능)
     */
    private void saveDiagnosisHistory(Long userId, double[] userVector, String personaName, double similarity) {
        // TODO: DB에 진단 결과 저장 (mapper 활용)
        // nowMeMapper.insertDiagnosisHistory(...);
    }
}