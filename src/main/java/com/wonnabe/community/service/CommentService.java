package com.wonnabe.community.service;

import com.wonnabe.community.dto.comment.CommentDTO;
import com.wonnabe.community.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;

    //댓글 조회
    public List<CommentDTO> getCommentsByBoardId(int communityId, Long boardId) {
        return commentMapper.selectCommentsByBoardId(communityId, boardId);
    }

    //댓글 생성
    public void createComment(String userId, int postId, Long boardId, String content) {
        commentMapper.insertComment(userId, postId, boardId, content);
    }

    //댓글 삭제
    public void deleteComment(Long commentId, Long boardId, String userId) {
        commentMapper.markCommentAsDeleted(commentId, boardId, userId);
    }

    //좋아요 생성 -댓글
    public void toggleCommentLike(String userId, Long boardId, Long commentId, int communityId) {
        Integer status = commentMapper.getCommentLikeStatus(userId, boardId, commentId, communityId);

        if (status == null) {
            commentMapper.insertCommentLike(userId, boardId, commentId, communityId);
        } else {
            int newStatus = (status == 0) ? 1 : 0;
            commentMapper.updateCommentLikeStatus(userId, boardId, commentId, communityId, newStatus);
        }
    }

}
