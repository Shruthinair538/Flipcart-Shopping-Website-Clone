package com.jsp.fc.serviceimpl;

import java.util.Date;

import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jsp.fc.cache.CacheStore;
import com.jsp.fc.entity.Customer;
import com.jsp.fc.entity.Seller;
import com.jsp.fc.entity.User;
import com.jsp.fc.exception.EmailAlreadyVerifiedException;
import com.jsp.fc.exception.InvalidOtpException;
import com.jsp.fc.exception.NoUserExistInCacheException;
import com.jsp.fc.exception.OtpExpiredException;
import com.jsp.fc.repository.CustomerRepository;
import com.jsp.fc.repository.SellerRepository;
import com.jsp.fc.repository.UserRepository;
import com.jsp.fc.requestdto.OtpModel;
import com.jsp.fc.requestdto.UserRequest;
import com.jsp.fc.responsedto.UserResponse;
import com.jsp.fc.service.AuthService;
import com.jsp.fc.utility.MessageStructure;
import com.jsp.fc.utility.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{


	//Constructor Injection
	private UserRepository userRepo;

	private CustomerRepository customRepo;

	private SellerRepository sellerRepo;

	private ResponseStructure<UserResponse> structure;
	
	private PasswordEncoder encoder;
	
	private CacheStore<String> otpCache;

	private CacheStore<User> userCachestore;
	
	private JavaMailSender javaMailSender;

	public <T extends User>T mapToUser(UserRequest request) {
		User user=null;

		switch (request.getUserRole()){
		case CUSTOMER: {
			user=new Customer();
			break;
		}
		case SELLER: {
			user=new Seller();
			break;
		}

		}
		user.setUserPassword(encoder.encode(request.getUserPassword()));
		user.setUserEmail(request.getUserEmail());
		user.setUserRole(request.getUserRole());
		user.setDeleted(false);
		user.setEmailVerified(false);
		user.setUserName(request.getUserEmail().split("@")[0]);//username will be generated from email

		return (T)user;
	}

	public UserResponse mapToUserResponse(User user) {
		return UserResponse.builder()
				.userId(user.getUserId())
				.userName(user.getUserName())
				.userEmail(user.getUserEmail())
				.userRole(user.getUserRole())
				.build();
	}


	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest request) {

		if (userRepo.existsByUserEmail(request.getUserEmail())) {   //if email exists
				// If email is already verified, throw exception
				throw new RuntimeException("Email doesnt exist!!");
			} 
		
		String otp=genetateOtp();
		User user = mapToUser(request);
		userCachestore.add(request.getUserEmail(), user);
		otpCache.add(request.getUserEmail(), otp);
		
		try {
			sentOtpToMail(user, otp);
		} catch (MessagingException e) {
			log.error("The emailId doesn't exist!! ");
		}
		
		return new ResponseEntity<ResponseStructure<UserResponse>>(
				structure.setStatusCode(HttpStatus.ACCEPTED.value())
				.setMessage("Otp has been sent,Please verify the otp sent through email. ")
				.setData(mapToUserResponse(user)), HttpStatus.ACCEPTED);
		
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> veifyOtp(OtpModel otpModel) {
		User user = userCachestore.get(otpModel.getEmail());  //Fetched the whole entry using key from cache
	    String otp = otpCache.get(otpModel.getEmail());
	    
	    if(user == null)
	    	throw new NoUserExistInCacheException("Registration has expired!!");
	    
	    if(otp == null) {
	    	throw new OtpExpiredException("Otp has expired!!");
	    }
	    
	    if(!otp.equals(otpModel.getOtp()))
	    	throw new InvalidOtpException("Invalid otp!!");
	    
	    user.setEmailVerified(true);
	    userRepo.save(user);
	    
	    try {
			sendConfirmationMail(user);
		} catch (MessagingException e) {
			log.error("Registration unsuccessful!!");
		}
	    
	    
	    
	    structure.setStatusCode(HttpStatus.CREATED.value());
	    structure.setMessage("User object registered successfully!!");
	    structure.setData(mapToUserResponse(user));
	    return new ResponseEntity<ResponseStructure<UserResponse>>(structure,HttpStatus.CREATED);
	    
	}
	
	
	
	private <T extends User>T saveUser(UserRequest request) {
		User user=null;

		switch (request.getUserRole()){
		case CUSTOMER: {
			user=customRepo.save(mapToUser(request));
			break;
		}
		case SELLER: {
			user=sellerRepo.save(mapToUser(request));
			break;
		}
		}
		return (T)user;

	}
	
	private String genetateOtp() {
		return String.valueOf(new Random().nextInt(100000, 999999));  //generates 6 digits otp
	}
	
	@Async
	private void sendMail(MessageStructure message) throws MessagingException {  //type as void because it should work asynchronously
		MimeMessage mimeMessage=javaMailSender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(mimeMessage, true);
		helper.setTo(message.getTo());
		helper.setSubject(message.getSubject());
		helper.setSentDate(message.getSentDate());
		helper.setText(message.getText(),true);
		javaMailSender.send(mimeMessage);
	}
	
	private void sentOtpToMail(User user,String otp) throws MessagingException {
		sendMail(MessageStructure.builder()
				.to(user.getUserEmail())
				.subject("Complete your registration process")
				.sentDate(new Date())
				.text("Welcome, "+user.getUserName()
				+ "Complete your registration using the otp sent to your email. "
				+otp+" is ur otp which is valid only for the next 10 minutes.")
				.build());
		
		
	}
	
	private void sendConfirmationMail(User user) throws MessagingException {
		sendMail(MessageStructure.builder()
				.to(user.getUserEmail())
				.subject("Registration complete")
				.sentDate(new Date())
				.text("Welcome, "+user.getUserName()
				+ "You are registered successfully!! ")
				.build());
		
		
	}
	
		
	



	





}
