package com.wonnabe.community.mapper;

import com.wonnabe.community.dto.comment.CommentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    //댓글 조회
    List<CommentDTO> selectCommentsByBoardId(@Param("communityId") int communityId,
                                             @Param("boardId") Long boardId);

    //댓글 생성
    void insertComment(@Param("userId") String userId,
                       @Param("postId") int postId,
                       @Param("parentCommentId") Long parentCommentId,
                       @Param("content") String content);

    //댓글 삭제
    void markCommentAsDeleted(@Param("commentId") Long commentId,
                              @Param("boardId") Long boardId,
                              @Param("userId") String userId);

    //좋아요 생성 -댓글
    Integer getCommentLikeStatus(@Param("userId") String userId,
                                 @Param("boardId") Long boardId,
                                 @Param("commentId") Long commentId,
                                 @Param("communityId") int communityId);

    void insertCommentLike(@Param("userId") String userId,
                           @Param("boardId") Long boardId,
                           @Param("commentId") Long commentId,
                           @Param("communityId") int communityId);

    void updateCommentLikeStatus(@Param("userId") String userId,
                                 @Param("boardId") Long boardId,
                                 @Param("commentId") Long commentId,
                                 @Param("communityId") int communityId,
                                 @Param("isDeleted") int isDeleted);

}
