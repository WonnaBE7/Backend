package com.wonnabe.community.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.community.dto.board.BoardDTO;
import com.wonnabe.community.dto.community.CommunityDTO;
import com.wonnabe.community.service.CommunityService;
import com.wonnabe.common.util.JsonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    // 전체 커뮤니티 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<Object> getCommunityList() {
        List<CommunityDTO> communityList = communityService.getCommunityList();
        return JsonResponse.ok("게시판 조회에 성공", Map.of("communities", communityList));
    }

    // 상위 3개 커뮤니티 조회
    @GetMapping("/list/top3")
    public ResponseEntity<Object> getTop3Communities() {
        return JsonResponse.ok("TOP 3 커뮤니티 조회 성공", communityService.getTop3CommunityList());
    }

    // 사용자의 상위 3개 게시글 조회
    @GetMapping("/board/top3")
    public ResponseEntity<Object> getTop3Boards(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        List<BoardDTO> boardList = communityService.getTop3BoardList(userId);

        return JsonResponse.ok("게시글을 불러오는 것에 성공하였습니다.", Map.of("boards", boardList));
    }

    // 내가 쓴 글 수 / 스크랩한 글 수 조회
    @GetMapping("/number")
    public ResponseEntity<Object> getBoardCounts(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        Map<String, Integer> result = communityService.getUserBoardAndScrapCounts(userId);
        return JsonResponse.ok("좋아요 스크랩 조회 성공", result);
    }

    //사용자가 스크랩한 게시글을 조회
    @GetMapping("/user/scraped")
    public ResponseEntity<Object> getUserScrapedBoards(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        List<BoardDTO> scrapedBoards = communityService.getScrapedBoards(userId);
        return JsonResponse.ok("스크랩된 게시글을 조회에 성공했습니다.", Map.of("boards", scrapedBoards));
    }

    //사용자가 작성한 글 목록 조회
    @GetMapping("/user/writed")
    public ResponseEntity<Object> getUserWrittenBoards(@AuthenticationPrincipal CustomUser customUser) {
        String userId = customUser.getUser().getUserId();
        List<BoardDTO> writtenBoards = communityService.getWrittenBoards(userId);
        return JsonResponse.ok("작성한 게시글 조회에 성공했습니다.", Map.of("boards", writtenBoards));
    }

}
