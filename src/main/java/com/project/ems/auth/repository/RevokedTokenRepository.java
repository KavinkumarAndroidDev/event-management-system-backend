package com.project.ems.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ems.common.entity.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Integer>{

    boolean existsByToken(String token);
}
