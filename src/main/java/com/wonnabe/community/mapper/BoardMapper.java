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


    //게시글 삭제
    void markBoardAsDeleted(@Param("boardId") Long boardId,
                            @Param("communityId") int communityId,
                            @Param("userId") String userId);


    //게시글 조회
    BoardDTO selectBoardDetail(@Param("communityId") int communityId,
                               @Param("boardId") Long boardId,
                               @Param("userId") String userId);



    //게시글 스크랩
    Integer getScrapStatus(@Param("userId") String userId,
                           @Param("boardId") Long boardId,
                           @Param("communityId") int communityId);

    void insertBoardScrap(@Param("userId") String userId,
                          @Param("boardId") Long boardId,
                          @Param("communityId") int communityId);

    void updateScrapStatus(@Param("userId") String userId,
                           @Param("boardId") Long boardId,
                           @Param("communityId") int communityId,
                           @Param("isDeleted") int isDeleted);


    //좋아요 생성 - 게시글
    Integer getBoardLikeStatus(@Param("userId") String userId,
                               @Param("boardId") Long boardId,
                               @Param("communityId") int communityId);

    void insertBoardLike(@Param("userId") String userId,
                         @Param("boardId") Long boardId,
                         @Param("communityId") int communityId);

    void updateBoardLikeStatus(@Param("userId") String userId,
                               @Param("boardId") Long boardId,
                               @Param("communityId") int communityId,
                               @Param("isDeleted") int isDeleted);

}
