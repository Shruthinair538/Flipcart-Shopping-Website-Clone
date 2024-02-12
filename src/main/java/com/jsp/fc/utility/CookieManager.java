package com.jsp.fc.utility;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
@Component
public class CookieManager {  //to configure cookie
	
	@Value("${myapp.domain}")
	private String domain;

	public Cookie configure(Cookie cookie,int expirationInSeconds) {
		cookie.setDomain(domain);
		cookie.setSecure(false);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(expirationInSeconds);
		
		return cookie;
	}
	
	public Cookie invalidateCookie(Cookie cookie) {  //to remove cookie from the browser
		cookie.setPath("/");
		cookie.setMaxAge(0);
		
		return cookie;
	}
}
