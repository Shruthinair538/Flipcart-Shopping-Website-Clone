package com.jsp.fc.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jsp.fc.exception.UserNameNotFoundException;
import com.jsp.fc.repository.UserRepository;

@Service
public class CustomUserDeatilsService implements UserDetailsService{
	
	@Autowired
	private UserRepository userRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepo.findByUserName(username).map(user ->
			new CustomUserDetails(user)).orElseThrow(()-> new UserNameNotFoundException("Failed to authentic the user"));
	}

}
