package com.salcatech.nfc_api.service;

import com.salcatech.nfc_api.model.UserProduct;
import com.salcatech.nfc_api.model.WeddingMemoryData;
import com.salcatech.nfc_api.repository.WeddingMemoryDataRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class WeddingMemoryDataService {

    private final WeddingMemoryDataRepository weddingMemoriesRepository;

    public WeddingMemoryDataService(WeddingMemoryDataRepository weddingMemoriesRepository) {
        this.weddingMemoriesRepository = weddingMemoriesRepository;
    }

    // ✅ Create veya Update (Upsert)
    public WeddingMemoryData upsertWeddingMemories(UserProduct userProduct, WeddingMemoryData newData) {
        Optional<WeddingMemoryData> existingOpt = weddingMemoriesRepository.findByUserProductId(userProduct.getId());

        WeddingMemoryData memory = existingOpt.orElseGet(() -> {
            WeddingMemoryData newMemory = new WeddingMemoryData();
            newMemory.setUserProduct(userProduct);
            return newMemory;
        });

        // Null olmayan alanları güncelle
        if (newData.getMaleName() != null) memory.setMaleName(newData.getMaleName());
        if (newData.getFemaleName() != null) memory.setFemaleName(newData.getFemaleName());
        if (newData.getDate() != null) memory.setDate(newData.getDate());

        return weddingMemoriesRepository.save(memory);
    }

    // 🔹 Normal save (manuel create/update için)
    public WeddingMemoryData saveWeddingMemories(WeddingMemoryData memories) {
        return weddingMemoriesRepository.save(memories);
    }

    // 🔹 Klasik update (ID üzerinden)
    public WeddingMemoryData updateWeddingMemories(Long id, WeddingMemoryData memory) {
        WeddingMemoryData existing = weddingMemoriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wedding memory not found with id " + id));

        existing.setMaleName(memory.getMaleName());
        existing.setFemaleName(memory.getFemaleName());
        existing.setDate(memory.getDate());
        existing.setUserProduct(memory.getUserProduct());

        return weddingMemoriesRepository.save(existing);
    }

    // 🔹 Delete
    public void deleteWeddingMemories(Long id) {
        weddingMemoriesRepository.deleteById(id);
    }

    // 🔹 Tüm kayıtları getir
    public List<WeddingMemoryData> getAllWeddingMemories() {
        return weddingMemoriesRepository.findAll();
    }
    public WeddingMemoryData getWeddingMemoryById(Long id) {
        return weddingMemoriesRepository.findById(id).orElse(null);
    }
    // 🔹 UserProduct ID’ye göre WeddingMemory bul
    public WeddingMemoryData getWeddingMemoryByProductId(String productId) {
        return weddingMemoriesRepository.findByUserProductId(productId).orElse(null);
    }

    // 🔹 Eğer userId ile ilişkilendirmek gerekiyorsa (eski metod)
    public WeddingMemoryData getWeddingMemoryByUserId(Long userId) {
        return weddingMemoriesRepository.findById(userId).orElse(null);
    }
}
