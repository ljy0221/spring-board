package com.example.board.controller;

import com.example.board.entity.Comment;
import com.example.board.entity.User;
import com.example.board.service.CommentService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CommentController {
	private final CommentService commentService;

	@PostMapping("/comment/write")
	public String write(@RequestParam Long boardId, @RequestParam String content, HttpSession session) {

		User loginUser = (User) session.getAttribute("loginUser");

		if (loginUser == null) {
			return "redirect:/user/login";
		}

		commentService.save(boardId, loginUser.getName(), content);

		return "redirect:/board/detail/" + boardId;
	}

	@GetMapping("/comment/delete/{id}")
	public String delete(@PathVariable Long id, @RequestParam Long boardId, HttpSession session) {

		User loginUser = (User) session.getAttribute("loginUser");

		if (loginUser == null) {
			return "redirect:/user/login";
		}

		Comment comment = commentService.findById(id);

		if (!loginUser.getName().equals(comment.getWriter())) {
			throw new RuntimeException("본인이 작성한 댓글만 삭제할 수 있습니다.");
		}

		commentService.delete(id);

		return "redirect:/board/detail/" + boardId;
	}
}