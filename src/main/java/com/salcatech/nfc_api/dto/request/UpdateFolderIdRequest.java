package com.salcatech.nfc_api.dto.request;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFolderIdRequest {
    @NotBlank(message = "folderId cannot be null or empty")
    private String folderId;
}
