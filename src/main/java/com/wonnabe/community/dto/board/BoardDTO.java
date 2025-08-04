package com.wonnabe.community.dto.board;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "boardId", "title", "content", "userName",
        "categoryId", "categoryName", "likeCount", "commentCount",
        "isScraped", "isLiked", "createdAt"
})
public class BoardDTO {

    private Long boardId;
    private String title;
    private String content;
    private String userName;

    @JsonProperty("categoryId")
    private Integer categoryId;

    private String categoryName;
    private int likeCount;
    private int commentCount;

    @JsonProperty("isScraped")
    private boolean scraped;

    @JsonProperty("isLiked")
    private boolean liked;

    private String createdAt;

    // 기본 생성자
    public BoardDTO() {}

    // Getter/Setter
    public Long getBoardId() {
        return boardId;
    }
    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getLikeCount() {
        return likeCount;
    }
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isScraped() {
        return scraped;
    }
    public void setScraped(boolean scraped) {
        this.scraped = scraped;
    }

    public boolean isLiked() {
        return liked;
    }
    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
