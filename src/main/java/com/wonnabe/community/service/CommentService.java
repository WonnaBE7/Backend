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
}
