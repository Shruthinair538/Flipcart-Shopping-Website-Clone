package com.jsp.fc.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.RefreshToken;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long>{
	List<RefreshToken> findAllByExpirationBeforeAndIsBlocked (LocalDateTime expiration,boolean isBlocked);

}
