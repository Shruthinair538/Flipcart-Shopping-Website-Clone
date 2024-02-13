package com.jsp.fc.serviceimpl;

import java.time.LocalDateTime;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jsp.fc.cache.CacheStore;
import com.jsp.fc.entity.AccessToken;
import com.jsp.fc.entity.Customer;
import com.jsp.fc.entity.RefreshToken;
import com.jsp.fc.entity.Seller;
import com.jsp.fc.entity.User;
import com.jsp.fc.exception.EmailAlreadyVerifiedException;
import com.jsp.fc.exception.InvalidOtpException;
import com.jsp.fc.exception.NoUserExistInCacheException;
import com.jsp.fc.exception.OtpExpiredException;
import com.jsp.fc.exception.UserNameNotFoundException;
import com.jsp.fc.exception.UserNotLoggedInException;
import com.jsp.fc.repository.AccessTokenRepo;
import com.jsp.fc.repository.CustomerRepository;
import com.jsp.fc.repository.RefreshTokenRepo;
import com.jsp.fc.repository.SellerRepository;
import com.jsp.fc.repository.UserRepository;
import com.jsp.fc.requestdto.AuthRequest;
import com.jsp.fc.requestdto.OtpModel;
import com.jsp.fc.requestdto.UserRequest;
import com.jsp.fc.responsedto.AuthResponse;
import com.jsp.fc.responsedto.UserResponse;
import com.jsp.fc.security.JwtService;
import com.jsp.fc.service.AuthService;
import com.jsp.fc.utility.CookieManager;
import com.jsp.fc.utility.MessageStructure;
import com.jsp.fc.utility.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService{


	//Constructor Injection
	private UserRepository userRepo;

	private CustomerRepository customRepo;

	private SellerRepository sellerRepo;

	private ResponseStructure<UserResponse> structure;
	
	private ResponseStructure<AuthResponse> authStructure;
	
	private PasswordEncoder encoder;
	
	private CacheStore<String> otpCache;

	private CacheStore<User> userCachestore;
	
	private JavaMailSender javaMailSender;
	
	private AuthenticationManager authenticationManager;
	
	private CookieManager cookieManager;
	
	@Value("${myapp.access.expiry}")
	private int accessExpiryInSeconds;
	
	@Value("${myapp.refresh.expiry}")
	private int refreshExpiryInSeconds;
	
	private JwtService jwtService;
	
	private AccessTokenRepo accessTokenRepo;
	
	private RefreshTokenRepo refreshTokenRepo;

	

	public AuthServiceImpl(UserRepository userRepo, CustomerRepository customRepo, SellerRepository sellerRepo,
			ResponseStructure<UserResponse> structure, ResponseStructure<AuthResponse> authStructure,
			PasswordEncoder encoder, CacheStore<String> otpCache, CacheStore<User> userCachestore,
			JavaMailSender javaMailSender, AuthenticationManager authenticationManager, CookieManager cookieManager,
			JwtService jwtService, AccessTokenRepo accessTokenRepo, RefreshTokenRepo refreshTokenRepo) {
		super();
		this.userRepo = userRepo;
		this.customRepo = customRepo;
		this.sellerRepo = sellerRepo;
		this.structure = structure;
		this.authStructure = authStructure;
		this.encoder = encoder;
		this.otpCache = otpCache;
		this.userCachestore = userCachestore;
		this.javaMailSender = javaMailSender;
		this.authenticationManager = authenticationManager;
		this.cookieManager = cookieManager;
		this.jwtService = jwtService;
		this.accessTokenRepo = accessTokenRepo;
		this.refreshTokenRepo = refreshTokenRepo;
	}

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
				throw new EmailAlreadyVerifiedException("Email already exist!!");
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
		User user = userCachestore.get(otpModel.getUserEmail());  //Fetched the whole entry using key from cache
	    String otp = otpCache.get(otpModel.getUserEmail());
	    
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
	
	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest,HttpServletResponse response) {
		String userName = authRequest.getUserEmail().split("@")[0];
		String userPassword = authRequest.getUserPassword();
		
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userName, userPassword);
	    Authentication authentication = authenticationManager.authenticate(token);
	    if(!authentication.isAuthenticated()) {
	    	throw new UserNameNotFoundException("Failed to authenticate the user!!");
	    }
	    
	    else 
	    	return userRepo.findByUserName(userName).map(user->{
	    		grantAccess(response, user);
	    		
	    		return ResponseEntity.ok(authStructure.setStatusCode(HttpStatus.OK.value())
	    				.setData(AuthResponse.builder()
	    						.userId(user.getUserId())
	    						.userName(userName)
	    						.userRole(user.getUserRole().name())
	    						.isAuthenticated(true)
	    						.accessExpiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
	    						.refreshExpiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
	    						.build())
	    				.setMessage("Login successfull!!"));
	    		
	    	}).get();
	    	
	    	   	     
	
	}
	
	
	
	private void grantAccess(HttpServletResponse response,User user) {
		//generating access and refresh token
		String accessToken = jwtService.generateAccessToken(user.getUserName());
		String refreshToken = jwtService.generateRefreshToken(user.getUserName());
		
		//adding access and refresh token to response
		response.addCookie(cookieManager.configure(new Cookie("at",accessToken), accessExpiryInSeconds));
		response.addCookie(cookieManager.configure(new Cookie("rt",refreshToken), refreshExpiryInSeconds));
		
		//saving access and refresh cookie to db
		accessTokenRepo.save(AccessToken.builder()
				.token(accessToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
				.build());
		
		refreshTokenRepo.save(RefreshToken.builder()
				.token(refreshToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
				.build());

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
	
	public void deleteExpiredTokens() {
		List<AccessToken> accessToken= accessTokenRepo.findAllByExpirationBeforeAndIsBlocked(LocalDateTime.now(), false);
		accessTokenRepo.deleteAll(accessToken);
		List<RefreshToken> refreshToken =refreshTokenRepo.findAllByExpirationBeforeAndIsBlocked(LocalDateTime.now(), false);
		refreshTokenRepo.deleteAll(refreshToken);
	}

	@Override
	public ResponseEntity<String> revokeAllOtherDevices(String accessToken, String refreshToken,
			HttpServletResponse response) {
   if(accessToken==null && refreshToken==null)
	   throw new UserNotLoggedInException("User is Not LoggedIn !!");
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		if(username == null) throw new UserNameNotFoundException("username not found");

		return userRepo.findByUserName(username)
		.map(user -> {
			blockAccessToken(accessTokenRepo.findByUserAndIsBlockedAndTokenNot(user, false, accessToken));
			blockRefreshToken(refreshTokenRepo.findByUserAndIsBlockedAndTokenNot(user, false, refreshToken));
			
			return ResponseEntity.ok("Revoked from all other devices excluding the current one successfully");
		})
		.orElseThrow(() -> new UserNameNotFoundException("username not found"));
		

	}

	private void blockRefreshToken(List<RefreshToken> refreshTokens) {
		refreshTokens.forEach(refreshToken -> {
			refreshToken.setBlocked(true);
			refreshTokenRepo.save(refreshToken);
		});
		
	}

	private void blockAccessToken(List<AccessToken> accessTokens) {
		accessTokens.forEach(accessToken -> {
			accessToken.setBlocked(true);
			accessTokenRepo.save(accessToken);
		});
		
	}

	@Override
	public ResponseEntity<String> revokeAllDevices(String accessToken, String refreshToken,
			HttpServletResponse response) {
     if(accessToken==null && refreshToken==null) 
	 throw new UserNotLoggedInException("User not logged In");
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		if(username == null) throw new UserNameNotFoundException("username not found");
		
		return userRepo.findByUserName(username)
		.map(user -> {

			blockAccessToken(accessTokenRepo.findByUserAndIsBlocked(user, false));
			blockRefreshToken(refreshTokenRepo.findByUserAndIsBlocked(user, false));

			response.addCookie(cookieManager.invalidateCookie(new Cookie("at", "")));
			response.addCookie(cookieManager.invalidateCookie(new Cookie("rt", "")));
			
			return ResponseEntity.ok("Revoked from all devices successfully");
		})
		.orElseThrow(() -> new UserNameNotFoundException("username not found"));
	}


	
	
	


}
