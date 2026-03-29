package com.project.ems.category.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryCreateRequest {
	
	@NotBlank(message = "Category must not be empty")
	private String categoryName;

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	
	
}
