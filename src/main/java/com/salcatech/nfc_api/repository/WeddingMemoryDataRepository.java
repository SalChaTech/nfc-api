package com.salcatech.nfc_api.repository;

import com.salcatech.nfc_api.model.WeddingMemoryData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeddingMemoryDataRepository extends JpaRepository<WeddingMemoryData, Long> {
    Optional<WeddingMemoryData> findById(Long id);
    Optional<WeddingMemoryData> findByUserProductId(String userProductId);


}