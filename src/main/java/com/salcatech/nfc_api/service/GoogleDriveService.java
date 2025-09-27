package com.salcatech.nfc_api.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GoogleDriveService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveService.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    private final GoogleAuthService googleAuthService;

    public GoogleDriveService(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    /**
     * Google Drive API için Drive service oluşturur
     */
    private Drive getDriveService(String accessToken) throws IOException, GeneralSecurityException {
        logger.info("🔵 Google Drive service oluşturuluyor...");

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        Credential credential = new GoogleCredential()
                .setAccessToken(accessToken);

        return new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("NFC API")
                .build();
    }

    /**
     * Dosya yükleme (Gerçek Google Drive API)
     */
    public Map<String, Object> uploadFile(String accessToken, java.io.File file, String fileName, String mimeType) {
        try {
            logger.info("🔵 Google Drive'a dosya yükleniyor: {}", fileName);
            logger.info("🔵 Dosya boyutu: {} bytes", file.length());
            logger.info("🔵 MIME type: {}", mimeType);

            Drive driveService = getDriveService(accessToken);

            File fileMetadata = new File();
            fileMetadata.setName(fileName);

            FileContent mediaContent = new FileContent(mimeType, file);

            File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id,name,size,webViewLink,createdTime")
                    .execute();

            logger.info("🔵 Dosya başarıyla yüklendi: {}", uploadedFile.getName());
            logger.info("🔵 Dosya ID: {}", uploadedFile.getId());
            logger.info("🔵 Dosya boyutu: {} bytes", uploadedFile.getSize());
            logger.info("🔵 Dosya linki: {}", uploadedFile.getWebViewLink());

            return Map.of(
                "id", uploadedFile.getId(),
                "name", uploadedFile.getName(),
                "size", uploadedFile.getSize() != null ? uploadedFile.getSize() : 0,
                "webViewLink", uploadedFile.getWebViewLink() != null ? uploadedFile.getWebViewLink() : "",
                "createdTime", uploadedFile.getCreatedTime() != null ? uploadedFile.getCreatedTime().toString() : "",
                "mimeType", mimeType
            );

        } catch (Exception e) {
            logger.error("🔴 Google Drive dosya yükleme hatası: ", e);
            throw new RuntimeException("Dosya yükleme hatası: " + e.getMessage());
        }
    }

    /**
     * Kullanıcının dosyalarını listele (Gerçek Google Drive API)
     */
    public List<Map<String, Object>> listFiles(String accessToken) {
        try {
            logger.info("🔵 Google Drive dosyaları listeleniyor...");

            Drive driveService = getDriveService(accessToken);

            FileList result = driveService.files().list()
                    .setPageSize(20)
                    .setFields("nextPageToken, files(id, name, size, webViewLink, createdTime, mimeType)")
                    .execute();

            List<File> files = result.getFiles();
            logger.info("🔵 {} dosya bulundu", files.size());

            return files.stream()
                    .map(file -> {
                        Map<String, Object> fileMap = new java.util.HashMap<>();
                        fileMap.put("id", file.getId());
                        fileMap.put("name", file.getName());
                        fileMap.put("size", file.getSize() != null ? file.getSize() : 0);
                        fileMap.put("webViewLink", file.getWebViewLink() != null ? file.getWebViewLink() : "");
                        fileMap.put("createdTime", file.getCreatedTime() != null ? file.getCreatedTime().toString() : "");
                        fileMap.put("mimeType", file.getMimeType() != null ? file.getMimeType() : "");
                        return fileMap;
                    })
                    .toList();

        } catch (Exception e) {
            logger.error("🔴 Google Drive dosya listeleme hatası: ", e);
            throw new RuntimeException("Dosya listeleme hatası: " + e.getMessage());
        }
    }

    /**
     * Dosya silme (Gerçek Google Drive API)
     */
    public void deleteFile(String accessToken, String fileId) {
        try {
            logger.info("🔵 Google Drive'dan dosya siliniyor: {}", fileId);

            Drive driveService = getDriveService(accessToken);
            driveService.files().delete(fileId).execute();

            logger.info("🔵 Dosya başarıyla silindi: {}", fileId);

        } catch (Exception e) {
            logger.error("🔴 Google Drive dosya silme hatası: ", e);
            throw new RuntimeException("Dosya silme hatası: " + e.getMessage());
        }
    }
}
