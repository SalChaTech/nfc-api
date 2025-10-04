package com.salcatech.nfc_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "wedding_memories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeddingMemories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key ilişki
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private String maleName;
    private String femaleName;
    private LocalDate date;
}
