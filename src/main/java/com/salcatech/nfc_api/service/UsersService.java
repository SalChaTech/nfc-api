package com.salcatech.nfc_api.service;

import com.salcatech.nfc_api.model.Users;
import com.salcatech.nfc_api.repository.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;


    public UsersService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Users saveUser(Users user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        return usersRepository.save(user);
    }

    public Users updateEmailAndDriveLinkByProductId(String productId, String email, String driveLink) {
        Users existing = usersRepository.findFirstByProductId(productId);
        if (existing == null) return null;

        existing.setEmail(email);
        existing.setDriveApplicationFolderId(driveLink);
        // password değiştirilmesi gerekiyorsa hashle
        return usersRepository.save(existing);
    }

    public void deleteUser(Long id) {
        usersRepository.deleteById(id);
    }

    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }

    public Users getUserById(Long id) {
        return usersRepository.findById(id)
                .orElse(null);
    }

    public Users getUserByProductId(String productId) {
        return usersRepository.findFirstByProductId(productId);
    }
}
