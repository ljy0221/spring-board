package com.example.board.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.board.controller.BoardController;
import com.example.board.entity.User;
import com.example.board.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public User register(User user) {
		if(userRepository.existsByUsername(user.getUsername())) {
			throw new RuntimeException("Already Exists username");
			
		}
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
		
		return userRepository.save(user);
	}
	
	public User login(String username, String password) {
		User user = userRepository.findByUsername(username).orElse(null);
		
		if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
	        return null;
	    }
	    
	    return user;
	}
}
