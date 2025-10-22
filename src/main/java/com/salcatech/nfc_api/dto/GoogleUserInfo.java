package com.salcatech.nfc_api.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GoogleUserInfo {
    String id;
    String email;
    String name;
    String role;
    String accessToken;
}
