package com.jsp.fc.responsedto;

import com.jsp.fc.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

	private int userId;
	private String userName;
	private String userEmail;
	private UserRole userRole;
}
