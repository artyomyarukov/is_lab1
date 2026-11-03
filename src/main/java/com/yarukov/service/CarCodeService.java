package com.yarukov.service;

import com.yarukov.aop.NotifyClients;
import com.yarukov.dao.CarCodeDAO;
import com.yarukov.dto.CarCodeDTO;
import com.yarukov.mapper.CarCodeMapper;
import com.yarukov.model.CarCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarCodeService {

    private final CarCodeDAO carCodeDAO;
    private final CarCodeMapper carCodeMapper;

    public List<CarCodeDTO> findAll() {
        return carCodeDAO.findAll().stream()
                .map(carCodeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CarCodeDTO findById(Long id) {
        CarCode code = carCodeDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Автомобильный код " + id + " не найден"));
        return carCodeMapper.toDTO(code);
    }

    @Transactional
    @NotifyClients
    public CarCodeDTO create(CarCodeDTO dto) {
        if (carCodeDAO.findById(dto.getId()).isPresent()) {
            throw new IllegalStateException("Автомобильный код " + dto.getId() + " уже существует");
        }
        return carCodeMapper.toDTO(carCodeDAO.save(carCodeMapper.toEntity(dto)));
    }

    @Transactional
    @NotifyClients
    public void delete(Long id) {
        CarCode existing = carCodeDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Автомобильный код " + id + " не найден"));
        if (carCodeDAO.isLinkedToAnyCity(id)) {
            throw new IllegalStateException("Невозможно удалить автомобильный код " + id + ", так как он привязан к городу");
        }
        carCodeDAO.delete(existing);
    }
}