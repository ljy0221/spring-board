package com.example.board.controller;

import com.example.board.config.AppConfig;
import com.example.board.entity.Board;
import com.example.board.entity.Comment;
import com.example.board.entity.User;
import com.example.board.service.BoardLikeService;
import com.example.board.service.BoardService;
import com.example.board.service.CommentService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {

	private final AppConfig appConfig;
	private final BoardService boardService;
	private final CommentService commentService;
	private final BoardLikeService boardLikeService;

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
		
		model.addAttribute("board", board);
		model.addAttribute("comments", comments);
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("isLiked", isLiked);
		
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
	public String write(Board board, HttpSession session) {
		User loginUser = (User) session.getAttribute("loginUser");

		if (loginUser == null) {
			return "redirect:/user/login";
		}

		board.setWriter(loginUser.getName());

		boardService.save(board);
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
}