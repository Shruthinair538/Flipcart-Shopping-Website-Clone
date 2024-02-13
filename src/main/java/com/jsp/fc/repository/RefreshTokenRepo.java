package com.jsp.fc.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.RefreshToken;
import com.jsp.fc.entity.User;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long>{
	Optional<RefreshToken> findByToken(String token);
	List<RefreshToken> findAllByExpirationBeforeAndIsBlocked (LocalDateTime expiration,boolean isBlocked);

	List<RefreshToken> findByUserAndIsBlocked(User user, boolean isBlocked);

	List<RefreshToken> findByUserAndIsBlockedAndTokenNot(User user, boolean b,
			String token);
	

}
