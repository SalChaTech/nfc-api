package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.dto.WeddingMemoryRequest;
import com.salcatech.nfc_api.model.WeddingMemories;
import com.salcatech.nfc_api.service.JwtService;
import com.salcatech.nfc_api.service.UsersService;
import com.salcatech.nfc_api.service.WeddingMemoriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wedding-memories")
public class WeddingMemoriesController {

    private static final Logger logger = LoggerFactory.getLogger(WeddingMemoriesController.class);


    private final UsersService usersService;
    private final WeddingMemoriesService weddingMemoriesService;
    private final JwtService jwtService;

    public WeddingMemoriesController(UsersService usersService, WeddingMemoriesService weddingMemoriesService, JwtService jwtService) {
        this.usersService = usersService;
        this.weddingMemoriesService = weddingMemoriesService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public WeddingMemories addWeddingMemories(@RequestBody WeddingMemories memories) {
        return weddingMemoriesService.saveWeddingMemories(memories);
    }

    @DeleteMapping("/{id}")
    public void deleteWeddingMemories(@PathVariable Long id) {
        weddingMemoriesService.deleteWeddingMemories(id);
    }

    @GetMapping
    public List<WeddingMemories> getAllWeddingMemories() {
        return weddingMemoriesService.getAllWeddingMemories();
    }

    @GetMapping("/by-user")
    public ResponseEntity<?> getWeddingMemoriesByUser(
            @RequestHeader("Authorization") String token) {

        // Token’dan userId çıkar
        String userIdStr = jwtService.extractUserIdFromToken(token);
        if (userIdStr == null) {
            return ResponseEntity.status(401).body("Invalid token!");
        }

        Long userId = Long.parseLong(userIdStr);
        var user = usersService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // Kullanıcının memory kaydını al
        WeddingMemories memory = weddingMemoriesService.getWeddingMemoryByUserId(userId);
        if (memory == null) {
            return ResponseEntity.status(404).body("Wedding memory not found");
        }

        return ResponseEntity.ok(memory);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateWeddingMemories(
            @RequestHeader("Authorization") String token,
            @RequestBody WeddingMemoryRequest request) {

        String userIdStr = jwtService.extractUserIdFromToken(token);
        if (userIdStr == null) {
            return ResponseEntity.status(401).body("Invalid token!");
        }
        logger.info("userIdStr: {}", userIdStr);


        Long userId = Long.parseLong(userIdStr);
        var user = usersService.getUserById(userId);
        logger.info("user: {}", user);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        // Kullanıcının memory kaydını al
        WeddingMemories memory = weddingMemoriesService.getWeddingMemoryByUserId(userId);
        if (memory == null) {
            return ResponseEntity.status(404).body("Wedding memory not found");
        }

        logger.info("memory: {}", memory);
        logger.info("female Name : : {}", request.getFemaleName());
        logger.info("male Name : : {}", request.getMaleName());
        logger.info("date: {}", request.getDate());

        // Sadece null olmayan alanları güncelle
        if (request.getMaleName() != null) {
            memory.setMaleName(request.getMaleName());
        }
        if (request.getFemaleName() != null) {
            memory.setFemaleName(request.getFemaleName());
        }
        if (request.getDate() != null) {
            memory.setDate(request.getDate());
        }

        logger.info("memory: {}", memory);


        WeddingMemories updatedMemory = weddingMemoriesService.saveWeddingMemories(memory);
        return ResponseEntity.ok(updatedMemory);
    }
}
