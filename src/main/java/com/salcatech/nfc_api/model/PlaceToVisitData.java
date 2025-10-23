package com.salcatech.nfc_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Table(name = "place_to_visit_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceToVisitData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key ilişki
    @ManyToOne
    @JoinColumn(name = "user_product_id", nullable = false)
    private UserProduct userProduct;

    private String name;
    private LocalDate date;
}
