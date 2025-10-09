package com.salcatech.nfc_api.repository;

import com.salcatech.nfc_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}