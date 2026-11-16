package com.yarukov.controller;

import com.yarukov.service.CitySpecialOperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cities/special")
@RequiredArgsConstructor
public class CitySpecialOperationsController {

    private final CitySpecialOperationsService specialService;

    @GetMapping("/sum-meters")
    public ResponseEntity<Map<String, Double>> sumMetersAboveSeaLevel() {
        return ResponseEntity.ok(Map.of("sum", specialService.sumMetersAboveSeaLevel()));
    }

    @GetMapping("/count-meters-greater")
    public ResponseEntity<Map<String, Long>> countMetersGreater(@RequestParam("threshold") double threshold) {
        return ResponseEntity.ok(Map.of("count", specialService.countByMetersAboveSeaLevelGreaterThan(threshold)));
    }

    @GetMapping("/unique-meters")
    public ResponseEntity<List<Double>> getUniqueMeters() {
        return ResponseEntity.ok(specialService.getUniqueMetersAboveSeaLevel());
    }

    @PostMapping("/relocate-from/{id}")
    public ResponseEntity<Map<String, String>> relocateFromCity(@PathVariable("id") Long id) {
        return ResponseEntity.ok(Map.of("message", specialService.relocatePopulationFromCity(id)));
    }

    @PostMapping("/relocate-capital")
    public ResponseEntity<Map<String, String>> relocateCapital() {
        return ResponseEntity.ok(Map.of("message", specialService.relocateHalfCapitalPopulation()));
    }
}