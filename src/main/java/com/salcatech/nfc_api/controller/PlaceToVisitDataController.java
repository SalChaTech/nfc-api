package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.dto.ApiResponse;
import com.salcatech.nfc_api.dto.PlaceToVisitDataDTO;
import com.salcatech.nfc_api.dto.request.PlaceToVisitDataRequest;
import com.salcatech.nfc_api.exception.ForbiddenOperationException;
import com.salcatech.nfc_api.exception.PlaceToVisitDataNotFoundException;
import com.salcatech.nfc_api.exception.UserProductNotFoundException;
import com.salcatech.nfc_api.model.PlaceToVisitData;
import com.salcatech.nfc_api.model.UserProduct;
import com.salcatech.nfc_api.service.PlaceToVisitDataService;
import com.salcatech.nfc_api.service.UserProductService;
import com.salcatech.nfc_api.util.JwtAuthenticationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/place-to-visit-data")
public class PlaceToVisitDataController {

    private static final Logger logger = LoggerFactory.getLogger(PlaceToVisitDataController.class);

    private final UserProductService userProductService;
    private final PlaceToVisitDataService placeToVisitDataService;

    public PlaceToVisitDataController(UserProductService userProductService, PlaceToVisitDataService placeToVisitDataService) {
        this.userProductService = userProductService;
        this.placeToVisitDataService = placeToVisitDataService;
    }

    @GetMapping("/by-product-id/{productId}")
    public ResponseEntity<ApiResponse<PlaceToVisitDataDTO>> getPlaceToVisitDataByProductId(@PathVariable String productId) throws PlaceToVisitDataNotFoundException {
        PlaceToVisitData data = placeToVisitDataService.getPlaceToVisitDataByProductId(productId);

        if (data == null) {
            throw new PlaceToVisitDataNotFoundException();
        }

        PlaceToVisitDataDTO dataDTO = new PlaceToVisitDataDTO(data.getName(), data.getDate());

        return ResponseEntity.ok(new ApiResponse<>(true, "Place to visit data found", dataDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<PlaceToVisitDataDTO>> addPlaceToVisitData(@RequestBody PlaceToVisitData memories) {
        PlaceToVisitData PlaceToVisitData = placeToVisitDataService.savePlaceToVisitData(memories);
        PlaceToVisitDataDTO memoryDataDTO = new PlaceToVisitDataDTO(PlaceToVisitData.getName(), PlaceToVisitData.getDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Place to visit data created successfully", memoryDataDTO));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{productId}/update")
    public ResponseEntity<ApiResponse<PlaceToVisitDataDTO>> updatePlaceToVisitData(
            @PathVariable String productId,
            @RequestBody PlaceToVisitDataRequest request
    ) throws UserProductNotFoundException, ForbiddenOperationException {
        String email = JwtAuthenticationUtil.getEmail();
        logger.info("Authenticated email: {}", email);

        UserProduct userProduct = userProductService.getById(productId);

        if (userProduct.getEmail() == null || !userProduct.getEmail().equals(email)) {
            throw new ForbiddenOperationException("You are not allowed to update this place to visit data.");
        }

        PlaceToVisitData data = placeToVisitDataService.getPlaceToVisitDataByProductId(productId);

        if (data == null) {
            data = new PlaceToVisitData();
            data.setUserProduct(userProduct);
            logger.info("No existing data found. Creating new record for productId {}", productId);
        }

        if (request.getName() != null) data.setName(request.getName());
        if (request.getDate() != null) data.setDate(request.getDate());

        PlaceToVisitData saved = placeToVisitDataService.savePlaceToVisitData(data);

        return ResponseEntity.ok(new ApiResponse<>(true, "Place to visit data updated successfully", new PlaceToVisitDataDTO(saved.getName(), saved.getDate())));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePlaceToVisitData(@PathVariable Long id) throws ForbiddenOperationException, PlaceToVisitDataNotFoundException {
        PlaceToVisitData memory = placeToVisitDataService.getPlaceToVisitDataById(id);

        String email = JwtAuthenticationUtil.getEmail();
        UserProduct product = memory.getUserProduct();
        if (product.getEmail() == null || !product.getEmail().equals(email)) {
            throw new ForbiddenOperationException("You are not allowed to delete this record");
        }

        placeToVisitDataService.deletePlaceToVisitData(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Place To Visit data deleted successfully", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<PlaceToVisitData> getAllPlaceToVisitData() {
        return placeToVisitDataService.getAllPlaceToVisitData();
    }
}
