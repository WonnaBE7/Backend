package com.wonnabe.community.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.community.dto.board.BoardCreateRequestDto;
import com.wonnabe.community.dto.board.BoardDTO;
import com.wonnabe.community.dto.board.BoardPageRequestDto;
import com.wonnabe.community.service.BoardService;
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
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/{communityId}/board")
    public ResponseEntity<Object> getBoardsByCommunity(
            @PathVariable int communityId,
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody BoardPageRequestDto pageRequest) {

        String userId = customUser.getUser().getUserId();
        List<BoardDTO> boards = boardService.getBoardsByCommunityId(
                communityId,
                userId,
                pageRequest.getPageSize(),
                pageRequest.getLastBoardId()
        );

        return JsonResponse.ok("게시글을 성공적으로 가져왔습니다.", Map.of("boards", boards));
    }

    @PostMapping("/{communityId}/board/create")
    public ResponseEntity<Object> createBoard(
            @PathVariable int communityId,
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody BoardCreateRequestDto requestDto
    ) {
        String userId = customUser.getUser().getUserId();
        boardService.createBoard(communityId, userId, requestDto);
        return JsonResponse.ok("게시글을 생성하였습니다.");
    }

}