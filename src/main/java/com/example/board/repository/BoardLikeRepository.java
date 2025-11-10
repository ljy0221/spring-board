package com.example.board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.board.entity.Board;
import com.example.board.entity.BoardLike;
import com.example.board.entity.User;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
	
	// 좋아요 확인
	public boolean existsByUserAndBoard(User user, Board board);
	
	// 좋아요 찾기
	public Optional<BoardLike> findByUserAndBoard(User user, Board board);
	
	// 좋아요 개수
	public long countByBoard(Board board);
	
	// 좋아요 제거
	public void deleteByBoard(Board board);
}
