package com.wonnabe.community.service;

import com.wonnabe.community.dto.board.BoardCreateRequestDto;
import com.wonnabe.community.dto.board.BoardDTO;
import com.wonnabe.community.dto.comment.CommentDTO;
import com.wonnabe.community.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    /**
     * 커서 기반 페이징으로 커뮤니티 내 게시글을 조회한다.
     *
     * @param communityId   조회할 커뮤니티 ID
     * @param userId        현재 로그인한 사용자 ID
     * @param pageSize      가져올 게시글 개수 (limit)
     * @param lastBoardId   이전 요청에서 마지막으로 가져온 게시글 ID (커서)
     * @return              게시글 리스트
     */
    public List<BoardDTO> getBoardsByCommunityId(int communityId, String userId, int pageSize, Long lastBoardId) {
        return boardMapper.selectBoardsByCommunityId(communityId, userId, pageSize, lastBoardId);
    }

    //게시글 생성
    public void createBoard(int communityId, String userId, BoardCreateRequestDto requestDto) {
        boardMapper.insertBoard(communityId, userId, requestDto.getTitle(), requestDto.getContent());
    }

    //게시글 삭제
    public void deleteBoard(Long boardId, int communityId, String userId) {
        boardMapper.markBoardAsDeleted(boardId, communityId, userId);
    }

    //게시글 조회
    public BoardDTO getBoardDetail(int communityId, Long boardId, String userId) {
        return boardMapper.selectBoardDetail(communityId, boardId, userId);
    }

    //게시글 스크랩
    public void toggleBoardScrap(String userId, Long boardId, int communityId) {
        Integer currentStatus = boardMapper.getScrapStatus(userId, boardId, communityId);

        if (currentStatus == null) {
            boardMapper.insertBoardScrap(userId, boardId, communityId); // 새로 추가
        } else {
            int newStatus = (currentStatus == 0) ? 1 : 0;
            boardMapper.updateScrapStatus(userId, boardId, communityId, newStatus); // 상태 토글
        }
    }

    //좋아요 생성 - 게시글
    public void toggleBoardLike(String userId, Long boardId, int communityId) {
        Integer status = boardMapper.getBoardLikeStatus(userId, boardId, communityId);

        if (status == null) {
            boardMapper.insertBoardLike(userId, boardId, communityId);
        } else {
            int newStatus = (status == 0) ? 1 : 0;
            boardMapper.updateBoardLikeStatus(userId, boardId, communityId, newStatus);
        }
    }





}