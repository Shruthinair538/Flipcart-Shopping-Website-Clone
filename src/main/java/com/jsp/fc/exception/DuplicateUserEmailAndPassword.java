package com.jsp.fc.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DuplicateUserEmailAndPassword extends RuntimeException {

	private String message;
}
