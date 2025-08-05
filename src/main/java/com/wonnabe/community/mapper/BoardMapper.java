package com.wonnabe.community.mapper;

import com.wonnabe.community.dto.board.BoardDTO;
import com.wonnabe.community.dto.comment.CommentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {
    List<BoardDTO> selectBoardsByCommunityId(@Param("communityId") int communityId,
                                             @Param("userId") String userId,
                                             @Param("pageSize") int pageSize,
                                             @Param("lastBoardId") Long lastBoardId);

    //게시글 생성
    void insertBoard(@Param("communityId") int communityId,
                     @Param("userId") String userId,
                     @Param("title") String title,
                     @Param("content") String content);


    //게시글 조회
    BoardDTO selectBoardDetail(@Param("communityId") int communityId,
                               @Param("boardId") Long boardId,
                               @Param("userId") String userId);

    //댓글 조회
    List<CommentDTO> selectCommentsByBoardId(@Param("communityId") int communityId,
                                             @Param("boardId") Long boardId);


    // 댓글 생성
    void insertComment(@Param("userId") String userId,
                       @Param("boardId") Long boardId,
                       @Param("content") String content);
}
