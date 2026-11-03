package com.yarukov.controller;

import com.yarukov.dto.HumanDTO;
import com.yarukov.service.HumanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/humans")
@RequiredArgsConstructor
public class HumanController {

    private final HumanService humanService;

    @GetMapping
    public ResponseEntity<List<HumanDTO>> getAll() {
        return ResponseEntity.ok(humanService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HumanDTO> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(humanService.findById(id));
    }

    @PostMapping
    public ResponseEntity<HumanDTO> create(@Valid @RequestBody HumanDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(humanService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HumanDTO> update(@PathVariable("id") Long id, @Valid @RequestBody HumanDTO dto) {
        return ResponseEntity.ok(humanService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        humanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}