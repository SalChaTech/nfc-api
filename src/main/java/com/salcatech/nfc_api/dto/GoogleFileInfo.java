package com.salcatech.nfc_api.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GoogleFileInfo {
    String id;
    String name;
    Long size;
    String webViewLink;
    String time;
    String mimeType;
    String folderId;
}
