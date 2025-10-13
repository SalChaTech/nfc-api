package com.salcatech.nfc_api.controller;

import com.google.api.client.util.IOUtils;
import com.salcatech.nfc_api.service.GoogleDriveService;
import com.salcatech.nfc_api.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

        long sizeInMB = file.getSize() / (1024 * 1024);
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            if (sizeInMB > 20) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(Map.of("message", "Fotoğraf boyutu en fazla 20MB olabilir."));
            }
        } else if (contentType != null && contentType.startsWith("video/")) {
            if (sizeInMB > 300) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(Map.of("message", "Video boyutu en fazla 300MB olabilir."));
            }
        }

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
                    fileId, toUploadSubfolder
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


    @DeleteMapping("/files/by-name/{fileName}")
    public ResponseEntity<?> deleteFileByName(
            @PathVariable String fileName,
            HttpServletRequest request
    ) {
        logger.info("🔵 Dosya silme isteği alındı: {}", fileName);

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

            String applicationFolderId = googleDriveService.getOrCreateAppFolder(accessToken);

            List<Map<String, Object>> files = googleDriveService.listFiles(applicationFolderId);

            // 2️⃣ Eşleşen isimdeki dosyaları bul
            List<Map<String, Object>> matchedFiles = files.stream()
                    .filter(file -> fileName.equals(file.get("name")))
                    .toList();

            if (matchedFiles.isEmpty()) {
                logger.warn("⚠️ '{}' isminde dosya bulunamadı", fileName);
                return ResponseEntity.status(404).body("Dosya bulunamadı: " + fileName);

            }

            // 3️⃣ Eşleşen dosyaları sil
            for (Map<String, Object> file : matchedFiles) {
                String fileId = (String) file.get("id");
                googleDriveService.deleteFile(accessToken, fileId);
                logger.info("🗑️ Silindi: {} (ID: {})", file.get("name"), fileId);
            }

            logger.info("✅ '{}' isminde tüm dosyalar silindi", fileName);

            return ResponseEntity.ok("Dosya(lar) başarıyla silindi: " + fileName);

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


    @GetMapping("/video/{id}")
    public void streamVideo(@PathVariable String id, HttpServletResponse response) {
        try {
            String fileUrl = "https://drive.google.com/uc?export=download&id=" + id;

            java.net.URL url = new java.net.URL(fileUrl);
            java.net.URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            InputStream inputStream = connection.getInputStream();

            response.setContentType("video/mp4");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Accept-Ranges", "bytes");

            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();

            logger.info("✅ Video public olarak stream edildi: {}", id);

        } catch (Exception e) {
            logger.error("🔴 Video stream hatası: ", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Video stream hatası: " + e.getMessage());
            } catch (IOException ignored) {
            }
        }
    }
}
