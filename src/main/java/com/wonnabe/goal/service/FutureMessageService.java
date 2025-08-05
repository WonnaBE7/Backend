package com.wonnabe.goal.service;

import java.math.BigDecimal;

public interface FutureMessageService {
    public String generateFutureMessage(String nowmeName, String nowmeDescription, String goalName, BigDecimal targetAmount);

}
