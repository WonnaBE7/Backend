//package com.wonnabe.asset.service;
//
//import com.wonnabe.asset.domain.*;
//import com.wonnabe.asset.mapper.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//public class AssetSaveService {
//
//    @Autowired private AssetMapper assetMapper;
//    @Autowired private SavingsMapper userSavingsMapper;
//    @Autowired private InsuranceMapper userInsuranceMapper;
//
//    public void saveChangedSavings(String userId, List<UserSavings> newList) {
//        List<UserSavings> existingList = userSavingsMapper.findByUserId(userId);
//
//        Map<String, UserSavings> existingMap = new HashMap<>();
//        for (UserSavings savings : existingList) {
//            String key = generateSavingsKey(savings); // 유일 기준
//            existingMap.put(key, savings);
//        }
//
//        for (UserSavings incoming : newList) {
//            String key = generateSavingsKey(incoming);
//            if (existingMap.containsKey(key)) {
//                UserSavings existing = existingMap.get(key);
//                if (!incoming.equals(existing)) {
//                    incoming.setId(existing.getId());
//                    userSavingsMapper.update(incoming);
//                }
//                existingMap.remove(key);
//            } else {
//                userSavingsMapper.insert(incoming);
//            }
//        }
//
//        // 남은 기존 데이터 삭제 (예: 더 이상 존재하지 않는 저축상품)
//        for (UserSavings obsolete : existingMap.values()) {
//            userSavingsMapper.deleteById(obsolete.getId());
//        }
//    }
//
//    public void saveChangedInsurances(String userId, List<UserInsurance> newList) {
//        List<UserInsurance> existingList = userInsuranceMapper.findByUserId(userId);
//
//        Map<String, UserInsurance> existingMap = new HashMap<>();
//        for (UserInsurance ins : existingList) {
//            String key = generateInsuranceKey(ins); // 유일 기준
//            existingMap.put(key, ins);
//        }
//
//        for (UserInsurance incoming : newList) {
//            String key = generateInsuranceKey(incoming);
//            if (existingMap.containsKey(key)) {
//                UserInsurance existing = existingMap.get(key);
//                if (!incoming.equals(existing)) {
//                    incoming.setId(existing.getId());
//                    userInsuranceMapper.update(incoming);
//                }
//                existingMap.remove(key);
//            } else {
//                userInsuranceMapper.insert(incoming);
//            }
//        }
//
//        for (UserInsurance obsolete : existingMap.values()) {
//            userInsuranceMapper.deleteById(obsolete.getId());
//        }
//    }
//
//    private String generateSavingsKey(UserSavings s) {
//        return s.getProductId() + "_" + s.getStartDate(); // 예시 키: 상품ID + 가입일
//    }
//
//    private String generateInsuranceKey(UserInsurance i) {
//        return i.getProductId() + "_" + i.getStartDate(); // 예시 키: 상품ID + 가입일
//    }
//}
