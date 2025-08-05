package com.wonnabe.community.dto.comment;

public class CommentDTO {
    private Long commentId;
    private String userName;
    private String nowme;
    private String content;
    private int likeCount;

    public CommentDTO() {}

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getNowme() { return nowme; }
    public void setNowme(String nowme) { this.nowme = nowme; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
}
