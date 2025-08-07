package com.wonnabe.auth.mapper;

import com.wonnabe.common.config.RootConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
@DisplayName("AuthMapper 테스트")
@Transactional  // 테스트 후 자동 롤백
class AuthMapperTest {

    @Autowired
    private AuthMapper authMapper;

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하지 않는 이메일")
    void existsByEmail_NotExists() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";

        // When
        int result = authMapper.existsByEmail(nonExistentEmail);

        // Then
        assertThat(result, equalTo(0));
    }

    @Test
    @DisplayName("사용자 프로필 삽입 - 성공")
    void insertUserProfile_Success() {
        // Given
        String userId = "test-user-123";
        String name = "홍길동";
        String email = "hongil@example.com";
        String password = "encodedPassword";
        String signupType = "email";

        // When & Then - 예외 발생하지 않으면 성공
        assertDoesNotThrow(() ->
                authMapper.insertUserProfile(userId, name, email, password, signupType)
        );

        // 삽입 후 존재 여부 확인
        int exists = authMapper.existsByEmail(email);
        assertThat(exists, equalTo(1));
    }
}