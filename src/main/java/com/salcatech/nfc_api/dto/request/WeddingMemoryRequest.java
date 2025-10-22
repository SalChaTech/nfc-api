package com.salcatech.nfc_api.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WeddingMemoryRequest {
    private String maleName;
    private String femaleName;
    private LocalDate date;
}