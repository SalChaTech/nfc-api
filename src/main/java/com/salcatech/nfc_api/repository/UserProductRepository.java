package com.salcatech.nfc_api.repository;

import com.salcatech.nfc_api.model.Product;
import com.salcatech.nfc_api.model.UserProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProductRepository extends JpaRepository<UserProduct, String> {
}