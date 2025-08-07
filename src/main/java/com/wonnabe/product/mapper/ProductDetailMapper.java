package com.wonnabe.product.mapper;

import com.wonnabe.product.domain.InsuranceProductVO;
import com.wonnabe.product.domain.SavingsProductVO;
import com.wonnabe.product.dto.BasicUserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductDetailMapper {
    SavingsProductVO findSavingProductById(String productId);

    BasicUserInfo findBasicUserInfoById(String userId);

    InsuranceProductVO findInsuranceProductById(String productId);
}
