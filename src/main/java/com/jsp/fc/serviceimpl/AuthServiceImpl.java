package com.jsp.fc.serviceimpl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jsp.fc.entity.Customer;
import com.jsp.fc.entity.Seller;
import com.jsp.fc.entity.User;
import com.jsp.fc.exception.EmailAlreadyVerifiedException;
import com.jsp.fc.repository.CustomerRepository;
import com.jsp.fc.repository.SellerRepository;
import com.jsp.fc.repository.UserRepository;
import com.jsp.fc.requestdto.UserRequest;
import com.jsp.fc.responsedto.UserResponse;
import com.jsp.fc.service.AuthService;
import com.jsp.fc.utility.ResponseStructure;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService{


	//Constructor Injection
	private UserRepository userRepo;

	private CustomerRepository customRepo;

	private SellerRepository sellerRepo;

	private ResponseStructure<UserResponse> structure;
	
	private PasswordEncoder encoder;

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
			User user = userRepo.findByUserEmail(request.getUserEmail());//find that user data
			if (user.isEmailVerified()) {  
				// If email is already verified, throw exception
				throw new EmailAlreadyVerifiedException("UserEmail is already verified!");
			} else {
				// If email exists but not verified, send verification email and return response
				sendVerificationEmail(user);  //verification will be sent

				return new ResponseEntity<ResponseStructure<UserResponse>>(
						structure.setStatusCode(HttpStatus.ACCEPTED.value())
						.setMessage("Email verification sent. Please verify your email to complete registration.")
						.setData(mapToUserResponse(user)), HttpStatus.ACCEPTED);

			}

		} else {

			// If email doesn't exist or not verified, save user data in the database
			User user = saveUser(request);

			return new ResponseEntity<ResponseStructure<UserResponse>>(
					structure.setStatusCode(HttpStatus.ACCEPTED.value())
					.setMessage("User registered successfully!!").setData(mapToUserResponse(user)),
					HttpStatus.ACCEPTED);

		}

	}

	private void sendVerificationEmail(User user) {
		

	}





}
