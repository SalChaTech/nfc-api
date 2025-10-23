package com.salcatech.nfc_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GoogleUserInfoDTO {
    String email;
    String name;
    String role;
    String accessToken;
}
