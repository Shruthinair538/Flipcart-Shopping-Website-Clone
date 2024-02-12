package com.jsp.fc.utility;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jsp.fc.entity.User;
import com.jsp.fc.repository.UserRepository;
import com.jsp.fc.serviceimpl.AuthServiceImpl;

@Component
public class ScheduledJobs {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private AuthServiceImpl authService;

	
	@Scheduled(fixedDelay = 1000l*60*60*24) //one day
	public void test() {
		deleteIfNotVerified();
		authService.deleteExpiredTokens();
		
	}

	private void deleteIfNotVerified() {
		 for (User user : userRepo.findAll()) {
		        if (Boolean.FALSE.equals(user.isEmailVerified())) {
		            userRepo.delete(user);
		        }
		    }
		}
	
	
	

}

