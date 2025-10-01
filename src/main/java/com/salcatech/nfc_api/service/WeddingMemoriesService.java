package com.salcatech.nfc_api.service;

import com.salcatech.nfc_api.model.WeddingMemories;
import com.salcatech.nfc_api.repository.WeddingMemoriesRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WeddingMemoriesService {

    private final WeddingMemoriesRepository weddingMemoriesRepository;

    public WeddingMemoriesService(WeddingMemoriesRepository weddingMemoriesRepository) {
        this.weddingMemoriesRepository = weddingMemoriesRepository;
    }

    public WeddingMemories saveWeddingMemories(WeddingMemories memories) {
        return weddingMemoriesRepository.save(memories);
    }

    public WeddingMemories updateWeddingMemories(Long id, WeddingMemories memory) {
        WeddingMemories existing = weddingMemoriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wedding memory not found with id " + id));

        existing.setMaleName(memory.getMaleName());
        existing.setFemaleName(memory.getFemaleName());
        existing.setDate(memory.getDate());
        existing.setUser(memory.getUser());

        return weddingMemoriesRepository.save(existing);
    }

    public void deleteWeddingMemories(Long id) {
        weddingMemoriesRepository.deleteById(id);
    }

    public List<WeddingMemories> getAllWeddingMemories() {
        return weddingMemoriesRepository.findAll();
    }
}
