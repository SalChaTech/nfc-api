package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.service.GoogleDriveService;
import com.salcatech.nfc_api.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drive")
public class DriveController {

    private static final Logger logger = LoggerFactory.getLogger(DriveController.class);
    
    private final GoogleDriveService googleDriveService;
    private final JwtService jwtService;

    public DriveController(GoogleDriveService googleDriveService, JwtService jwtService) {
        this.googleDriveService = googleDriveService;
        this.jwtService = jwtService;
    }

    /**
     * Dosya yükleme endpoint'i
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileId", required = false) String fileId, // yeni
            @RequestParam(value = "toUploadSubfolder", defaultValue = "false") boolean toUploadSubfolder, // yeni
            HttpServletRequest request
    ) {
        logger.info("🔵 Dosya yükleme isteği alındı: {}", file.getOriginalFilename());

        try {
            // JWT token'ı al
            String jwt = getJwtFromRequest(request);
            if (jwt == null || !jwtService.validateToken(jwt)) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            // Geçici dosya oluştur
            File tempFile = File.createTempFile("upload_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile);

            logger.info("🔵 Geçici dosya oluşturuldu: {}", tempFile.getAbsolutePath());

            // JWT'den access token'ı çıkar
            String accessToken = jwtService.extractAccessToken(jwt);
            if (accessToken == null) {
                return ResponseEntity.status(401).body("Access token bulunamadı");
            }

            // Google Drive'a yükle
            Map<String, Object> result = googleDriveService.uploadFile(
                accessToken,
                tempFile,
                file.getOriginalFilename(),
                file.getContentType(),
                    fileId,toUploadSubfolder
            );

            // Geçici dosyayı sil
            tempFile.delete();

            logger.info("🔵 Dosya başarıyla yüklendi: {}", result);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("🔴 Dosya yükleme hatası: ", e);
            return ResponseEntity.status(500).body("Dosya yükleme hatası: " + e.getMessage());
        }
    }

    /**
     * Dosyaları listele endpoint'i
     */
    @GetMapping("/files")
    public ResponseEntity<?> listFiles(@RequestParam String folderId) {
        logger.info("🔵 Dosya listeleme isteği alındı");

        try {
            // Google Drive'dan dosyaları listele
            List<Map<String, Object>> files = googleDriveService.listFiles(folderId);

            logger.info("🔵 {} dosya bulundu", files.size());
            return ResponseEntity.ok(files);

        } catch (Exception e) {
            logger.error("🔴 Dosya listeleme hatası: ", e);
            return ResponseEntity.status(500).body("Dosya listeleme hatası: " + e.getMessage());
        }
    }

    /**
     * Dosya silme endpoint'i
     */
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable String fileId,
            HttpServletRequest request
    ) {
        logger.info("🔵 Dosya silme isteği alındı: {}", fileId);

        try {
            // JWT token'ı al
            String jwt = getJwtFromRequest(request);
            if (jwt == null || !jwtService.validateToken(jwt)) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            // JWT'den access token'ı çıkar
            String accessToken = jwtService.extractAccessToken(jwt);
            if (accessToken == null) {
                return ResponseEntity.status(401).body("Access token bulunamadı");
            }

            // Google Drive'dan dosyayı sil
            googleDriveService.deleteFile(accessToken, fileId);

            logger.info("🔵 Dosya başarıyla silindi: {}", fileId);
            return ResponseEntity.ok("Dosya başarıyla silindi");

        } catch (Exception e) {
            logger.error("🔴 Dosya silme hatası: ", e);
            return ResponseEntity.status(500).body("Dosya silme hatası: " + e.getMessage());
        }
    }


    /**
     * Request'ten JWT token'ı al
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> "jwt".equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        return null;
    }
}
