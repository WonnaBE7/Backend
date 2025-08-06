package com.wonnabe.community.service;

import com.wonnabe.community.dto.comment.CommentDTO;
import com.wonnabe.community.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

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
    public void toggleCommentLike(String userId, Long commentId, Long boardId, int communityId) {
        CommentDTO comment = commentMapper.selectCommentByIdAndBoardAndCommunity(commentId, boardId, communityId);
        if (comment == null) {
            // 반드시 이 메시지 그대로
            throw new NoSuchElementException("댓글 없음");
        }

        Integer status = commentMapper.getCommentLikeStatus(userId, commentId, boardId, communityId);
        if (status == null) {
            commentMapper.insertCommentLike(userId, commentId, boardId, communityId);
        } else {
            int newStatus = (status == 0) ? 1 : 0;
            commentMapper.updateCommentLikeStatus(userId, commentId, boardId, communityId, newStatus);
        }
    }



}
