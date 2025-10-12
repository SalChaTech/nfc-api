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
import com.google.api.services.drive.model.Permission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleDriveService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveService.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String APP_FOLDER_NAME = "Tag4Ever";
    String DRIVE_API_URL =
            "https://www.googleapis.com/drive/v3/files?q='%s'+in+parents+and+trashed=false&fields=files(id,name,webViewLink,mimeType)&key=%s";
    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.drive.api.key}")
    private String driveApiKey;

    private final GoogleAuthService googleAuthService;

    private final RestTemplate restTemplate = new RestTemplate();

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
     * Uygulama klasörünü oluşturur veya mevcut olanı bulur
     */
    public String getOrCreateAppFolder(String accessToken) {
        try {
            logger.info("🔵 Uygulama klasörü aranıyor: {}", APP_FOLDER_NAME);

            Drive driveService = getDriveService(accessToken);

            // Önce mevcut klasörü ara
            FileList result = driveService.files().list()
                    .setQ("name='" + APP_FOLDER_NAME + "' and mimeType='application/vnd.google-apps.folder' and trashed=false")
                    .setFields("files(id,name)")
                    .execute();

            List<File> folders = result.getFiles();

            if (!folders.isEmpty()) {
                String folderId = folders.get(0).getId();
                logger.info("🔵 Mevcut klasör bulundu: {} (ID: {})", APP_FOLDER_NAME, folderId);


                return folderId;
            }

            // Klasör yoksa oluştur
            logger.info("🔵 Yeni klasör oluşturuluyor: {}", APP_FOLDER_NAME);

            File folderMetadata = new File();
            folderMetadata.setName(APP_FOLDER_NAME);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");

            File folder = driveService.files().create(folderMetadata)
                    .setFields("id,name")
                    .execute();

            logger.info("🔵 Klasör başarıyla oluşturuldu: {} (ID: {})", folder.getName(), folder.getId());

            // 🔹 Klasörü paylaşılabilir yap
            Permission permission = new Permission()
                    .setType("anyone")
                    .setRole("reader"); // sadece görüntüleme
            driveService.permissions().create(folder.getId(), permission)
                    .setFields("id")
                    .execute();
            logger.info("🔵 Klasör paylaşılabilir hale getirildi (herkese link ile erişim)");

            // Alt klasör (Gallery) oluşturulacaksa
//            if (toUploadSubfolder) {
//                return getOrCreateGalleryFolder(driveService, folder.getId());
//            }

            return folder.getId();

        } catch (Exception e) {
            logger.error("🔴 Klasör oluşturma/bulma hatası: ", e);
            throw new RuntimeException("Klasör işlemi hatası: " + e.getMessage());
        }
    }

    private String getOrCreateGalleryFolder(Drive driveService, String parentFolderId) {
        try {
            // Gallery klasörünü arıyoruz
            FileList result = driveService.files().list()
                    .setQ("name='Gallery' and mimeType='application/vnd.google-apps.folder' and trashed=false and '" + parentFolderId + "' in parents")
                    .setFields("files(id,name)")
                    .execute();

            List<File> folders = result.getFiles();

            if (!folders.isEmpty()) {
                String folderId = folders.get(0).getId();
                logger.info("🔵 Gallery klasörü bulundu: {} (ID: {})", "Gallery", folderId);
                return folderId;
            }

            // Klasör yoksa oluştur
            logger.info("🔵 Yeni Gallery klasörü oluşturuluyor...");

            File folderMetadata = new File();
            folderMetadata.setName("Gallery");
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            folderMetadata.setParents(Collections.singletonList(parentFolderId));

            File folder = driveService.files().create(folderMetadata)
                    .setFields("id,name")
                    .execute();

            logger.info("🔵 Gallery klasörü başarıyla oluşturuldu: {} (ID: {})", folder.getName(), folder.getId());
            return folder.getId();
        } catch (Exception e) {
            logger.error("🔴 Gallery klasörü oluşturma hatası: ", e);
            throw new RuntimeException("Gallery klasörü oluşturma hatası: " + e.getMessage());
        }
    }


    /**
     * Dosya yükleme (Gerçek Google Drive API) - Uygulama klasörüne
     */
    public Map<String, Object> uploadFile(String accessToken, java.io.File file, String fileName, String mimeType, String fileId, boolean toUploadSubfolder) {
        try {
            logger.info("🔵 Google Drive'a dosya yükleniyor: {}", fileName);
            logger.info("🔵 Dosya boyutu: {} bytes", file.length());
            logger.info("🔵 MIME type: {}", mimeType);

            Drive driveService = getDriveService(accessToken);

            // Uygulama klasörünü al veya oluştur
            String applicationFolderId = getOrCreateAppFolder(accessToken);
            String galleryFolderId = getOrCreateGalleryFolder(driveService, applicationFolderId);
            logger.info("🔵 Dosya klasöre yüklenecek: {}", applicationFolderId);

            File uploadedFile;

            if (fileId != null && !fileId.isEmpty() && fileId.length() != 1) {
                // --- Var olan dosyayı güncelle (overwrite) ---
                FileContent mediaContent = new FileContent(mimeType, file);
                File fileMetadata = new File();
                fileMetadata.setName(fileName);
                // Update sırasında parent göndermiyoruz
                uploadedFile = driveService.files().update(fileId, fileMetadata, mediaContent)
                        .setFields("id,name,size,webViewLink,modifiedTime")
                        .execute();

                logger.info("🔵 Dosya güncellendi: {}", uploadedFile.getName());
            } else {
                // --- Yeni dosya oluştur ---
                File fileMetadata = new File();
                fileMetadata.setName(fileName);
                if (toUploadSubfolder) {
                    fileMetadata.setParents(Collections.singletonList(galleryFolderId)); // sadece create'de kullan

                } else {

                    fileMetadata.setParents(Collections.singletonList(applicationFolderId)); // sadece create'de kullan
                }

                FileContent mediaContent = new FileContent(mimeType, file);

                uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id,name,size,webViewLink,createdTime")
                        .execute();

                logger.info("🔵 Dosya başarıyla yüklendi: {}", uploadedFile.getName());
            }

            return Map.of(
                    "id", uploadedFile.getId(),
                    "name", uploadedFile.getName(),
                    "size", uploadedFile.getSize() != null ? uploadedFile.getSize() : 0,
                    "webViewLink", uploadedFile.getWebViewLink() != null ? uploadedFile.getWebViewLink() : "",
                    "time", uploadedFile.getCreatedTime() != null ? uploadedFile.getCreatedTime().toString() :
                            uploadedFile.getModifiedTime() != null ? uploadedFile.getModifiedTime().toString() : "",
                    "mimeType", mimeType,
                    "folderId", applicationFolderId
            );

        } catch (Exception e) {
            logger.error("🔴 Google Drive dosya yükleme hatası: ", e);
            throw new RuntimeException("Dosya yükleme hatası: " + e.getMessage());
        }
    }


    /**
     * Uygulama klasöründeki dosyaları listele (Gerçek Google Drive API)
     */
    public List<Map<String, Object>> listFiles(String folderId) {
        try {
            String url = String.format(DRIVE_API_URL, folderId, driveApiKey);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            List<Map<String, Object>> files = (List<Map<String, Object>>) response.getBody().get("files");

            if (files == null) {
                return Collections.emptyList();
            }

            return files.stream()
                    .map(file -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", file.get("id"));
                        map.put("name", file.get("name"));
                        map.put("mimeType", file.get("mimeType"));
                        map.put("webViewLink", file.get("webViewLink"));
                        map.put("previewUrl", "https://drive.google.com/uc?export=view&id=" + file.get("id"));
                        return map;
                    })
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("🔴 Google Drive klasör listeleme hatası: " + e.getMessage(), e);
        }

        //        try {
