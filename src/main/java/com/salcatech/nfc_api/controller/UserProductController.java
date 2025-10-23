package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.dto.ApiResponse;
import com.salcatech.nfc_api.dto.request.UpdateFolderIdRequest;
import com.salcatech.nfc_api.exception.UserProductNotClaimedException;
import com.salcatech.nfc_api.exception.UserProductNotFoundException;
import com.salcatech.nfc_api.exception.UserProductOwnershipException;
import com.salcatech.nfc_api.model.UserProduct;
import com.salcatech.nfc_api.service.UserProductService;
import com.salcatech.nfc_api.util.JwtAuthenticationUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public UserProduct getById(@PathVariable String id) throws UserProductNotFoundException {
        return userProductService.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UserProduct create(@RequestBody UserProduct userProduct) {
        return userProductService.save(userProduct);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/claim")
    public ResponseEntity<ApiResponse<String>> claimProduct(
            @PathVariable String id
    ) throws UserProductNotFoundException {

        String email = JwtAuthenticationUtil.getEmail();

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

    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/update-folder-id")
    public ResponseEntity<ApiResponse<String>> updateDriveApplicationFolderId(
            @PathVariable String id,
            @Valid @RequestBody UpdateFolderIdRequest request
    ) throws UserProductNotFoundException, UserProductNotClaimedException, UserProductOwnershipException {
        String email = JwtAuthenticationUtil.getEmail();

        String newFolderId = request.getFolderId();

        UserProduct product = userProductService.getById(id);

        if (product.getEmail() == null) {
            throw new UserProductNotClaimedException();
        }

        if (!product.getEmail().equals(email)) {
            throw new UserProductOwnershipException();
        }

        product.setDriveApplicationFolderId(newFolderId);
        userProductService.save(product);

        return ResponseEntity.ok(new ApiResponse<>(true, "Folder ID updated successfully", newFolderId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        userProductService.delete(id);
    }
}
