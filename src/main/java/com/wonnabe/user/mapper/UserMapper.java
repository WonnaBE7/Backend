package com.wonnabe.user.mapper;

import com.wonnabe.user.dto.DiagnosisHistoryResponse;
import com.wonnabe.user.dto.UserDetailRequest;
import com.wonnabe.user.dto.UserDetailResponse;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 사용자 이름 및 비밀번호를 업데이트합니다.
     */
    void updateUser(@Param("userId") String userId,
                    @Param("name") String name,
                    @Param("passwordHash") String passwordHash);

    /**
     * 사용자의 워너비 선택 정보를 업데이트합니다.
     */
    void updateWonnabe(@Param("userId") String userId,
                       @Param("selectedWonnabeIds") String selectedWonnabeIds);

    /**
     * 특정 사용자의 진단 히스토리를 조회합니다.
     */
    List<DiagnosisHistoryResponse.DiagnosisHistoryItem> selectDiagnosisHistory(@Param("userId") String userId);

    /**
     * 사용자의 User_Info 정보를 조회합니다.
     * @param userId 사용자 ID
     * @return Map 형태로 User_Info 데이터 반환
     */
    /**
     * 기존 getUserInfo API용 메소드들
     */
    Map<String, Object> selectUserInfo(@Param("userId") String userId);

    String selectFinancialTendencyName(@Param("nowmeId") Integer nowmeId);

    List<String> selectFinancialTendencyNames(@Param("userId") String userId);

    /**
     * 사용자 상세 정보를 조회합니다.
     */
    UserDetailResponse.UserDetailData selectUserDetail(@Param("userId") String userId);

    /**
     * 사용자 상세 정보를 등록합니다.
     */
    void insertUserDetail(UserDetailRequest request);

    /**
     * 사용자 상세 정보를 수정합니다.
     */
    void updateUserDetail(UserDetailRequest request);

    /**
     * 사용자 상세 정보 존재 여부를 확인합니다.
     */
    int checkUserDetailExists(@Param("userId") String userId);
}