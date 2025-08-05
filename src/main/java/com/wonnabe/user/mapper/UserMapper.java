package com.wonnabe.user.mapper;

import com.wonnabe.user.dto.DiagnosisHistoryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 사용자 이름 및 비밀번호를 업데이트합니다.
     * - 사용자 ID를 기준으로 DB에서 해당 사용자의 정보를 찾아 수정합니다.
     * - 이름(name)과 비밀번호(passwordHash)를 모두 수정합니다.
     *
     * @param userId       수정 대상 사용자의 UUID
     * @param name         수정할 사용자 이름
     * @param passwordHash 수정할 사용자 비밀번호 (BCrypt 등으로 암호화된 값)
     */
    void updateUser(@Param("userId") String userId,
                    @Param("name") String name,
                    @Param("passwordHash") String passwordHash);

    /**
     * 사용자의 워너비 선택 정보를 업데이트합니다.
     * - User_Info 테이블의 selected_wonnabe_ids 필드를 JSON 형태로 저장합니다.
     *
     * @param userId              수정 대상 사용자의 UUID
     * @param selectedWonnabeIds  선택된 워너비 ID 배열 (JSON 문자열 형태)
     */
    void updateWonnabe(@Param("userId") String userId,
                       @Param("selectedWonnabeIds") String selectedWonnabeIds);

    /**
     * 특정 사용자의 진단 히스토리를 조회합니다.
     * - diagnosis_history 테이블과 Financial_Tendency_Type 테이블을 조인하여
     *   진단 날짜, 타입명, 점수 정보를 조회합니다.
     *
     * @param userId 조회할 사용자의 UUID
     * @return DiagnosisHistoryItem 리스트 (진단 히스토리 정보)
     */
    List<DiagnosisHistoryResponse.DiagnosisHistoryItem> selectDiagnosisHistory(@Param("userId") String userId);
}
