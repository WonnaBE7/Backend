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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, RedisConfig.class})
@Log4j2
class RootConfigTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    @DisplayName("데이터소스(DataSource) 연결 테스트")
    void testDataSourceConnection() {
        try (Connection con = dataSource.getConnection()) {
            log.info("DataSource Connection 성공: {}", con);
            assertNotNull(con, "DataSource로부터 받은 Connection은 null이 아니어야 합니다.");
        } catch (Exception e) {
            fail("DataSource 연결 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("MyBatis SqlSessionFactory 세션 열기 테스트")
    void testSqlSessionFactory() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            log.info("SqlSession 생성 성공: {}", session);
            assertNotNull(session, "SqlSessionFactory로부터 받은 SqlSession은 null이 아니어야 합니다.");

            Connection con = session.getConnection();
            log.info("SqlSession으로부터 Connection 획득 성공: {}", con);
            assertNotNull(con, "SqlSession으로부터 받은 Connection은 null이 아니어야 합니다.");
        } catch (Exception e) {
            fail("SqlSessionFactory 테스트 실패: " + e.getMessage());
        }
    }
}