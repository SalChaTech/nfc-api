package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.dto.ApiResponse;
import com.salcatech.nfc_api.dto.request.UpdateFolderIdRequest;
import com.salcatech.nfc_api.model.UserProduct;
import com.salcatech.nfc_api.service.UserProductService;
import com.salcatech.nfc_api.util.JwtAuthenticationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/user-product")
public class UserProductController {
    private static final Logger logger = LoggerFactory.getLogger(UserProductController.class);

    private final UserProductService userProductService;

    public UserProductController(UserProductService userProductService) {
        this.userProductService = userProductService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserProduct> getAll() {
        return userProductService.getAll();
    }

    @GetMapping("/{id}")
    public UserProduct getById(@PathVariable String id) {
        return userProductService.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UserProduct create(@RequestBody UserProduct userProduct) {
        return userProductService.save(userProduct);
    }

    @PutMapping("/{id}/claim")
    public ResponseEntity<ApiResponse<String>> claimProduct(
            @PathVariable String id
    ) {

        String email = JwtAuthenticationUtil.getEmail();

        try {
            UserProduct product = userProductService.getById(id);
            String driveApplicationFolderId = product.getDriveApplicationFolderId() == null ? "" : product.getDriveApplicationFolderId();

            if (product.getEmail() == null || product.getEmail().isEmpty()) {
                product.setEmail(email);
                userProductService.save(product);
                ApiResponse<String> apiResponse = new ApiResponse<>(true, "Product successfully claimed", driveApplicationFolderId);
                return ResponseEntity.ok(apiResponse);
            }

            if (product.getEmail().equals(email)) {
                ApiResponse<String> apiResponse = new ApiResponse<>(true, "Product already claimed by you", driveApplicationFolderId);
                return ResponseEntity.ok(apiResponse);
            }

            ApiResponse<String> apiResponse = new ApiResponse<>(false, "Product already claimed by another user", null);

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
        } catch (NoSuchElementException e) {
            ApiResponse<String> apiResponse = new ApiResponse<>(false, "UserProduct bulunamadı: id = " + id, null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(apiResponse);
        } catch (Exception e) {
            ApiResponse<String> apiResponse = new ApiResponse<>(false, "Sunucu hatası: " + e.getMessage(), null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(apiResponse);
        }


    }


    @PutMapping("/{id}/update-folder-id")
    public ResponseEntity<ApiResponse<String>> updateFolderId(
            @PathVariable String id,
            @RequestBody UpdateFolderIdRequest request
    ) {
        String email = JwtAuthenticationUtil.getEmail();
        String newFolderId = request.getFolderId();

        if (newFolderId == null || newFolderId.isEmpty()) {
            ApiResponse<String> apiResponse = new ApiResponse<>(false, "folderId is required", null);

            return ResponseEntity.badRequest().body(apiResponse);
        }

        UserProduct product = userProductService.getById(id);

        if (product.getEmail() == null) {
            ApiResponse<String> apiResponse = new ApiResponse<>(false, "Product has not been claimed yet", null);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }

        if (!product.getEmail().equals(email)) {
            ApiResponse<String> apiResponse = new ApiResponse<>(false, "You are not allowed to update this product", null);

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
        }

        product.setDriveApplicationFolderId(newFolderId);
        userProductService.save(product);

        return ResponseEntity.ok(new ApiResponse<>(true, "Folder ID updated successfully", newFolderId));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        userProductService.delete(id);
    }
}
