package com.salcatech.nfc_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeddingMemoryDataDTO {
    private String maleName;
    private String femaleName;
    private LocalDate date;
}
