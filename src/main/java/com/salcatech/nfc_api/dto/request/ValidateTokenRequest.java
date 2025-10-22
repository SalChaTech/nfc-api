package com.salcatech.nfc_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ValidateTokenRequest {
    private String token;

}
