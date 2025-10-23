package com.salcatech.nfc_api.repository;

import com.salcatech.nfc_api.model.PlaceToVisitData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceToVisitDataRepository extends JpaRepository<PlaceToVisitData, Long> {
    Optional<PlaceToVisitData> findById(Long id);
    Optional<PlaceToVisitData> findByUserProductId(String userProductId);


}