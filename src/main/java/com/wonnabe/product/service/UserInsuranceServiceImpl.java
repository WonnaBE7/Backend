package com.wonnabe.product.service;

import com.wonnabe.product.domain.UserInsuranceVO;
import com.wonnabe.product.dto.UserInsuranceDetailDTO;
import com.wonnabe.product.mapper.UserInsuranceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * {@link UserInsuranceService}의 구현 클래스.
 */

@Log4j2
@Service("UserInsuranceServiceImpl")
@RequiredArgsConstructor
public class UserInsuranceServiceImpl implements UserInsuranceService {

    private final UserInsuranceMapper userInsuranceMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserInsuranceDetailDTO getDetailByProductId(String userId, Long productId) {
        // Mapper를 통해 DB에서 사용자의 특정 보험 정보를 조회합니다.
        UserInsuranceVO userInsuranceVO = userInsuranceMapper.findDetailByProductId(userId, productId);

        // 조회된 데이터가 없으면 null을 반환하여, 컨트롤러에서 404 처리를 하도록 합니다.
        if (userInsuranceVO == null) {
            return null;
        }

        // 조회된 VO 객체를 DTO로 변환하여 반환합니다.
        // DTO의 정적 메소드를 사용하여 변환 로직을 위임합니다.
        return UserInsuranceDetailDTO.from(userInsuranceVO);
    }

    @Override
    public String existsById(Long productId) {
        return "";
    }
}
