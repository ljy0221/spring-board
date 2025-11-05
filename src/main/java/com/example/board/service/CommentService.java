package com.example.board.service;

import com.example.board.entity.Board;
import com.example.board.entity.Comment;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    
    // 댓글 목록 조회
    public List<Comment> findByBoardId(Long boardId) {
        return commentRepository.findByBoardIdOrderByCreatedDateAsc(boardId);
    }
    
    // 댓글 저장
    public Comment save(Long boardId, String writer, String content) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        
        Comment comment = new Comment();
        comment.setWriter(writer);
        comment.setContent(content);
        comment.setBoard(board);
        
        return commentRepository.save(comment);
    }
    
    // 댓글 삭제
    public void delete(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}