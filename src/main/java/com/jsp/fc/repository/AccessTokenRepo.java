package com.jsp.fc.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.AccessToken;
import com.jsp.fc.entity.User;

public interface AccessTokenRepo extends JpaRepository<AccessToken, Long>{
	Optional<AccessToken> findByToken(String token);
	List<AccessToken> findAllByExpirationBeforeAndIsBlocked (LocalDateTime expiration,boolean isBlocked);
	Optional<AccessToken> findByTokenAndIsBlocked(String at,boolean b);
	
	List<AccessToken> findByUserAndIsBlocked(User user, boolean isBlocked);

	List<AccessToken> findByUserAndIsBlockedAndTokenNot(User user, boolean isBlocked, String accessToken);}