//            logger.info("🔵 Uygulama klasöründeki dosyalar listeleniyor...");
//
//            Drive driveService = getDriveService(accessToken);
//
//            // Uygulama klasörünü al
//            String applicationFolderId =
//                    getOrCreateAppFolder(accessToken);
//
//            String galleryFolderId = getOrCreateGalleryFolder(driveService, applicationFolderId);
//
//            logger.info("🔵 Klasör ID: {}", applicationFolderId);
//
//            String folderId = toUploadSubfolder ? galleryFolderId : applicationFolderId;
//
//            // Sadece bu klasördeki dosyaları listele
//            FileList result = driveService.files().list()
//                    .setQ("'" + folderId + "' in parents and trashed=false")
//                    .setPageSize(20)
//                    .setFields("nextPageToken, files(id, name, size, webViewLink, createdTime, mimeType)")
//                    .execute();
//
//            List<File> files = result.getFiles();
//            logger.info("🔵 {} dosya bulundu", files.size());
//
//            return files.stream()
//                    .map(file -> {
//                        Map<String, Object> fileMap = new java.util.HashMap<>();
//                        fileMap.put("id", file.getId());
//                        fileMap.put("name", file.getName());
//                        fileMap.put("size", file.getSize() != null ? file.getSize() : 0);
//                        fileMap.put("webViewLink", file.getWebViewLink() != null ? file.getWebViewLink() : "");
//                        fileMap.put("createdTime", file.getCreatedTime() != null ? file.getCreatedTime().toString() : "");
//                        fileMap.put("mimeType", file.getMimeType() != null ? file.getMimeType() : "");
//                        return fileMap;
//                    })
//                    .toList();
//
//        } catch (Exception e) {
//            logger.error("🔴 Google Drive dosya listeleme hatası: ", e);
//            throw new RuntimeException("Dosya listeleme hatası: " + e.getMessage());
//        }
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
