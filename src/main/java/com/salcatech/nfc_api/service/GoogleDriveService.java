package com.salcatech.nfc_api.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.salcatech.nfc_api.dto.GoogleFileInfo;
import com.salcatech.nfc_api.util.JwtAuthenticationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GoogleDriveService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveService.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    @Value("${application.name}")
    private String applicationName;

    @Value("${google.drive.api.url}")
    private String driveApiUrl;

    @Value("${google.drive.api.key}")
    private String driveApiKey;


    private final RestTemplate restTemplate = new RestTemplate();


    /**
     * Google Drive API için Drive service oluşturur
     */
    private Drive getDriveService() throws IOException, GeneralSecurityException {
        logger.info("🔵 Google Drive service oluşturuluyor...");

        String accessToken = JwtAuthenticationUtil.getAccessToken();

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        Credential credential = new GoogleCredential()
                .setAccessToken(accessToken);

        return new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(this.applicationName)
                .build();
    }

    /**
     * Uygulama klasörünü oluşturur veya mevcut olanı bulur
     */
    public String getOrCreateApplicationFolder(String applicationFolderName) {
        try {
            logger.info("🔵 Uygulama klasörü aranıyor: {}", applicationFolderName);
            if (applicationFolderName == null || applicationFolderName.isEmpty()) {
                applicationFolderName = this.applicationName;
            }

            Drive driveService = getDriveService();

            FileList result = driveService.files().list()
                    .setQ("name='" + applicationFolderName + "' and mimeType='application/vnd.google-apps.folder' and trashed=false")
                    .setFields("files(id,name)")
                    .execute();

            List<File> folders = result.getFiles();

            if (!folders.isEmpty()) {
                String folderId = folders.get(0).getId();
                logger.info("🔵 Mevcut klasör bulundu: {} (ID: {})", applicationFolderName, folderId);
                return folderId;
            }

            logger.info("🔵 Yeni klasör oluşturuluyor: {}", applicationFolderName);

            File folderMetadata = new File();
            folderMetadata.setName(applicationFolderName);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");

            File folder = driveService.files().create(folderMetadata)
                    .setFields("id,name")
                    .execute();

            logger.info("🔵 Klasör başarıyla oluşturuldu: {} (ID: {})", folder.getName(), folder.getId());

            Permission permission = new Permission()
                    .setType("anyone")
                    .setRole("reader"); // sadece görüntüleme
            driveService.permissions().create(folder.getId(), permission)
                    .setFields("id")
                    .execute();
            logger.info("🔵 Klasör paylaşılabilir hale getirildi (herkese link ile erişim)");

            return folder.getId();

        } catch (Exception e) {
            logger.error("🔴 Klasör oluşturma/bulma hatası: ", e);
            throw new RuntimeException("Klasör işlemi hatası: " + e.getMessage());
        }
    }

    public String getOrCreateSubFolder(String parentFolderId, String subFolderName) {
        try {
            Drive driveService = getDriveService();

            FileList result = driveService.files().list()
                    .setQ("name='" + subFolderName + "' and mimeType='application/vnd.google-apps.folder' and trashed=false and '" + parentFolderId + "' in parents")
                    .setFields("files(id,name)")
                    .execute();

            List<File> folders = result.getFiles();

            if (!folders.isEmpty()) {
                String folderId = folders.get(0).getId();
                logger.info("🔵 SubFolder klasörü bulundu: {} (ID: {})", subFolderName, folderId);
                return folderId;
            }

            logger.info("🔵 Yeni SubFolder klasörü oluşturuluyor...");

            File folderMetadata = new File();
            folderMetadata.setName(subFolderName);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            folderMetadata.setParents(Collections.singletonList(parentFolderId));

            File folder = driveService.files().create(folderMetadata)
                    .setFields("id,name")
                    .execute();

            logger.info("🔵 SubFolder klasörü başarıyla oluşturuldu: {} (ID: {})", folder.getName(), folder.getId());
            return folder.getId();
        } catch (Exception e) {
            logger.error("🔴 SubFolder klasörü oluşturma hatası: {}", subFolderName, e);
            throw new RuntimeException("SubFolder klasörü oluşturma hatası: " + e.getMessage());
        }
    }


    /**
     * Dosya yükleme (Gerçek Google Drive API) - Uygulama klasörüne
     */
    public GoogleFileInfo uploadFile(String applicationFolderName, String subFolderName, java.io.File file, String fileId, String fileName, String mimeType) {
        try {
            logger.info("🔵 Google Drive'a dosya yükleniyor: {}", fileName);
            logger.info("🔵 Dosya boyutu: {} bytes", file.length());
            logger.info("🔵 MIME type: {}", mimeType);

            Drive driveService = getDriveService();

            String applicationFolderId = getOrCreateApplicationFolder(applicationFolderName);
            logger.info("🔵 Dosya klasöre yüklenecek: {}", applicationFolderId);

            File uploadedFile;

            if (fileId != null && !fileId.isEmpty() && fileId.length() != 1) {
                FileContent mediaContent = new FileContent(mimeType, file);
                File fileMetadata = new File();
                fileMetadata.setName(fileName);
                uploadedFile = driveService.files().update(fileId, fileMetadata, mediaContent)
                        .setFields("id,name,size,webViewLink,modifiedTime")
                        .execute();

                logger.info("🔵 Dosya güncellendi: {}", uploadedFile.getName());
            } else {

                File fileMetadata = new File();
                fileMetadata.setName(fileName);

                if (subFolderName != null && !subFolderName.isEmpty()) {
                    String subFolderId = getOrCreateSubFolder(applicationFolderId, subFolderName);
                    fileMetadata.setParents(Collections.singletonList(subFolderId));
                } else {
                    fileMetadata.setParents(Collections.singletonList(applicationFolderId));
                }

                FileContent mediaContent = new FileContent(mimeType, file);

                uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id,name,size,webViewLink,createdTime")
                        .execute();

                logger.info("🔵 Dosya başarıyla yüklendi: {}", uploadedFile.getName());
            }

            return new GoogleFileInfo(uploadedFile.getId(),
                    uploadedFile.getName(),
                    uploadedFile.getSize() != null ? uploadedFile.getSize() : 0,
                    uploadedFile.getWebViewLink() != null ? uploadedFile.getWebViewLink() : "",
                    uploadedFile.getCreatedTime() != null ? uploadedFile.getCreatedTime().toString() : uploadedFile.getModifiedTime() != null ? uploadedFile.getModifiedTime().toString() : "",
                    mimeType,
                    applicationFolderId);


        } catch (Exception e) {
            logger.error("🔴 Google Drive dosya yükleme hatası: ", e);
            throw new RuntimeException("Dosya yükleme hatası: " + e.getMessage());
        }
    }


    /**
     * Uygulama klasöründeki dosyaları listele (Gerçek Google Drive API)
     */
    public List<GoogleFileInfo> listFiles(String folderId) {
        try {
            String url = String.format(this.driveApiUrl, folderId, driveApiKey);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getBody() == null || response.getBody().get("files") == null) {
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> files = (List<Map<String, Object>>) response.getBody().get("files");

            if (files == null) {
                return Collections.emptyList();
            }

            return files.stream()
                    .map(file -> {
                        GoogleFileInfo googleFileInfo = new GoogleFileInfo();
                        googleFileInfo.setId(file.get("id").toString());
                        googleFileInfo.setName(file.get("name").toString());
                        googleFileInfo.setMimeType(file.get("mimeType").toString());
                        googleFileInfo.setWebViewLink(file.get("webViewLink").toString());
                        return googleFileInfo;
                    })
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("🔴 Google Drive klasör listeleme hatası: " + e.getMessage(), e);
        }


    }


    /**
     * Dosya silme (Gerçek Google Drive API)
     */
    public void deleteFile(String fileId) {
        try {
            logger.info("🔵 Google Drive'dan dosya siliniyor: {}", fileId);

            Drive driveService = getDriveService();
            driveService.files().delete(fileId).execute();

            logger.info("🔵 Dosya başarıyla silindi: {}", fileId);

        } catch (Exception e) {
            logger.error("🔴 Google Drive dosya silme hatası: ", e);
            throw new RuntimeException("Dosya silme hatası: " + e.getMessage());
        }
    }

}
