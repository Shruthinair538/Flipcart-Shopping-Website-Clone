package com.jsp.fc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.fc.entity.User;
import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer>{

	User findByUserEmail(String userEmail);

	boolean existsByUserEmail(String userEmail);

	Optional<User> findByUserName(String username);


}
