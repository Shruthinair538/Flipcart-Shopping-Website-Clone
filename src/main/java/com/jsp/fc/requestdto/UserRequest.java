package com.jsp.fc.requestdto;

import com.jsp.fc.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

	
	private String userEmail;
	private String userPassword;
	private UserRole userRole;
}
