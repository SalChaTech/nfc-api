package com.salcatech.nfc_api.repository;

import com.salcatech.nfc_api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
