package com.salcatech.nfc_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "user_product")
@Data   // Lombok getter/setter/toString vs. için
@NoArgsConstructor
@AllArgsConstructor
public class UserProduct {
    @Id
    @GeneratedValue(generator = "ten-digit-id")
    @GenericGenerator(name = "ten-digit-id", strategy = "com.salcatech.nfc_api.helper.UserProductIdGenerator")
    @Column(length = 10, updatable = false, nullable = false)
    private String id;

    @Column(name = "email")
    private String email;

    @Column(name = "drive_application_folder_id")
    private String driveApplicationFolderId;
}
