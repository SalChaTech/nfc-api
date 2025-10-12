package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.model.Product;
import com.salcatech.nfc_api.model.UserProduct;
import com.salcatech.nfc_api.service.ProductService;
import com.salcatech.nfc_api.service.UserProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    // 📌 1️⃣ İlk sahiplenme (sadece email set edilir)
    @PutMapping("/{id}/claim")
    public ResponseEntity<?> claimProduct(
            @PathVariable String id,
            Authentication auth
    ) {

        String email = auth.getName();

        try {
            UserProduct product = userProductService.getById(id);
            String driveApplicationFolderId = product.getDriveApplicationFolderId() == null ? "" : product.getDriveApplicationFolderId();

            // 1. Eğer ürün boşsa → sahiplen
            if (product.getEmail() == null || product.getEmail().isEmpty()) {
                product.setEmail(email);
                userProductService.save(product);

                return ResponseEntity.ok(Map.of(
                        "message", "Product successfully claimed",
                        "status", "claimed",
                        "folder_id", driveApplicationFolderId
                ));
            }

            // 2. Eğer zaten aynı kullanıcı sahiplenmişse
            if (product.getEmail().equals(email)) {

                return ResponseEntity.ok(Map.of(
                        "message", "Product already claimed by you",
                        "status", "owner",
                        "folder_id", driveApplicationFolderId
                ));
            }

            // 3. Başkası sahiplenmişse
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "Product already claimed by another user",
                    "status", "forbidden"
            ));
        } catch (NoSuchElementException e) {
            // Eğer userProduct bulunamazsa 404 döner
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("UserProduct bulunamadı: id = " + id);
        } catch (Exception e) {
            // Beklenmeyen hata
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Sunucu hatası: " + e.getMessage());
        }


    }


    // 📌 2️⃣ Folder ID güncelleme (sadece sahibi yapabilir)
    @PutMapping("/{id}/update-folder-id")
    public ResponseEntity<?> updateFolderId(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            Authentication auth
    ) {
        String email = auth.getName();
        String newFolderId = body.get("folderId");

        if (newFolderId == null || newFolderId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "folderId is required"
            ));
        }

        UserProduct product = userProductService.getById(id);

        // Sahiplik kontrolü
        if (product.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Product has not been claimed yet"
            ));
        }

        if (!product.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "You are not allowed to update this product"
            ));
        }

        product.setDriveApplicationFolderId(newFolderId);
        userProductService.save(product);

        return ResponseEntity.ok(Map.of(
                "message", "Folder ID updated successfully",
                "status", "updated"
        ));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        userProductService.delete(id);
    }
}
