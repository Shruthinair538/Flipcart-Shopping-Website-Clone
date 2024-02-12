package com.jsp.fc.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.AccessToken;

public interface AccessTokenRepo extends JpaRepository<AccessToken, Long>{
	List<AccessToken> findAllByExpirationBeforeAndIsBlocked (LocalDateTime expiration,boolean isBlocked);

}
