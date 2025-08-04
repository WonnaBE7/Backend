package com.wonnabe.community.mapper;

import com.wonnabe.community.dto.board.BoardDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {
    List<BoardDTO> selectBoardsByCommunityId(@Param("communityId") int communityId,
                                             @Param("userId") String userId,
                                             @Param("pageSize") int pageSize,
                                             @Param("lastBoardId") Long lastBoardId);

    void insertBoard(@Param("communityId") int communityId,
                     @Param("userId") String userId,
                     @Param("title") String title,
                     @Param("content") String content);

}
