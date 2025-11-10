package com.example.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
	
	@GetMapping("/user/profile")
	public String profileForm(HttpSession session, Model model) {
		User loginUser = (User)session.getAttribute("loginUser");
		
		if(loginUser == null) {
			return "redirect:/user/login";
		} 
		
		
		model.addAttribute("user", loginUser);
		
		return "user/profile";
	}
	
	@PostMapping("/user/profile/update")
	public String updateUserProfile(@RequestParam String name, @RequestParam String email, HttpSession session) {
		User loginUser = (User)session.getAttribute("loginUser");
			
		if(loginUser == null) {
			return "redirect:/user/login";
		}
		
		User updatedUser = userService.updateProfile(loginUser.getId(), name, email);
		session.setAttribute("loginUser", updatedUser);
		
		return "redirect:/user/profile";
	}
	
	@PostMapping("/user/profile/change-password")
	public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword, HttpSession session) {
		User loginUser = (User)session.getAttribute("loginUser");
		
		if(loginUser == null) {
			return "redirect:/user/login";
		}
		
		if(userService.changePassword(loginUser.getId(), currentPassword, newPassword)) {
			return "redirect:/user/profile";
		}
		
		return "redirect:/user/login";
	}
	
	@PostMapping("/user/profile/delete")
	public String deleteUser(@RequestParam String password, HttpSession session) {
		User loginUser = (User)session.getAttribute("loginUser");
				
		if(loginUser == null) {
			return "redirect:/user/login";
		}
		
		if(userService.deleteAccount(loginUser.getId(), password)) {
			session.invalidate();
			return "redirect:/user/login";
		} else {
			return "redirect:/user/profile";
		}
	}
}
