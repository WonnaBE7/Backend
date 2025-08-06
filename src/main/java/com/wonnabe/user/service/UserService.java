package com.wonnabe.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.user.dto.*;
import com.wonnabe.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    /**
     * 로그인한 사용자의 정보를 조회합니다. (기존 메소드 수정)
     */
    public UserInfoResponse getUserInfo(CustomUser user) {
        String userId = user.getUser().getUserId();

        // 기본 사용자 정보 조회
        Map<String, Object> userInfoMap = userMapper.selectUserInfo(userId);
        if (userInfoMap == null) {
            // User_Info가 없는 경우 기본 정보만 반환
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

        // nowME 조회 (현재 진단된 금융성향)
        String nowME = null;
        Object nowmeIdObj = userInfoMap.get("nowmeId");
        if (nowmeIdObj != null) {
            Integer nowmeId = (Integer) nowmeIdObj;
            nowME = userMapper.selectFinancialTendencyName(nowmeId);
        }

        // wonnaBE 조회 (선택한 워너비들)
        List<String> wonnaBE = userMapper.selectFinancialTendencyNames(userId);

        // monthlyIncome 타입 변환 처리 (BigDecimal → Long)
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
                .wonnaBE(wonnaBE != null ? wonnaBE : new ArrayList<>())
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
        // [1, 2, 3] 형태의 JSON 문자열 직접 생성
        StringBuilder json = new StringBuilder("[");
        List<Integer> ids = request.getSelected_wonnabe_ids();

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
     * 특정 사용자의 진단 결과 히스토리를 조회합니다.
     */
    public DiagnosisHistoryResponse getNowmeHistory(String userId) {
        List<DiagnosisHistoryResponse.DiagnosisHistoryItem> historyItems =
                userMapper.selectDiagnosisHistory(userId);

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
        // 이미 존재하는지 확인
        int exists = userMapper.checkUserDetailExists(request.getUser_id());
        if (exists > 0) {
            throw new RuntimeException("이미 등록된 사용자입니다.");
        }

        userMapper.insertUserDetail(request);
    }

    /**
     * 사용자 상세 정보를 수정하고 변경된 필드 목록을 반환합니다.
     */
    public List<String> updateUserDetail(UserDetailRequest request) {
        // 사용자 존재 여부 확인
        int exists = userMapper.checkUserDetailExists(request.getUser_id());
        if (exists == 0) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        // 수정된 필드 목록 생성
        List<String> updatedFields = new ArrayList<>();
        if (request.getLifestyle_smoking() != null) updatedFields.add("lifestyle_smoking");
        if (request.getLifestyle_drinking() != null) updatedFields.add("lifestyle_drinking");
        if (request.getLifestyle_exercise() != null) updatedFields.add("lifestyle_exercise");
        if (request.getHousehold_size() != null) updatedFields.add("household_size");
        if (request.getLifestyle_family_medical() != null) updatedFields.add("lifestyle_family_medical");
        if (request.getLifestyle_before_diseases() != null) updatedFields.add("lifestyle_before_diseases");
        if (request.getIncome_job_type() != null) updatedFields.add("income_job_type");

        userMapper.updateUserDetail(request);
        return updatedFields;
    }
}