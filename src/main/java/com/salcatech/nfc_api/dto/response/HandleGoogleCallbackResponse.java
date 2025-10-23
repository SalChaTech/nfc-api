package com.salcatech.nfc_api.dto.response;

import com.salcatech.nfc_api.dto.GoogleUserInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HandleGoogleCallbackResponse {
    public String jwt;
    public GoogleUserInfoDTO googleUserInfo;

}
