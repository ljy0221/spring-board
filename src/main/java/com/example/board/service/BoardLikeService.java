package com.example.board.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.board.entity.Board;
import com.example.board.entity.BoardLike;
import com.example.board.entity.User;
import com.example.board.repository.BoardLikeRepository;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardLikeService {
	private final BoardLikeRepository boardLikeRepository;
	private final BoardRepository boardRepository;
	private final UserRepository userRepository;
	
	public boolean toggleLike(Long boardId, Long userId) {
		Board board = boardRepository.findById(boardId).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
		
		Optional<BoardLike> existingLike = boardLikeRepository.findByUserAndBoard(user, board);
		
		if(existingLike.isPresent()) {
			boardLikeRepository.delete(existingLike.get());
			return false;
		} else {
			BoardLike boardLike = new BoardLike();
			boardLike.setUser(user);
			boardLike.setBoard(board);
			boardLikeRepository.save(boardLike);
			return true;
		}
	}
	
	public long countLikes(Long boardId) {
		Board board = boardRepository.findById(boardId).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
		
		return boardLikeRepository.countByBoard(board);
	}
	
	public boolean isLiked(Long boardId, Long userId) {
		Board board = boardRepository.findById(boardId).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
		
		return boardLikeRepository.existsByUserAndBoard(user, board);
	}
}
