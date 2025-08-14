package com.wonnabe.codef.dto.card.account;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wonnabe.codef.domain.UserCard;
import lombok.Getter;
import lombok.Setter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardListResponse {

    @JsonProperty("data")
    private List<CardItem> data;

    private String userId;
    private String connectedId;
    private Map<String, Object> result;
    private String endpoint;

    public List<UserCard> toUserCards(String userId) {
        List<UserCard> cards = new ArrayList<>();
        for (CardItem cardDto : data) {
            UserCard card = new UserCard();
            card.setUserId(userId);
            card.setCardName(cardDto.getResCardName());
            card.setCardNumber(cardDto.getResCardNo());
            card.setCardType(cardDto.getResCardType());
            card.setUserName(cardDto.getResUserNm());
            card.setSleepYn(cardDto.getResSleepYN());
            card.setTrafficYn(cardDto.getResTrafficYN());
            card.setValidPeriod(parseDate(cardDto.getResValidPeriod()));
            card.setIssueDate(parseDate(cardDto.getResIssueDate()));
            card.setImageLink(cardDto.getResImageLink());
            card.setCardState(cardDto.getResState());
            cards.add(card);
        }
        return cards;
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
