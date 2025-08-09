package com.wonnabe.user.service;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.user.dto.*;
import com.wonnabe.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로그인한 사용자의 정보를 조회합니다.
     */
    public UserInfoResponse getUserInfo(CustomUser user) {
        String userId = user.getUser().getUserId();

        // 사용자 기본 정보 조회
        Map<String, Object> userInfoMap = userMapper.selectUserInfo(userId);

        // User_Info가 없는 경우 기본값으로 응답
        if (userInfoMap == null || userInfoMap.get("userId") == null) {
            return UserInfoResponse.builder()
                    .userId(user.getUser().getUserId())
                    .name(user.getUser().getName())
                    .email(user.getUser().getEmail())
                    .nowME(null)
                    .wonnaBE(new ArrayList<>())
                    .job(null)
                    .monthlyIncome(null)
                    .build();
        }

        // nowME (현재 진단된 금융성향) 조회
        String nowME = null;
        Object nowmeIdObj = userInfoMap.get("nowmeId");
        if (nowmeIdObj != null) {
            Integer nowmeId = (Integer) nowmeIdObj;
            nowME = userMapper.selectFinancialTendencyName(nowmeId);
        }

        // wonnaBE (선택한 워너비들) 조회
        List<String> wonnaBE = userMapper.selectFinancialTendencyNames(userId);
        if (wonnaBE == null) {
            wonnaBE = new ArrayList<>();
        }

        // monthlyIncome 타입 변환 처리
        Long monthlyIncome = null;
        Object monthlyIncomeObj = userInfoMap.get("monthlyIncome");
        if (monthlyIncomeObj != null) {
            if (monthlyIncomeObj instanceof BigDecimal) {
                monthlyIncome = ((BigDecimal) monthlyIncomeObj).longValue();
            } else if (monthlyIncomeObj instanceof Long) {
                monthlyIncome = (Long) monthlyIncomeObj;
            } else if (monthlyIncomeObj instanceof Integer) {
                monthlyIncome = ((Integer) monthlyIncomeObj).longValue();
            }
        }

        return UserInfoResponse.builder()
                .userId((String) userInfoMap.get("userId"))
                .name((String) userInfoMap.get("name"))
                .email((String) userInfoMap.get("email"))
                .nowME(nowME)
                .wonnaBE(wonnaBE)
                .job((String) userInfoMap.get("job"))
                .monthlyIncome(monthlyIncome)
                .build();
    }

    /**
     * 로그인한 사용자의 이름 또는 비밀번호를 수정합니다.
     */
    public void updateUserInfo(CustomUser user, UpdateUserRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        userMapper.updateUser(user.getUser().getUserId(), request.getName(), encodedPassword);
    }

    /**
     * 로그인한 사용자의 워너비 선택 정보를 수정합니다.
     */
    public void updateWonnabe(CustomUser user, UpdateWonnabeRequest request) {
        StringBuilder json = new StringBuilder("[");
        List<Integer> ids = request.getSelectedWonnabeIds();

        for (int i = 0; i < ids.size(); i++) {
            json.append(ids.get(i));
            if (i < ids.size() - 1) {
                json.append(", ");
            }
        }
        json.append("]");

        userMapper.updateWonnabe(user.getUser().getUserId(), json.toString());
    }

    /**
     * 특정 사용자의 진단 결과 히스토리를 조회합니다. (최근 12개월 월별 최신 데이터)
     */
    public DiagnosisHistoryResponse getNowmeHistory(String userId) {
        List<DiagnosisHistoryResponse.DiagnosisHistoryItem> historyItems =
                userMapper.selectDiagnosisHistoryLast12Months(userId);

        return DiagnosisHistoryResponse.builder()
                .isSuccess(true)
                .response(historyItems)
                .build();
    }

    /**
     * 사용자 상세 정보를 조회합니다.
     */
    public UserDetailResponse getUserDetail(String userId) {
        UserDetailResponse.UserDetailData userDetail = userMapper.selectUserDetail(userId);

        if (userDetail == null) {
            return UserDetailResponse.builder()
                    .code(404)
                    .message("사용자를 찾을 수 없습니다.")
                    .data(null)
                    .build();
        }

        return UserDetailResponse.builder()
                .code(200)
                .message("성공")
                .data(userDetail)
                .build();
    }

    /**
     * 사용자 상세 정보를 등록합니다.
     */
    public void createUserDetail(UserDetailRequest request) {
        int exists = userMapper.checkUserDetailExists(request.getUserId());
        if (exists > 0) {
            throw new RuntimeException("이미 등록된 사용자입니다.");
        }

        userMapper.insertUserDetail(request);
    }

    /**
     * 사용자 상세 정보를 수정하고 변경된 필드 목록을 반환합니다.
     */
    public List<String> updateUserDetail(UserDetailRequest request) {
        int exists = userMapper.checkUserDetailExists(request.getUserId());
        if (exists == 0) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        List<String> updatedFields = new ArrayList<>();
        if (request.getLifestyleSmoking() != null) updatedFields.add("lifestyleSmoking");
        if (request.getLifestyleFamilyMedical() != null) updatedFields.add("lifestyleFamilyMedical");
        if (request.getLifestyleBeforeDiseases() != null) updatedFields.add("lifestyleBeforeDiseases");
        if (request.getLifestyleExerciseFreq() != null) updatedFields.add("lifestyleExerciseFreq");
        if (request.getLifestyleAlcoholFreq() != null) updatedFields.add("lifestyleAlcoholFreq");
        if (request.getIncomeSourceType() != null) updatedFields.add("incomeSourceType");
        if (request.getIncomeEmploymentStatus() != null) updatedFields.add("incomeEmploymentStatus");
        if (request.getHouseholdSize() != null) updatedFields.add("householdSize");
        if (request.getIncomeJobType() != null) updatedFields.add("incomeJobType");

        userMapper.updateUserDetail(request);
        return updatedFields;
    }
}