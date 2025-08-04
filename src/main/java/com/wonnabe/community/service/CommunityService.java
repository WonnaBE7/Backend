package com.wonnabe.community.service;

import com.wonnabe.community.dto.board.BoardDTO;
import com.wonnabe.community.dto.community.CommunityDTO;
import com.wonnabe.community.dto.community.FavoriteProductDTO;
import com.wonnabe.community.mapper.CommunityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityMapper communityMapper;

    // 전체 커뮤니티 리스트 조회
    public List<CommunityDTO> getCommunityList() {
        return communityMapper.selectAllCommunities();
    }

    // 상위 3개 커뮤니티 조회
    public List<CommunityDTO> getTop3CommunityList() {
        return communityMapper.selectTop3Communities();
    }

    // 필요 시 확장할 예정
    // public CommunityDTO getCommunityById(int communityId) {
    //     return communityMapper.selectCommunityById(communityId);
    // }

    // 사용자의 상위 3개 게시글 조회
    public List<BoardDTO> getTop3BoardList(String userId) {
        return communityMapper.selectTop3Boards(userId);
    }

    // 내가 쓴 글 수 / 스크랩한 글 수 조회
    public Map<String, Integer> getUserBoardAndScrapCounts(String userId) {
        int writeCount = communityMapper.countUserBoards(userId);
        int scrapCount = communityMapper.countUserScraps(userId);

        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("write", writeCount);
        result.put("scrap", scrapCount);

        return result;
    }

    //사용자가 스크랩한 게시글을 조회
    public List<BoardDTO> getScrapedBoards(String userId) {
        return communityMapper.selectScrapedBoards(userId);
    }

    //사용자가 작성한 글 목록 조회
    public List<BoardDTO> getWrittenBoards(String userId) {
        return communityMapper.selectWrittenBoards(userId);
    }

}
