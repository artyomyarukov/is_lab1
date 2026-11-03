package com.yarukov.controller;

import com.yarukov.dto.CoordinatesDTO;
import com.yarukov.service.CoordinatesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coordinates")
@RequiredArgsConstructor
public class CoordinatesController {

    private final CoordinatesService coordinatesService;

    @GetMapping
    public ResponseEntity<List<CoordinatesDTO>> getAll() {
        return ResponseEntity.ok(coordinatesService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoordinatesDTO> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(coordinatesService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CoordinatesDTO> create(@Valid @RequestBody CoordinatesDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coordinatesService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoordinatesDTO> update(@PathVariable("id") Long id, @Valid @RequestBody CoordinatesDTO dto) {
        return ResponseEntity.ok(coordinatesService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        coordinatesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}