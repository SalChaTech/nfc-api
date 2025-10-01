package com.salcatech.nfc_api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data   // Lombok getter/setter/toString vs. için
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    private String driveLink;
}
