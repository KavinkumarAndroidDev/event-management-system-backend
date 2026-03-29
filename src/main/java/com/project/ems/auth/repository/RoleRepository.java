package com.project.ems.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.ems.common.entity.Role;
import com.project.ems.common.entity.RoleName;

public interface RoleRepository extends JpaRepository<Role, Integer> {

	Optional<Role> findByName(RoleName attendee);

}
