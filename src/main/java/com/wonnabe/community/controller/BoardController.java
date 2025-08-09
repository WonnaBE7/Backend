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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class BoardController {

    private final BoardService boardService;

    // 게시판 전체 글 조회 - 페이지네이션
    @PostMapping("/board")
    public ResponseEntity<Object> getBoardsByCommunity(
            @RequestParam int communityId,
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody BoardPageRequestDto pageRequest) {

        try {
            String userId = customUser.getUser().getUserId();
            List<BoardDTO> boards = boardService.getBoardsByCommunityId(
                    communityId, userId,
                    pageRequest.getPageSize(),
                    pageRequest.getLastBoardId()
            );
            return JsonResponse.ok("게시글을 성공적으로 가져왔습니다.", Map.of("boards", boards));
        } catch (Exception e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 조회 중 오류가 발생했습니다.");
        }
    }

    // 게시글 생성
    @PostMapping("/board/create")
    public ResponseEntity<Object> createBoard(
            @RequestParam int communityId,
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody BoardCreateRequestDto requestDto) {

        try {
            String userId = customUser.getUser().getUserId();
            boardService.createBoard(communityId, userId, requestDto);
            return JsonResponse.ok("게시글을 생성하였습니다.");
        } catch (Exception e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "communityId를 확인해주세요.");
        }
    }

    // 게시글 삭제
    @PatchMapping("/board/delete")
    public ResponseEntity<Object> deleteBoard(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam("communityId") int communityId,
            @RequestParam("boardId") Long boardId) {

        try {
            String userId = customUser.getUser().getUserId();
            boardService.deleteBoard(boardId, communityId, userId);
            return JsonResponse.ok("게시글이 삭제되었습니다.");
        } catch (NoSuchElementException e) {
            return JsonResponse.error(HttpStatus.NOT_FOUND, "삭제할 게시글을 다시 확인해주세요.");
        } catch (Exception e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 삭제에 실패하였습니다.");
        }
    }



    // 게시글 정보 조회
    @GetMapping("/board")
    public ResponseEntity<Object> getBoardDetail(
            @RequestParam int communityId,
            @RequestParam Long boardId,
            @AuthenticationPrincipal CustomUser customUser) {

        try {
            String userId = customUser.getUser().getUserId();
            BoardDTO board = boardService.getBoardDetail(communityId, boardId, userId);
            return JsonResponse.ok("게시글 조회에 성공하였습니다.", Map.of("data", board));
        } catch (NoSuchElementException e) {
            return JsonResponse.error(HttpStatus.NOT_FOUND, "해당 게시글이 존재하지 않습니다.");
        } catch (Exception e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 조회에 실패하였습니다.");
        }
    }

    // 게시글 스크랩
    @PatchMapping("/board/scrap")
    public ResponseEntity<Object> toggleBoardScrap(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam("communityId") int communityId,
            @RequestParam("boardId") Long boardId) {

        try {
            String userId = customUser.getUser().getUserId();
            boardService.toggleBoardScrap(userId, boardId, communityId);
            return JsonResponse.ok("스크랩 상태가 변경되었습니다.");
        } catch (NoSuchElementException e) {
            return JsonResponse.error(HttpStatus.NOT_FOUND, "스크랩 대상 게시글이 존재하지 않습니다.");
        } catch (Exception e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "게시물 스크랩에 실패하였습니다.");
        }
    }

    // 좋아요 생성 - 게시글
    @PatchMapping("/board/like")
    public ResponseEntity<Object> toggleBoardLike(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam("communityId") int communityId,
            @RequestParam("boardId") Long boardId) {

        try {
            String userId = customUser.getUser().getUserId();
            boardService.toggleBoardLike(userId, boardId, communityId);
            return JsonResponse.ok("게시글 좋아요 상태가 변경되었습니다.");
        } catch (NoSuchElementException e) {
            return JsonResponse.error(HttpStatus.NOT_FOUND, "좋아요 대상 게시글이 존재하지 않습니다.");
        } catch (Exception e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "좋아요 처리 중 오류가 발생했습니다.");
        }
    }
}
