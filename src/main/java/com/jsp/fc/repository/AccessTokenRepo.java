package com.jsp.fc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.AccessToken;
import com.jsp.fc.entity.User;

public interface AccessTokenRepo extends JpaRepository<AccessToken, Long>{

	Optional<AccessToken> findByToken(String accessToken);

}
