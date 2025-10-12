package com.salcatech.nfc_api;

import com.salcatech.nfc_api.model.UserProduct;
import com.salcatech.nfc_api.repository.UserProductRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Commit // H2 ile memory DB açar, JPA repository’leri test eder
public class RandomTest {

    @Autowired
    private UserProductRepository userProductRepository;

    @Test
    void testCreateUserProduct() {
        // Yeni bir UserProduct nesnesi oluştur
        UserProduct userProduct = new UserProduct();
//        userProduct.setEmail("test@example.com");
//        userProduct.setDriveApplicationFolderId("folder_12345");

        // Veritabanına kaydet
        UserProduct saved = userProductRepository.save(userProduct);

        // Assertions
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull(); // custom generator ID üretmiş olmalı
//        assertThat(saved.getEmail()).isEqualTo("test@example.com");
//        assertThat(saved.getDriveApplicationFolderId()).isEqualTo("folder_12345");

        System.out.println("✅ Kayıt başarıyla oluşturuldu: " + saved);
    }
}
