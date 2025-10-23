package com.example.board.service;

import com.example.board.entity.Board;
import com.example.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public List<Board> findAll(){
        return boardRepository.findAll();
    }

    @Transactional
    public Board findById(Long id){
        Board board = boardRepository.findById(id).orElseThrow();
        board.setViewCount(board.getViewCount()+1);
        return board;
    }

    public Board save(Board board){
        return boardRepository.save(board);
    }

    @Transactional
    public Board update(Long id, Board board){
        Board updateBoard = boardRepository.findById(id).orElseThrow();
        updateBoard.setTitle(board.getTitle());
        updateBoard.setContent(board.getContent());
        return updateBoard;
    }

    public void delete(Long id){
        boardRepository.deleteById(id);
    }
}
