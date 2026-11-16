package com.yarukov.controller;

import com.yarukov.dto.CarCodeDTO;
import com.yarukov.service.CarCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/car-codes")
@RequiredArgsConstructor
public class CarCodeController {

    private final CarCodeService carCodeService;

    @GetMapping
    public ResponseEntity<List<CarCodeDTO>> getAll() {
        return ResponseEntity.ok(carCodeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarCodeDTO> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(carCodeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CarCodeDTO> create(@Valid @RequestBody CarCodeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carCodeService.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        carCodeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}