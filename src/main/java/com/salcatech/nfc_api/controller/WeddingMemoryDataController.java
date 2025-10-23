package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.dto.ApiResponse;
import com.salcatech.nfc_api.dto.WeddingMemoryDataDTO;
import com.salcatech.nfc_api.dto.request.WeddingMemoryRequest;
import com.salcatech.nfc_api.exception.ForbiddenOperationException;
import com.salcatech.nfc_api.exception.UserProductNotFoundException;
import com.salcatech.nfc_api.exception.WeddingMemoryDataNotFoundException;
import com.salcatech.nfc_api.model.UserProduct;
import com.salcatech.nfc_api.model.WeddingMemoryData;
import com.salcatech.nfc_api.service.UserProductService;
import com.salcatech.nfc_api.service.WeddingMemoryDataService;
import com.salcatech.nfc_api.util.JwtAuthenticationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/by-product-id/{productId}")
    public ResponseEntity<ApiResponse<WeddingMemoryDataDTO>> getWeddingMemoriesByProductId(@PathVariable String productId) throws WeddingMemoryDataNotFoundException {
        WeddingMemoryData memory = weddingMemoriesService.getWeddingMemoryByProductId(productId);

        if (memory == null) {
            throw new WeddingMemoryDataNotFoundException();
        }

        WeddingMemoryDataDTO memoryDataDTO = new WeddingMemoryDataDTO(memory.getMaleName(), memory.getFemaleName(), memory.getDate());

        return ResponseEntity.ok(new ApiResponse<>(true, "Wedding memory found", memoryDataDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<WeddingMemoryDataDTO>> addWeddingMemories(@RequestBody WeddingMemoryData memories) {
        WeddingMemoryData weddingMemoryData = weddingMemoriesService.saveWeddingMemories(memories);
        WeddingMemoryDataDTO memoryDataDTO = new WeddingMemoryDataDTO(weddingMemoryData.getMaleName(), weddingMemoryData.getFemaleName(), weddingMemoryData.getDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Wedding memory created successfully", memoryDataDTO));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{productId}/update")
    public ResponseEntity<ApiResponse<WeddingMemoryDataDTO>> updateWeddingMemory(
            @PathVariable String productId,
            @RequestBody WeddingMemoryRequest request
    ) throws UserProductNotFoundException, ForbiddenOperationException {
        String email = JwtAuthenticationUtil.getEmail();
        logger.info("Authenticated email: {}", email);

        UserProduct userProduct = userProductService.getById(productId);

        if (userProduct.getEmail() == null || !userProduct.getEmail().equals(email)) {
            throw new ForbiddenOperationException("You are not allowed to update this wedding memory");
        }

        WeddingMemoryData memory = weddingMemoriesService.getWeddingMemoryByProductId(productId);

        if (memory == null) {
            memory = new WeddingMemoryData();
            memory.setUserProduct(userProduct);
            logger.info("No existing memory found. Creating new record for productId {}", productId);
        }

        if (request.getMaleName() != null) memory.setMaleName(request.getMaleName());
        if (request.getFemaleName() != null) memory.setFemaleName(request.getFemaleName());
        if (request.getDate() != null) memory.setDate(request.getDate());

        WeddingMemoryData saved = weddingMemoriesService.saveWeddingMemories(memory);

        return ResponseEntity.ok(new ApiResponse<>(true, "Wedding memory updated successfully", new WeddingMemoryDataDTO(saved.getMaleName(), saved.getFemaleName(), saved.getDate())));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteWeddingMemories(@PathVariable Long id) throws ForbiddenOperationException, WeddingMemoryDataNotFoundException {
        WeddingMemoryData memory = weddingMemoriesService.getWeddingMemoryById(id);

        String email = JwtAuthenticationUtil.getEmail();
        UserProduct product = memory.getUserProduct();
        if (product.getEmail() == null || !product.getEmail().equals(email)) {
            throw new ForbiddenOperationException("You are not allowed to delete this record");
        }

        weddingMemoriesService.deleteWeddingMemories(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Wedding memory deleted successfully", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<WeddingMemoryData> getAllWeddingMemories() {
        return weddingMemoriesService.getAllWeddingMemories();
    }
}
