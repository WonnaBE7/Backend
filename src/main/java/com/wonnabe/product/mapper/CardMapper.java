package com.wonnabe.product.mapper;

import com.wonnabe.product.domain.CardProductVO;
import com.wonnabe.product.domain.UserCardVO;
import com.wonnabe.product.dto.UserCardDTO;
import org.apache.ibatis.annotations.Param;

public interface CardMapper {
    CardProductVO findById(long productId); // 카드 ID로 카드 정보를 가져옴

    UserCardVO findUserCardByproductId(
            @Param("productId") long productId, // Param은 파라미터에 이름을 지정함
            @Param("userId") String userId
    ); // 카드 ID와 userID로 사용자 카드 정보를 가져옴

    UserCardDTO findUserCardDetailById(
            @Param("productId") long productId, // Param은 파라미터에 이름을 지정함
            @Param("userId") String userId
    );
}
