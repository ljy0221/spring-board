package com.example.board.service;

import com.example.board.entity.Board;
import com.example.board.entity.BoardFile;
import com.example.board.repository.BoardFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final BoardFileRepository boardFileRepository;
    
    // 파일 저장 경로
    private final String uploadPath = "C:/upload/";  // Windows
    // private final String uploadPath = "/upload/";  // Linux/Mac
    
    // 파일 저장
    public List<BoardFile> saveFiles(List<MultipartFile> files, Board board) throws IOException {
        List<BoardFile> savedFiles = new ArrayList<>();
        
        // 업로드 디렉토리가 없으면 생성
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            
            // 원본 파일명
            String originalFileName = file.getOriginalFilename();
            
            // 저장될 파일명 (UUID + 원본파일명)
            String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
            
            // 파일 저장 경로
            String filePath = uploadPath + savedFileName;
            
            // 파일 저장
            File dest = new File(filePath);
            file.transferTo(dest);
            
            // BoardFile 엔티티 생성
            BoardFile boardFile = new BoardFile();
            boardFile.setBoard(board);
            boardFile.setOriginalFileName(originalFileName);
            boardFile.setSavedFileName(savedFileName);
            boardFile.setFilePath(filePath);
            boardFile.setFileSize(file.getSize());
            
            savedFiles.add(boardFileRepository.save(boardFile));
        }
        
        return savedFiles;
    }
    
    // 게시글의 파일 목록 조회
    public List<BoardFile> getFilesByBoard(Board board) {
        return boardFileRepository.findByBoard(board);
    }
    
    // 파일 삭제
    public void deleteFile(Long fileId) throws IOException {
        BoardFile boardFile = boardFileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));
        
        // 실제 파일 삭제
        File file = new File(boardFile.getFilePath());
        if (file.exists()) {
            file.delete();
        }
        
        // DB에서 삭제
        boardFileRepository.delete(boardFile);
    }
}