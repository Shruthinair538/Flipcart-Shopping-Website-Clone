package com.jsp.fc.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jsp.fc.entity.AccessToken;
import com.jsp.fc.repository.AccessTokenRepo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Component
@AllArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter{

	private AccessTokenRepo accessTokenRepo;
	private CustomUserDeatilsService customUserDetailsService;
	private JwtService jwtService;
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Cookie[] cookies=request.getCookies();

		if(cookies!=null) {
			String at=null;
			String rt=null;
			for(Cookie cookie:cookies) {
				if(cookie.getName().equals("at"))
					at=cookie.getValue();
				if(cookie.getName().equals("rt"))
					rt=cookie.getValue();
			}
			String username=null;
			if(at!=null && rt!=null) {
				Optional<AccessToken> accesstoken = accessTokenRepo.findByTokenAndIsBlocked(at,false);
				if(accesstoken==null)throw new RuntimeException();
				else {
					log.info("authenticating the token");
					username=jwtService.extractUserName(at);
					UserDetails userdetails=customUserDetailsService.loadUserByUsername(username);
					UsernamePasswordAuthenticationToken token=new UsernamePasswordAuthenticationToken(username,null, userdetails.getAuthorities());
					token.setDetails(new WebAuthenticationDetails(request));
					SecurityContextHolder.getContext().setAuthentication(token);
					log.info("authenticated successfully");

				}
				filterChain.doFilter(request, response);
			}
		}	
	}
}





