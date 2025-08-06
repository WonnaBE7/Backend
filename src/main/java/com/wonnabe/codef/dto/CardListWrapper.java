package com.wonnabe.codef.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wonnabe.codef.domain.UserCard;
import lombok.Getter;
import lombok.Setter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardListWrapper {

    @JsonProperty("data")
    private CardListResponse data;

    public UserCard toUserCard(String userId) {
        UserCard card = new UserCard();
        card.setUserId(userId);
        card.setCardName(data.getResCardName());
        card.setCardNumber(data.getResCardNo());
        card.setCardType(data.getResCardType());
        card.setUserName(data.getResUserNm());
        card.setSleepYn(data.getResSleepYN());
        card.setTrafficYn(data.getResTrafficYN());
        card.setValidPeriod(parseDate(data.getResValidPeriod()));
        card.setIssueDate(parseDate(data.getResIssueDate()));
        card.setImageLink(data.getResImageLink());
        card.setCardState(data.getResState());
        return card;
    }

    private Date parseDate(String yyyymmdd) {
        try {
            if (yyyymmdd == null || yyyymmdd.isBlank()) return null;
            return new SimpleDateFormat("yyyyMMdd").parse(yyyymmdd);
        } catch (ParseException e) {
            return null;
        }
    }
}
