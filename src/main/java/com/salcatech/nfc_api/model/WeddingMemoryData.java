package com.salcatech.nfc_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "wedding_memory_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeddingMemoryData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key ilişki
    @ManyToOne
    @JoinColumn(name = "user_product_id", nullable = false)
    private UserProduct userProduct;

    private String maleName;
    private String femaleName;
    private LocalDate date;
}
