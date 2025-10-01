package com.salcatech.nfc_api.repository;

import com.salcatech.nfc_api.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Users findByEmail(String email);
}

