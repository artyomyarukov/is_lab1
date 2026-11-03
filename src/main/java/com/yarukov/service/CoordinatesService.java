package com.yarukov.service;

import com.yarukov.aop.NotifyClients;
import com.yarukov.dao.CoordinatesDAO;
import com.yarukov.dto.CoordinatesDTO;
import com.yarukov.model.Coordinates;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CoordinatesService {

    private final CoordinatesDAO coordinatesDAO;

    public CoordinatesService(CoordinatesDAO coordinatesDAO) {
        this.coordinatesDAO = coordinatesDAO;
    }

    public List<CoordinatesDTO> findAll() {
        return coordinatesDAO.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CoordinatesDTO findById(Long id) {
        Coordinates coords = coordinatesDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Координаты с ID " + id + " не найдены"));
        return toDTO(coords);
    }

    @Transactional
    @NotifyClients
    public CoordinatesDTO create(CoordinatesDTO dto) {
        if (coordinatesDAO.existsByXAndY(dto.getX(), dto.getY())) {
            throw new IllegalStateException("Координаты (" + dto.getX() + "; " + dto.getY() + ") уже существуют в системе!");
        }
        Coordinates coords = Coordinates.builder()
                .x(dto.getX())
                .y(dto.getY())
                .build();
        return toDTO(coordinatesDAO.save(coords));
    }

    @Transactional
    @NotifyClients
    public CoordinatesDTO update(Long id, CoordinatesDTO dto) {
        Coordinates existing = coordinatesDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Координаты с ID " + id + " не найдены"));

        if ((!existing.getX().equals(dto.getX()) || !existing.getY().equals(dto.getY()))
                && coordinatesDAO.existsByXAndY(dto.getX(), dto.getY())) {
            throw new IllegalStateException("Координаты (" + dto.getX() + "; " + dto.getY() + ") уже существуют в системе");
        }

        existing.setX(dto.getX());
        existing.setY(dto.getY());
        return toDTO(coordinatesDAO.update(existing));
    }

    @Transactional
    @NotifyClients
    public void delete(Long id) {
        Coordinates existing = coordinatesDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Координаты с ID " + id + " не найдены"));
        if (coordinatesDAO.isLinkedToAnyCity(id)) {
            throw new IllegalStateException("Невозможно удалить координаты ID: " + id + ", так как они связаны с городом");
        }
        coordinatesDAO.delete(existing);
    }

    public CoordinatesDTO toDTO(Coordinates coords) {
        if (coords == null) return null;
        return CoordinatesDTO.builder()
                .id(coords.getId())
                .x(coords.getX())
                .y(coords.getY())
                .build();
    }
}