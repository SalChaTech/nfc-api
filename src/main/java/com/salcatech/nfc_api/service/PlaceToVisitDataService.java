package com.salcatech.nfc_api.service;

import com.salcatech.nfc_api.exception.PlaceToVisitDataNotFoundException;
import com.salcatech.nfc_api.model.PlaceToVisitData;
import com.salcatech.nfc_api.repository.PlaceToVisitDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaceToVisitDataService {

    private final PlaceToVisitDataRepository placeToVisitDataRepository;

    public PlaceToVisitDataService(PlaceToVisitDataRepository placeToVisitDataRepository) {
        this.placeToVisitDataRepository = placeToVisitDataRepository;
    }

    public PlaceToVisitData savePlaceToVisitData(PlaceToVisitData memories) {
        return placeToVisitDataRepository.save(memories);
    }


    public void deletePlaceToVisitData(Long id) {
        placeToVisitDataRepository.deleteById(id);
    }

    public List<PlaceToVisitData> getAllPlaceToVisitData() {
        return placeToVisitDataRepository.findAll();
    }

    public PlaceToVisitData getPlaceToVisitDataById(Long id) throws PlaceToVisitDataNotFoundException {
        return placeToVisitDataRepository.findById(id).orElseThrow(PlaceToVisitDataNotFoundException::new);
    }

    public PlaceToVisitData getPlaceToVisitDataByProductId(String productId) {
        return placeToVisitDataRepository.findByUserProductId(productId).orElse(null);
    }


}
