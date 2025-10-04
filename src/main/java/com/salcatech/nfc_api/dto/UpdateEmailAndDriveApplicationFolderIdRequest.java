package com.salcatech.nfc_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UpdateEmailAndDriveApplicationFolderIdRequest {
    private String email;
    private String driveApplicationFolderId;
}
