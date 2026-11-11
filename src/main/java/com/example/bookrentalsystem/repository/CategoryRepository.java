package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}