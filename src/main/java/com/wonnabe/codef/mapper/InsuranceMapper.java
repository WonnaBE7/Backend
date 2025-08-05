package com.wonnabe.codef.mapper;

import com.wonnabe.codef.domain.UserInsurance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InsuranceMapper {

    List<UserInsurance> findByUserId(@Param("userId") String userId);

    void insert(@Param("insurance") UserInsurance insurance);

    void update(@Param("insurance") UserInsurance insurance);

    void deleteById(@Param("id") Long id);
}
