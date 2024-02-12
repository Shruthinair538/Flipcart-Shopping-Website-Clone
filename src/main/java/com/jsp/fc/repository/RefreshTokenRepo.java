package com.jsp.fc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.RefreshToken;
import com.jsp.fc.entity.User;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long>{

	Optional<RefreshToken> findByToken(String refreshToken);

}
