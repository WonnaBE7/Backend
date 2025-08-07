package com.wonnabe.codef.mapper;

import com.wonnabe.codef.domain.UserCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssetCardMapper {

    /**
     * 카드 정보를 Upsert합니다 (기존 데이터가 있으면 update, 없으면 insert).
     *
     * @param card UserCard 객체
     * @return 영향받은 행 수
     */
    int upsert(UserCard card);

    /**
     * 카드 이름으로부터 유사하게 product_id를 조회합니다.
     *
     * 예: "nori 체크카드(RF)" → "노리체크카드"
     *
     * @param cardName 카드 이름 (입력받은 원본)
     * @return 매칭된 product_id (없으면 null)
     */
    Long findProductIdByKeyword(@Param("cardName") String cardName,
                                @Param("institutionCode") String institutionCode);

}
