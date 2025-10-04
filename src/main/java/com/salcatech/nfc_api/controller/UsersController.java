package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.dto.UpdateEmailAndDriveApplicationFolderIdRequest;
import com.salcatech.nfc_api.model.Users;
import com.salcatech.nfc_api.service.JwtService;
import com.salcatech.nfc_api.service.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);


    private final UsersService usersService;
    private final JwtService jwtService;

    public UsersController(UsersService usersService, JwtService jwtService) {
        this.usersService = usersService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public Users createUser(@RequestBody Users user) {
        return usersService.saveUser(user);
    }

    @PutMapping("/update-email-and-drive-application-folder-id")
    public ResponseEntity<?> updateEmailAndDriveApplicationFolderId(
            @RequestHeader("Authorization") String userToken,
            @RequestBody UpdateEmailAndDriveApplicationFolderIdRequest request) {

        // Token’dan productId çek
        String tokenProductId = jwtService.extractProductIdFromToken(userToken);
        if (tokenProductId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token!");
        }
        String newEmail = request.getEmail();
        if (newEmail == null || newEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required!");
        }

        String newDriveLink = request.getDriveApplicationFolderId();
        if (newDriveLink == null || newDriveLink.isEmpty()) {
            return ResponseEntity.badRequest().body("Drive Link is required!");
        }

        Users updatedUser = usersService.updateEmailAndDriveLinkByProductId(tokenProductId, newEmail,newDriveLink);

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        usersService.deleteUser(id);
    }

    @GetMapping
    public List<Users> getAllUsers() {
        return usersService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Users getUserById(@PathVariable Long id) {
        return usersService.getUserById(id);
    }

    @GetMapping("/drive-application-folder-id/{productId}")
    public ResponseEntity<?> getDriveApplicationFolderIdByProductId(@PathVariable String productId) {
        try {
            Users user = usersService.getUserByProductId(productId);
            logger.info("user: {}", user);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found for productId: " + productId));
            }

            String driveApplicationFolderId = user.getDriveApplicationFolderId();

            logger.info("driveApplicationFolderId: {}", driveApplicationFolderId);

            if (driveApplicationFolderId == null || driveApplicationFolderId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(Map.of("message", "Drive link not set for this user"));
            }

            return ResponseEntity.ok(Map.of("driveApplicationFolderId", driveApplicationFolderId));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error retrieving drive link: " + e.getMessage()));
        }
    }
}

