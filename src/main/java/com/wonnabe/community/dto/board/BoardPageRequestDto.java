package com.wonnabe.community.dto.board;

public class BoardPageRequestDto {
    private int pageSize;
    private Long lastBoardId;

    private String title;
    private String content;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Long getLastBoardId() {
        return lastBoardId;
    }

    public void setLastBoardId(Long lastBoardId) {
        this.lastBoardId = lastBoardId;
    }


}
