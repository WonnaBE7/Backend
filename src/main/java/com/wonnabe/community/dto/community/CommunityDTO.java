package com.wonnabe.community.dto.community;

public class CommunityDTO {
    private int communityId;
    private String communityName;
    private String simpleDescription;
    private int memberCount;
    private String latestBoard;

    public CommunityDTO() {}

    public int getCommunityId() {
        return communityId;
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getSimpleDescription() {
        return simpleDescription;
    }

    public void setSimpleDescription(String simpleDescription) {
        this.simpleDescription = simpleDescription;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getLatestBoard() {
        return latestBoard;
    }

    public void setLatestBoard(String latestBoard) {
        this.latestBoard = latestBoard;
    }
}
