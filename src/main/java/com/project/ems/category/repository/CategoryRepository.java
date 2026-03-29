package com.project.ems.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.ems.common.entity.Category;
import com.project.ems.common.entity.Status;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	
	List<Category> findByStatus(Status status);
	
	Optional<Category> findByNameIgnoreCase(String name);
}
