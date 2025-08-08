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
    public List<CommentDTO> getCommentsByBoardId(int communityId, Long boardId, String userId) {
        boolean exists = commentMapper.existsBoardInCommunity(communityId, boardId);
        if (!exists) {
            throw new NoSuchElementException("조회할 댓글을 확인해주세요.");
        }

        return commentMapper.selectCommentsByBoardId(communityId, boardId, userId);
    }



    //댓글 생성
    public void createComment(String userId, int postId, Long boardId, String content) {
        commentMapper.insertComment(userId, postId, boardId, content);
    }

    //댓글 삭제
    public void deleteComment(Long commentId, Long boardId, String userId, int communityId) {
        int exists = commentMapper.existsComment(commentId, boardId, communityId, userId);
        if (exists == 0) {
            throw new NoSuchElementException("삭제할 댓글이 존재하지 않음");
        }
        commentMapper.markCommentAsDeleted(commentId, boardId, userId);
    }


    //좋아요 생성 -댓글
    // 댓글 좋아요 토글
    public CommentDTO toggleCommentLike(String userId, Long commentId, Long boardId, int communityId) {
        // 댓글 존재 여부 확인
        CommentDTO comment = commentMapper.selectCommentByIdAndBoardAndCommunity(commentId, boardId, communityId);
        if (comment == null) {
            throw new IllegalArgumentException("좋아요 대상 댓글이 존재하지 않습니다.");
        }

        // 좋아요 상태 조회
        Integer status = commentMapper.getCommentLikeStatus(userId, boardId, commentId, communityId);
        if (status == null) {
            commentMapper.insertCommentLike(userId, boardId, commentId, communityId);
        } else {
            int newStatus = (status == 0) ? 1 : 0;
            commentMapper.updateCommentLikeStatus(userId, boardId, commentId, communityId, newStatus);
        }

        // 최신 댓글 정보 반환
        return commentMapper.selectCommentByIdAndBoardAndCommunity(commentId, boardId, communityId);
    }

}
