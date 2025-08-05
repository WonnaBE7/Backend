package com.wonnabe.community.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.util.JsonResponse;
import com.wonnabe.community.dto.comment.CommentCreateRequestDto;
import com.wonnabe.community.dto.comment.CommentDTO;
import com.wonnabe.community.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
}
