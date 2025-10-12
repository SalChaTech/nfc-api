package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.dto.WeddingMemoryRequest;
import com.salcatech.nfc_api.model.UserProduct;
import com.salcatech.nfc_api.model.WeddingMemoryData;
import com.salcatech.nfc_api.service.UserProductService;
import com.salcatech.nfc_api.service.WeddingMemoryDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wedding-memory-data")
public class WeddingMemoryDataController {

    private static final Logger logger = LoggerFactory.getLogger(WeddingMemoryDataController.class);

    private final UserProductService userProductService;
    private final WeddingMemoryDataService weddingMemoriesService;

    public WeddingMemoryDataController(UserProductService userProductService, WeddingMemoryDataService weddingMemoriesService) {
        this.userProductService = userProductService;
        this.weddingMemoriesService = weddingMemoriesService;
    }

    // 📌 1️⃣ GET - Herkese açık (örneğin QR ile erişim)
    @GetMapping("/by-product-id/{productId}")
    public ResponseEntity<?> getWeddingMemoriesByProductId(@PathVariable String productId) {
        WeddingMemoryData memory = weddingMemoriesService.getWeddingMemoryByProductId(productId);

        if (memory == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Wedding memory not found for product ID: " + productId);
        }

        return ResponseEntity.ok(memory);
    }

    // 📌 2️⃣ POST - Yeni düğün kaydı oluşturma (admin veya backend tarafında)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> addWeddingMemories(@RequestBody WeddingMemoryData memories) {
        WeddingMemoryData saved = weddingMemoriesService.saveWeddingMemories(memories);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{productId}/update")
    public ResponseEntity<?> updateWeddingMemory(
            @PathVariable String productId,
            @RequestBody WeddingMemoryRequest request,
            Authentication auth
    ) {
        String email = auth.getName(); // JWT filter'dan gelen mail
        logger.info("Authenticated email: {}", email);

        // 1️⃣ İlgili UserProduct bulunur
        UserProduct userProduct = userProductService.getById(productId);
        if (userProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UserProduct not found");
        }

        // 2️⃣ Sahiplik kontrolü
        if (userProduct.getEmail() == null || !userProduct.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not allowed to update this wedding memory");
        }

        // 3️⃣ Mevcut WeddingMemories kaydını getir
        WeddingMemoryData memory = weddingMemoriesService.getWeddingMemoryByProductId(productId);

        // 4️⃣ Eğer yoksa → yeni oluştur
        if (memory == null) {
            memory = new WeddingMemoryData();
            memory.setUserProduct(userProduct);
            logger.info("No existing memory found. Creating new record for productId {}", productId);
        }

        // 5️⃣ Alanları güncelle
        if (request.getMaleName() != null) memory.setMaleName(request.getMaleName());
        if (request.getFemaleName() != null) memory.setFemaleName(request.getFemaleName());
        if (request.getDate() != null) memory.setDate(request.getDate());

        // 6️⃣ Kaydet
        WeddingMemoryData saved = weddingMemoriesService.saveWeddingMemories(memory);

        return ResponseEntity.ok(Map.of(
                "message", (memory.getId() == null ? "Created new record" : "Updated existing record"),
                "status", "success",
                "data", saved
        ));
    }


    // 📌 4️⃣ DELETE (opsiyonel, sadece admin veya sahip)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWeddingMemories(@PathVariable Long id, Authentication auth) {
        WeddingMemoryData memory = weddingMemoriesService.getWeddingMemoryById(id);
        if (memory == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wedding memory not found");
        }

        String email = auth.getName();
        UserProduct product = memory.getUserProduct();
        if (product.getEmail() == null || !product.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not allowed to delete this record");
        }

        weddingMemoriesService.deleteWeddingMemories(id);
        return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
    }

    // 📌 5️⃣ GET all (admin)
    @GetMapping
    public List<WeddingMemoryData> getAllWeddingMemories() {
        return weddingMemoriesService.getAllWeddingMemories();
    }
}
