package com.example.ECM.repository;

import com.example.ECM.model.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    @EntityGraph(attributePaths = "products") // Tự động tải products khi lấy category
    Optional<Category> findById(Long id);
}
