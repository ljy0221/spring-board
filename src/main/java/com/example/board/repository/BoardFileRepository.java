package com.example.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.board.entity.Board;
import com.example.board.entity.BoardFile;

public interface BoardFileRepository extends JpaRepository<BoardFile, Long>{
	
	List<BoardFile> findByBoard(Board board);
	
	void deleteByBoard(Board board);
}
