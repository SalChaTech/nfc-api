package com.salcatech.nfc_api.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PlaceToVisitDataRequest {
    private String name;
    private LocalDate date;
}