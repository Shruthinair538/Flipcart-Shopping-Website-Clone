package com.jsp.fc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.RefreshToken;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long>{

}
