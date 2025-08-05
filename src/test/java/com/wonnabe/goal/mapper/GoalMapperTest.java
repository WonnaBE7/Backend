package com.wonnabe.goal.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wonnabe.common.config.RootConfig;
import com.wonnabe.product.domain.SavingsProductVO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j2
class GoalMapperTest {

    @Autowired
    private GoalMapper goalMapper;

    @Test
    void getGoalList() {
    }

    @Test
    void getGoal() {
    }

    @Test
    void createGoal() {
    }

    @Test
    void getNowmeIdByUserId() {
        // given
        String userId = "02747659-2dd1-41d6-80a7-131b9ddfac97";

        // when
        Integer nowmeId = goalMapper.getNowmeIdByUserId(userId);

        // then
        log.info("userId: " + userId);
        log.info("nowmeId: " + nowmeId);
    }

    @Test
    void getNowmeNameByNowmeId() {
        // given
        int nowmeId = 1;

        // when
        String nowmeName = goalMapper.getNowmeNameByNowmeId(nowmeId);

        // then
        log.info("nowmeId: " + nowmeId);
        log.info("nowmeName: " + nowmeName);
    }

    @Test
    void getSavingsProductList() throws JsonProcessingException {
        List<SavingsProductVO> list = goalMapper.getSavingsProductList();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT); // 들여쓰기 활성화
        String json = mapper.writeValueAsString(list);
        log.info("\ngetSavingsProductList:\n{}", json);
    }
}