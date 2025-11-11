package com.example.board.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "board_file")
@Getter @Setter
@NoArgsConstructor
public class BoardFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="board_id")
	private Board board;
	
	@Column(nullable = false, length = 200)
	private String originalFileName;
	
	@Column(nullable = false, length = 200)
	private String savedFileName;

	@Column(nullable = false, length = 500)
	private String filePath;
	
	@Column(nullable = false)
	private Long fileSize;
	
	@Column(nullable = false)
	private LocalDateTime uploadDate;
	
	@PrePersist
	public void prePersist() {
		this.uploadDate = LocalDateTime.now();
	}
}
