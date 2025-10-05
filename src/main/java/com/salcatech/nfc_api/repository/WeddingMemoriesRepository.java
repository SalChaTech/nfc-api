package com.salcatech.nfc_api.repository;

import com.salcatech.nfc_api.model.WeddingMemories;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeddingMemoriesRepository extends JpaRepository<WeddingMemories, Long> {
    Optional<WeddingMemories> findByUserId(Long userId);

}