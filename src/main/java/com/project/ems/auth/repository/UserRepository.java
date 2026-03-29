package com.project.ems.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.ems.common.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	
	Optional<User> findByEmailIgnoreCase(String emailId);
	
	Optional<User> findByPhone(String phoneNumber);
}
