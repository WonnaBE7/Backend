package com.wonnabe.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.user.dto.DiagnosisHistoryResponse;
import com.wonnabe.user.dto.UpdateUserRequest;
import com.wonnabe.user.dto.UpdateWonnabeRequest;
import com.wonnabe.user.dto.UserInfoResponse;
import com.wonnabe.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로그인한 사용자의 정보를 조회합니다.
     * - SecurityContext에 저장된 사용자 정보를 기반으로
     *   사용자 ID, 이름, 이메일 정보를 추출하여 반환합니다.
     *
     * @param user SecurityContext에서 주입된 인증 사용자(CustomUser)
     * @return UserInfoResponse 객체 (userId, name, email 포함)
     */
    public UserInfoResponse getUserInfo(CustomUser user) {
        return UserInfoResponse.builder()
                .userId(user.getUser().getUserId())
                .name(user.getUser().getName())
                .email(user.getUser().getEmail())
                .build();
    }

    /**
     * 로그인한 사용자의 이름 또는 비밀번호를 수정합니다.
     * - 요청 객체의 이름 및 비밀번호 정보를 기반으로 DB에 반영
     * - 비밀번호는 암호화하여 저장합니다.
     *
     * @param user SecurityContext에서 주입된 인증 사용자(CustomUser)
     * @param request UpdateUserRequest 객체 (수정할 이름, 비밀번호 포함)
     */
    public void updateUserInfo(CustomUser user, UpdateUserRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        userMapper.updateUser(user.getUser().getUserId(), request.getName(), encodedPassword);
    }

    /**
     * 로그인한 사용자의 워너비 선택 정보를 수정합니다.
     * - 사용자가 선택한 워너비 ID 리스트를 JSON 형태로 변환하여 DB에 저장합니다.
     * - User_Info 테이블의 selected_wonnabe_ids 필드에 저장됩니다.
     *
     * @param user SecurityContext에서 주입된 인증 사용자(CustomUser)
     * @param request UpdateWonnabeRequest 객체 (선택된 워너비 ID 리스트 포함)
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
     * - diagnosis_history 테이블에서 해당 사용자의 과거 진단 기록들을 조회합니다.
     * - Financial_Tendency_Type과 조인하여 타입명을 포함한 정보를 반환합니다.
     *
     * @param userId 조회할 사용자의 UUID
     * @return DiagnosisHistoryResponse 객체 (성공 여부와 진단 히스토리 리스트 포함)
     */
    public DiagnosisHistoryResponse getNowmeHistory(String userId) {
        List<DiagnosisHistoryResponse.DiagnosisHistoryItem> historyItems =
                userMapper.selectDiagnosisHistory(userId);

        return DiagnosisHistoryResponse.builder()
                .isSuccess(true)
                .response(historyItems)
                .build();
    }
}
