package com.salcatech.nfc_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceToVisitDataDTO {
    private String name;
    private LocalDate date;
}
