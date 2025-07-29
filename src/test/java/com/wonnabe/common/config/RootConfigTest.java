package com.wonnabe.common.config;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(SpringExtension.class) // JUnit5와 스프링 통합
@ContextConfiguration(classes = {RootConfig.class}) // 테스트 설정으로 RootConfig 사용
@Log4j2
class RootConfigTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    // DB연결 테스트
    @Test
    @DisplayName("DataSource 연결이 된다.")
    public void testDataSource() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            log.info("DataSource 준비 완료");
            log.info(con);
        }
    }
}