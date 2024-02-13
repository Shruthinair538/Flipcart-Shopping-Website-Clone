package com.jsp.fc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jsp.fc.requestdto.AuthRequest;
import com.jsp.fc.requestdto.OtpModel;
import com.jsp.fc.requestdto.UserRequest;
import com.jsp.fc.responsedto.AuthResponse;
import com.jsp.fc.responsedto.UserResponse;
import com.jsp.fc.service.AuthService;
import com.jsp.fc.utility.ResponseStructure;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class AuthController {
	
	@Autowired
	private AuthService service;

	@PostMapping("/register")
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(@RequestBody UserRequest request){
		return service.registerUser(request);
	}
	
	@PostMapping("/verify-otp")
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOtp(@RequestBody OtpModel otpModel){
		return service.veifyOtp(otpModel);
	}
	
	@PostMapping("/login")
	public ResponseEntity<ResponseStructure<AuthResponse>> login(@RequestBody AuthRequest authRequest,HttpServletResponse response){
		return service.login(authRequest,response);
	}
	
	@PreAuthorize("hasAuthority('CUSTOMER') OR hasAuthority('SELLER')")
	@PutMapping("/revoke-all")
	public ResponseEntity<String> revokeAllDevices(@CookieValue(name = "at", required = false) String accessToken,
			@CookieValue(name = "rt", required = false) String refreshToken,HttpServletResponse response){
		return service.revokeAllDevices(accessToken, refreshToken,response);
	}
	
	@PreAuthorize("hasAuthority('CUSTOMER') OR hasAuthority('SELLER')")
	@PutMapping("/revoke-other")
	public ResponseEntity<String> revokeAllOtherDevices(@CookieValue(name = "at", required = false) String accessToken,
			@CookieValue(name = "rt", required = false) String refreshToken,HttpServletResponse response){
		return service.revokeAllOtherDevices(accessToken, refreshToken,response);
	

}
}
