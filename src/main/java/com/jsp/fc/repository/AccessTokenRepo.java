package com.jsp.fc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.AccessToken;

public interface AccessTokenRepo extends JpaRepository<AccessToken, Long>{

}
