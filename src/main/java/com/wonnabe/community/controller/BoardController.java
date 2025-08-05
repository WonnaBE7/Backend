package com.wonnabe.community.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.community.dto.board.BoardCreateRequestDto;
import com.wonnabe.community.dto.board.BoardDTO;
import com.wonnabe.community.dto.board.BoardPageRequestDto;
import com.wonnabe.community.dto.comment.CommentCreateRequestDto;
import com.wonnabe.community.dto.comment.CommentDTO;
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

    //게시판 전체 글 조회 - 페이지네이션
    @PostMapping("/board")
    public ResponseEntity<Object> getBoardsByCommunity(
            @RequestParam int communityId,
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

    //게시글 생성
    @PostMapping("/board/create")
    public ResponseEntity<Object> createBoard(
            @RequestParam int communityId,
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody BoardCreateRequestDto requestDto
    ) {
        String userId = customUser.getUser().getUserId();
        boardService.createBoard(communityId, userId, requestDto);
        return JsonResponse.ok("게시글을 생성하였습니다.");
    }

    //게시글 조회
    @GetMapping("/board")
    public ResponseEntity<Object> getBoardDetail(
            @RequestParam int communityId,
            @RequestParam Long boardId,
            @AuthenticationPrincipal CustomUser customUser) {

        String userId = customUser.getUser().getUserId();
        BoardDTO board = boardService.getBoardDetail(communityId, boardId, userId);
        return JsonResponse.ok("게시글 조회에 성공하였습니다.", Map.of("data", board));
    }

}