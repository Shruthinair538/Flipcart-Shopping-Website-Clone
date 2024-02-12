package com.jsp.fc.service;

import org.springframework.http.ResponseEntity;

import com.jsp.fc.requestdto.AuthRequest;
import com.jsp.fc.requestdto.OtpModel;
import com.jsp.fc.requestdto.UserRequest;
import com.jsp.fc.responsedto.AuthResponse;
import com.jsp.fc.responsedto.UserResponse;
import com.jsp.fc.utility.ResponseStructure;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	ResponseEntity<ResponseStructure<UserResponse>> registerUser(UserRequest request);

	ResponseEntity<ResponseStructure<UserResponse>> veifyOtp(OtpModel otpModel);

	ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest, HttpServletResponse response);

	ResponseEntity<String> userLogout(String refreshToken, String accessToken, HttpServletResponse response);

}
