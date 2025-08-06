package com.wonnabe.community.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.community.dto.comment.CommentCreateRequestDto;
import com.wonnabe.community.dto.comment.CommentDTO;
import com.wonnabe.community.service.CommentService;
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
public class CommentController {

    private final CommentService commentService;

    // 댓글 조회
    @GetMapping("/board/comment")
    public ResponseEntity<Object> getComments(@RequestParam int communityId,
                                              @RequestParam Long boardId) {
        List<CommentDTO> comments = commentService.getCommentsByBoardId(communityId, boardId);
        return JsonResponse.ok("댓글 조회에 성공하였습니다.", Map.of("comments", comments));
    }


    //댓글 생성
    @PostMapping("/board/comment")
    public ResponseEntity<Object> createComment(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam int communityId, // post_id
            @RequestParam Long boardId,    // board_id → parent_comment_id에 저장할 값
            @RequestBody CommentCreateRequestDto requestDto
    ) {
        String userId = customUser.getUser().getUserId();
        commentService.createComment(userId, communityId, boardId, requestDto.getContent());
        return JsonResponse.ok("댓글 작성에 성공하였습니다.");
    }

    //댓글 삭제
    @PatchMapping("/board/comment/delete")
    public ResponseEntity<Object> deleteComment(
            @RequestParam("communityId") int communityId,
            @RequestParam("boardId") Long boardId,
            @RequestParam("commentId") Long commentId,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        String userId = customUser.getUser().getUserId();
        commentService.deleteComment(commentId, boardId, userId);
        return JsonResponse.ok("댓글 삭제에 성공하였습니다.", Map.of());
    }

    //좋아요 생성 -댓글
    @PatchMapping("/board/comment/like")
    public ResponseEntity<Object> toggleCommentLike(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam("communityId") int communityId,
            @RequestParam("boardId") Long boardId,
            @RequestParam("commentId") Long commentId) {

        try {
            String userId = customUser.getUser().getUserId();
            commentService.toggleCommentLike(userId, commentId, boardId, communityId);
            return JsonResponse.ok("댓글 좋아요 상태가 변경되었습니다.");
        } catch (NoSuchElementException e) {
            // 메시지로 분기
            if ("댓글 없음".equals(e.getMessage())) {
                return JsonResponse.error(HttpStatus.NOT_FOUND, "좋아요 대상 댓글이 존재하지 않습니다.");
            }
            return JsonResponse.error(HttpStatus.NOT_FOUND, "대상 데이터가 존재하지 않습니다.");
        } catch (Exception e) {
            return JsonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 좋아요 처리 중 오류가 발생했습니다.");
        }
    }



}
