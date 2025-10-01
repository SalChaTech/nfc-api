package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.model.WeddingMemories;
import com.salcatech.nfc_api.service.WeddingMemoriesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wedding-memories")
public class WeddingMemoriesController {

    private final WeddingMemoriesService weddingMemoriesService;

    public WeddingMemoriesController(WeddingMemoriesService weddingMemoriesService) {
        this.weddingMemoriesService = weddingMemoriesService;
    }

    @PostMapping
    public WeddingMemories addWeddingMemories(@RequestBody WeddingMemories memories) {
        return weddingMemoriesService.saveWeddingMemories(memories);
    }

    @PutMapping("/{id}")
    public WeddingMemories updateWeddingMemories(@PathVariable Long id, @RequestBody WeddingMemories memory) {
        return weddingMemoriesService.updateWeddingMemories(id, memory);
    }

    @DeleteMapping("/{id}")
    public void deleteWeddingMemories(@PathVariable Long id) {
        weddingMemoriesService.deleteWeddingMemories(id);
    }

    @GetMapping
    public List<WeddingMemories> getAllWeddingMemories() {
        return weddingMemoriesService.getAllWeddingMemories();
    }
}
