package com.wonnabe.community.mapper;

import com.wonnabe.community.dto.comment.CommentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    boolean existsBoardInCommunity(@Param("communityId") int communityId, @Param("boardId") Long boardId);

    //댓글 조회
    List<CommentDTO> selectCommentsByBoardId(@Param("communityId") int communityId,
                                             @Param("boardId") Long boardId);

    //댓글 생성
    void insertComment(@Param("userId") String userId,
                       @Param("postId") int postId,
                       @Param("parentCommentId") Long parentCommentId,
                       @Param("content") String content);

    //존재 여부 확인
    int existsComment(@Param("commentId") Long commentId,
                      @Param("boardId") Long boardId,
                      @Param("communityId") int communityId,
                      @Param("userId") String userId);

    //댓글 삭제
    int markCommentAsDeleted(@Param("commentId") Long commentId,
                             @Param("boardId") Long boardId,
                             @Param("userId") String userId);

    // 댓글 + 게시판 + 커뮤니티 일치하는 유효 댓글 조회
    CommentDTO selectCommentByIdAndBoardAndCommunity(@Param("commentId") Long commentId,
                                                     @Param("boardId") Long boardId,
                                                     @Param("communityId") int communityId);

    // 댓글 좋아요 상태 조회 (is_deleted 값 확인용)
    Integer getCommentLikeStatus(@Param("userId") String userId,
                                 @Param("boardId") Long boardId,
                                 @Param("commentId") Long commentId,
                                 @Param("communityId") int communityId);

    // 댓글 좋아요 삽입
    void insertCommentLike(@Param("userId") String userId,
                           @Param("boardId") Long boardId,
                           @Param("commentId") Long commentId,
                           @Param("communityId") int communityId);

    // 댓글 좋아요 상태 수정 (is_deleted)
    void updateCommentLikeStatus(@Param("userId") String userId,
                                 @Param("boardId") Long boardId,
                                 @Param("commentId") Long commentId,
                                 @Param("communityId") int communityId,
                                 @Param("isDeleted") int isDeleted);
}
