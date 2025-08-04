package com.wonnabe.community.mapper;

import com.wonnabe.community.dto.board.BoardDTO;
import com.wonnabe.community.dto.community.CommunityDTO;
import com.wonnabe.community.dto.community.FavoriteProductDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityMapper {

    //모든 커뮤니티(성향) 리스트 조회
    List<CommunityDTO> selectAllCommunities();

    //회원 수 기준 상위 3개 커뮤니티 조회
    List<CommunityDTO> selectTop3Communities();

    // CommunityDTO selectCommunityById(@Param("communityId") int communityId);

    //좋아요 수 기준 상위 3개 게시글 조회
    List<BoardDTO> selectTop3Boards(@Param("userId") String userId);

    //사용자가 작성한 게시글 수 반환
    int countUserBoards(@Param("userId") String userId);
    //사용자가 스크랩한 게시글 수 반환
    int countUserScraps(@Param("userId") String userId);

    //사용자가 스크랩한 게시글을 조회
    List<BoardDTO> selectScrapedBoards(@Param("userId") String userId);

    //사용자가 작성한 글 목록 조회
    List<BoardDTO> selectWrittenBoards(@Param("userId") String userId);

}
