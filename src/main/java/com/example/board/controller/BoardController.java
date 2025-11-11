package com.example.board.controller;

import com.example.board.config.AppConfig;
import com.example.board.entity.Board;
import com.example.board.entity.BoardFile;
import com.example.board.entity.Comment;
import com.example.board.entity.User;
import com.example.board.service.BoardLikeService;
import com.example.board.service.BoardService;
import com.example.board.service.CommentService;
import com.example.board.service.FileService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class BoardController {

	private final AppConfig appConfig;
	private final BoardService boardService;
	private final CommentService commentService;
	private final BoardLikeService boardLikeService;
	private final FileService fileService;

	@GetMapping("/board/list")
	public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
			@RequestParam(required = false) String searchType, @RequestParam(required = false) String keyword,
			Model model) {
		Page<Board> boards;

		if (keyword != null && !keyword.isEmpty()) {
			boards = boardService.search(searchType, keyword, pageable);
		} else {
			boards = boardService.findAll(pageable);
		}

		model.addAttribute("boards", boards);
		model.addAttribute("searchType", searchType);
		model.addAttribute("keyword", keyword);

		return "board/list";
	}

	@GetMapping("/board/detail/{id}")
	public String detail(@PathVariable Long id, Model model, HttpSession session) {
		Board board = boardService.findById(id);
		List<Comment> comments = commentService.findByBoardId(id);
		
		long likeCount = boardLikeService.countLikes(id);
		
		boolean isLiked = false;
		User loginUser = (User)session.getAttribute("loginUser");
		if(loginUser != null) {
			isLiked = boardLikeService.isLiked(id, loginUser.getId());
		}
		
		List<BoardFile> files = fileService.getFilesByBoard(board);
		
		model.addAttribute("board", board);
		model.addAttribute("comments", comments);
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("isLiked", isLiked);
		model.addAttribute("files", files);
		
		return "board/detail";
	}

	@GetMapping("/board/write")
	public String writeForm(HttpSession session) {
	    User loginUser = (User) session.getAttribute("loginUser");
	    
	    if (loginUser == null) {
	        return "redirect:/user/login";
	    }
	    
	    return "board/write";
	}

	@PostMapping("/board/write")
	public String write(
	        Board board, 
	        @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> files,
	        HttpSession session) {
	    
	    User loginUser = (User) session.getAttribute("loginUser");
	    
	    if (loginUser == null) {
	        return "redirect:/user/login";
	    }
	    
	    board.setWriter(loginUser.getName());
	    Board savedBoard = boardService.save(board);
	    
	    // 파일 업로드 처리 (개선)
	    if (files != null && !files.isEmpty()) {
	        // 빈 파일 제외
	        List<MultipartFile> validFiles = files.stream()
	            .filter(file -> !file.isEmpty())
	            .collect(Collectors.toList());
	        
	        if (!validFiles.isEmpty()) {
	            try {
	                fileService.saveFiles(validFiles, savedBoard);
	            } catch (IOException e) {
	                // 파일 업로드 실패해도 게시글은 등록됨
	                e.printStackTrace();
	            }
	        }
	    }
	    
	    return "redirect:/board/list";
	}

	@GetMapping("/board/edit/{id}")
	public String editForm(@PathVariable Long id, Model model, HttpSession session) {
	    User loginUser = (User) session.getAttribute("loginUser");
	    
	    if (loginUser == null) {
	        return "redirect:/user/login";
	    }
	    
	    Board board = boardService.findById(id);
	    
	    // 본인 확인
	    if (!loginUser.getName().equals(board.getWriter())) {
	        throw new RuntimeException("본인이 작성한 글만 수정할 수 있습니다.");
	    }
	    
	    model.addAttribute("board", board);
	    return "board/edit";
	}

	@PostMapping("/board/edit/{id}")
	public String edit(@PathVariable Long id, Board board, HttpSession session) {
		User loginUser = (User) session.getAttribute("loginUser");

		if (loginUser == null) {
			return "redirect:/user/login";
		}

		Board existingBoard = boardService.findById(id);

		if (!loginUser.getName().equals(existingBoard.getWriter())) {
			throw new RuntimeException("You are not the writer!");
		}

		boardService.update(id, board);
		return "redirect:/board/detail/" + id;
	}

	@GetMapping("/board/delete/{id}")
	public String delete(@PathVariable Long id, HttpSession session) {

		User loginUser = (User) session.getAttribute("loginUser");
		Board board = boardService.findById(id);
		if (loginUser == null) {
			return "redirect:/user/login";
		}

		if (!loginUser.getName().equals(board.getWriter())) {
			throw new RuntimeException("you are not the writer!");
		}

		boardService.delete(id);
		return "redirect:/board/list";
	}

	@PostMapping("/board/{id}/like")
	public String toggleLike(@PathVariable Long id, HttpSession session) {
		User loginUser = (User)session.getAttribute("loginUser");
		
		if(loginUser == null) {
			return "redirect:/user/login";
		}
		
		boardLikeService.toggleLike(id, loginUser.getId());
		return "redirect:/board/detail/" + id;
	}
	
	@GetMapping("/board/file/download/{fileId}")
	public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws IOException {
	    BoardFile boardFile = fileService.getFilesByBoard(null).stream()
	        .filter(f -> f.getId().equals(fileId))
	        .findFirst()
	        .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));
	    
	    Path filePath = Paths.get(boardFile.getFilePath());
	    Resource resource = new UrlResource(filePath.toUri());
	    
	    String contentType = Files.probeContentType(filePath);
	    if (contentType == null) {
	        contentType = "application/octet-stream";
	    }
	    
	    String encodedFileName = URLEncoder.encode(boardFile.getOriginalFileName(), StandardCharsets.UTF_8)
	        .replaceAll("\\+", "%20");
	    
	    return ResponseEntity.ok()
	        .contentType(MediaType.parseMediaType(contentType))
	        .header(HttpHeaders.CONTENT_DISPOSITION, 
	                "attachment; filename=\"" + encodedFileName + "\"")
	        .body(resource);
	}
}