//package com.wonnabe.nowme.mapper;
//
//import org.apache.ibatis.annotations.Mapper;
//import org.apache.ibatis.annotations.Param;
//
//@Mapper
//public interface NowMeMapper {
//
//    // 최근 진단 이력 조회 (userId로)
//    DiagnosisHistory findRecentHistory(@Param("userId") String userId);
//
//    // 진단 결과 저장
//    void insertDiagnosisHistory(DiagnosisHistory history);
//}