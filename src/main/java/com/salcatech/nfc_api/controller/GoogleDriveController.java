package com.salcatech.nfc_api.controller;

import com.google.api.client.util.IOUtils;
import com.salcatech.nfc_api.dto.ApiResponse;
import com.salcatech.nfc_api.dto.GoogleFileInfo;
import com.salcatech.nfc_api.service.GoogleDriveService;
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
import java.util.List;

@RestController
@RequestMapping("/api/drive")
public class GoogleDriveController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveController.class);

    private final GoogleDriveService googleDriveService;

    public GoogleDriveController(GoogleDriveService googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    /**
     * Dosya yükleme endpoint'i
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<GoogleFileInfo>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "applicationFolderName", required = false) String applicationFolderName,
            @RequestParam(value = "subFolderName", required = false) String subFolderName,
            @RequestParam(value = "fileId", required = false) String fileId
    ) {
        logger.info("🔵 Dosya yükleme isteği alındı: {}", file.getOriginalFilename());

        long sizeInMB = file.getSize() / (1024 * 1024);
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            if (sizeInMB > 20) {
                ApiResponse<GoogleFileInfo> apiResponse = new ApiResponse<>(false, "Fotoğraf boyutu en fazla 20MB olabilir.", null);

                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(apiResponse);
            }
        } else if (contentType != null && contentType.startsWith("video/")) {
            if (sizeInMB > 300) {
                ApiResponse<GoogleFileInfo> apiResponse = new ApiResponse<>(false, "Video boyutu en fazla 300MB olabilir.", null);

                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(apiResponse);
            }
        }

        try {

            File tempFile = File.createTempFile("upload_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile);

            logger.info("🔵 Geçici dosya oluşturuldu: {}", tempFile.getAbsolutePath());


            GoogleFileInfo fileInfo = googleDriveService.uploadFile(
                    applicationFolderName,
                    subFolderName,
                    tempFile,
                    fileId,
                    file.getOriginalFilename(),
                    file.getContentType());

            tempFile.delete();

            ApiResponse<GoogleFileInfo> apiResponse = new ApiResponse<>(true, "Succeed!", fileInfo);


            logger.info("🔵 Dosya başarıyla yüklendi: {}", fileInfo);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("🔴 Dosya yükleme hatası: ", e);
            ApiResponse<GoogleFileInfo> apiResponse = new ApiResponse<>(false, "Dosya yükleme hatası: " + e.getMessage(), null);

            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    /**
     * Dosyaları listele endpoint'i
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<GoogleFileInfo>>> listFiles(@RequestParam String folderId) {
        logger.info("🔵 Dosya listeleme isteği alındı");

        try {
            List<GoogleFileInfo> files = googleDriveService.listFiles(folderId);

            logger.info("🔵 {} dosya bulundu", files.size());

            ApiResponse<List<GoogleFileInfo>> apiResponse = new ApiResponse<>(true, "Succeed!", files);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("🔴 Dosya listeleme hatası: ", e);
            ApiResponse<List<GoogleFileInfo>> apiResponse = new ApiResponse<>(false, "Dosya yükleme hatası: " + e.getMessage(), null);

            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    /**
     * Dosya silme endpoint'i
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<String>> deleteFile(
            @PathVariable String fileId
    ) {
        logger.info("🔵 Dosya silme isteği alındı: {}", fileId);

        try {


            googleDriveService.deleteFile(fileId);

            logger.info("🔵 Dosya başarıyla silindi: {}", fileId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Succeed!", null));

        } catch (Exception e) {
            logger.error("🔴 Dosya silme hatası: ", e);
            ApiResponse<String> apiResponse = new ApiResponse<>(false, "Dosya silme hatası: " + e.getMessage(), null);

            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    @DeleteMapping("/by-name/{fileName}")
    public ResponseEntity<ApiResponse<String>> deleteFileByName(
            @RequestParam(value = "applicationFolderName", required = false) String applicationFolderName,
            @RequestParam(value = "subFolderName", required = false) String subFolderName,
            @PathVariable String fileName
    ) {
        logger.info("🔵 Dosya silme isteği alındı: {}", fileName);

        try {


            String applicationFolderId = googleDriveService.getOrCreateApplicationFolder(applicationFolderName);

            String folderId = applicationFolderId;
            if (subFolderName != null && !subFolderName.isBlank()) {
                folderId = googleDriveService.getOrCreateSubFolder(applicationFolderId, subFolderName);
            }

            List<GoogleFileInfo> files = googleDriveService.listFiles(folderId);

            List<GoogleFileInfo> matchedFiles = files.stream()
                    .filter(file -> fileName.equals(file.getName()))
                    .toList();

            if (matchedFiles.isEmpty()) {
                logger.warn("⚠️ '{}' isminde dosya bulunamadı", fileName);
                ApiResponse<String> apiResponse = new ApiResponse<>(false, "Dosya bulunamadı hatası: " + fileName, null);

                return ResponseEntity.status(404).body(apiResponse);

            }

            for (GoogleFileInfo file : matchedFiles) {
                String fileId = (String) file.getId();
                googleDriveService.deleteFile(fileId);
                logger.info("🗑️ Silindi: {} (ID: {})", file.getName(), fileId);
            }

            logger.info("✅ '{}' isminde tüm dosyalar silindi", fileName);

            return ResponseEntity.ok(new ApiResponse<>(true, "Succeed!", null));

        } catch (Exception e) {
            logger.error("🔴 Dosya silme hatası: ", e);
            ApiResponse<String> apiResponse = new ApiResponse<>(false, "Dosya silme hatası: " + e.getMessage(), null);

            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    @GetMapping("/stream-video/{id}")
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
