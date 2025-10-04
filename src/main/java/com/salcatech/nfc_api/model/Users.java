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
    private Long id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "email")
    private String email;

    @Column(name = "drive_application_folder_id")
    private String driveApplicationFolderId;

    @Column(name = "password_hash")
    private String passwordHash;


}
