package com.example.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.board.entity.User;
import com.example.board.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@GetMapping("/user/register")
	public String registerForm() {
		return "user/register";
	}

	@PostMapping("/user/register")
	public String resgister(User user) {
		try {
			userService.register(user);
			return "redirect:/user/login";
		} catch (RuntimeException e) {
			// 실제로는 Model에 에러 메시지를 담아서 뷰로 전달해야 함
			return "redirect:/user/register";
		}
	}

	@GetMapping("/user/login")
	public String loginForm() {
		return "user/login";
	}

	@PostMapping("/user/login")
	public String login(@RequestParam String username, @RequestParam String password, HttpSession session) {
		User user = userService.login(username, password);

		if (user == null) {
			return "redirect:/user/login";
		}

		session.setAttribute("loginUser", user);
		return "redirect:/board/list";
	}

	@GetMapping("/user/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/user/login";
	}
}
