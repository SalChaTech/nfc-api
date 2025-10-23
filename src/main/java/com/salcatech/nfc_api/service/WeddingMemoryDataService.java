package com.salcatech.nfc_api.service;

import com.salcatech.nfc_api.exception.WeddingMemoryDataNotFoundException;
import com.salcatech.nfc_api.model.WeddingMemoryData;
import com.salcatech.nfc_api.repository.WeddingMemoryDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeddingMemoryDataService {

    private final WeddingMemoryDataRepository weddingMemoriesRepository;

    public WeddingMemoryDataService(WeddingMemoryDataRepository weddingMemoriesRepository) {
        this.weddingMemoriesRepository = weddingMemoriesRepository;
    }

    public WeddingMemoryData saveWeddingMemories(WeddingMemoryData memories) {
        return weddingMemoriesRepository.save(memories);
    }


    public void deleteWeddingMemories(Long id) {
        weddingMemoriesRepository.deleteById(id);
    }

    public List<WeddingMemoryData> getAllWeddingMemories() {
        return weddingMemoriesRepository.findAll();
    }

    public WeddingMemoryData getWeddingMemoryById(Long id) throws WeddingMemoryDataNotFoundException {
        return weddingMemoriesRepository.findById(id).orElseThrow(WeddingMemoryDataNotFoundException::new);
    }

    public WeddingMemoryData getWeddingMemoryByProductId(String productId) {
        return weddingMemoriesRepository.findByUserProductId(productId).orElse(null);
    }


}
