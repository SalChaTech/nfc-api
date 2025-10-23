package com.salcatech.nfc_api.controller;

import com.google.api.client.util.IOUtils;
import com.salcatech.nfc_api.dto.ApiResponse;
import com.salcatech.nfc_api.dto.GoogleFileInfoDTO;
import com.salcatech.nfc_api.exception.*;
import com.salcatech.nfc_api.service.GoogleDriveService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<GoogleFileInfoDTO>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "applicationFolderName", required = false) String applicationFolderName,
            @RequestParam(value = "subFolderName", required = false) String subFolderName,
            @RequestParam(value = "fileId", required = false) String fileId
    ) throws FileSizeExceedException, IOException, UploadFileException {
        logger.info("🔵 Dosya yükleme isteği alındı: {}", file.getOriginalFilename());

        long sizeInMB = file.getSize() / (1024 * 1024);
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            if (sizeInMB > 20) {
                throw new FileSizeExceedException("Fotoğraf boyutu en fazla 20MB olabilir.");
            }
        } else if (contentType != null && contentType.startsWith("video/")) {
            if (sizeInMB > 300) {
                throw new FileSizeExceedException("Video boyutu en fazla 300MB olabilir.");
            }
        }


        File tempFile = File.createTempFile("upload_", "_" + file.getOriginalFilename());
        file.transferTo(tempFile);

        logger.info("🔵 Geçici dosya oluşturuldu: {}", tempFile.getAbsolutePath());


        GoogleFileInfoDTO fileInfo = googleDriveService.uploadFile(
                applicationFolderName,
                subFolderName,
                tempFile,
                fileId,
                file.getOriginalFilename(),
                file.getContentType());

        tempFile.delete();

        ApiResponse<GoogleFileInfoDTO> apiResponse = new ApiResponse<>(true, "Succeed!", fileInfo);


        logger.info("🔵 Dosya başarıyla yüklendi: {}", fileInfo);
        return ResponseEntity.ok(apiResponse);

    }

    /**
     * Dosyaları listele endpoint'i
     */
    @GetMapping("/list/{folderId}")
    public ResponseEntity<ApiResponse<List<GoogleFileInfoDTO>>> listFiles(@PathVariable String folderId) throws ListFilesException {
        logger.info("🔵 Dosya listeleme isteği alındı");

        List<GoogleFileInfoDTO> files = googleDriveService.listFiles(folderId);

        logger.info("🔵 {} dosya bulundu", files.size());

        ApiResponse<List<GoogleFileInfoDTO>> apiResponse = new ApiResponse<>(true, "Succeed!", files);

        return ResponseEntity.ok(apiResponse);


    }

    /**
     * Dosya silme endpoint'i
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<String>> deleteFile(
            @PathVariable String fileId
    ) throws ListFilesException {
        logger.info("🔵 Dosya silme isteği alındı: {}", fileId);


        googleDriveService.deleteFile(fileId);

        logger.info("🔵 Dosya başarıyla silindi: {}", fileId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Succeed!", null));

    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/by-name/{fileName}")
    public ResponseEntity<ApiResponse<String>> deleteFileByName(
            @RequestParam(value = "applicationFolderName", required = false) String applicationFolderName,
            @RequestParam(value = "subFolderName", required = false) String subFolderName,
            @PathVariable String fileName
    ) throws FolderNotGetOrCreateException, ListFilesException, NotMatchedFilesException {
        logger.info("🔵 Dosya silme isteği alındı: {}", fileName);


        String applicationFolderId = googleDriveService.getOrCreateApplicationFolder(applicationFolderName);

        String folderId = applicationFolderId;
        if (subFolderName != null && !subFolderName.isBlank()) {
            folderId = googleDriveService.getOrCreateSubFolder(applicationFolderId, subFolderName);
        }

        List<GoogleFileInfoDTO> files = googleDriveService.listFiles(folderId);

        List<GoogleFileInfoDTO> matchedFiles = files.stream()
                .filter(file -> fileName.equals(file.getName()))
                .toList();

        if (matchedFiles.isEmpty()) {
            logger.warn("⚠️ '{}' isminde dosya bulunamadı", fileName);
            throw new NotMatchedFilesException();

        }

        for (GoogleFileInfoDTO file : matchedFiles) {
            String fileId = (String) file.getId();
            googleDriveService.deleteFile(fileId);
            logger.info("🗑️ Silindi: {} (ID: {})", file.getName(), fileId);
        }

        logger.info("✅ '{}' isminde tüm dosyalar silindi", fileName);

        return ResponseEntity.ok(new ApiResponse<>(true, "Succeed!", null));

    }

    @GetMapping("/stream-video/{id}")
    public void streamVideo(@PathVariable String id, HttpServletResponse response) throws IOException {
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

    }
}
