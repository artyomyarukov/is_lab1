package com.yarukov.service;

import com.yarukov.aop.NotifyClients;
import com.yarukov.dao.HumanDAO;
import com.yarukov.dto.HumanDTO;
import com.yarukov.model.Human;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HumanService {

    private final HumanDAO humanDAO;

    public HumanService(HumanDAO humanDAO) {
        this.humanDAO = humanDAO;
    }

    public List<HumanDTO> findAll() {
        return humanDAO.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public HumanDTO findById(Long id) {
        Human human = humanDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Губернатор с ID " + id + " не найден"));
        return toDTO(human);
    }

    @Transactional
    @NotifyClients
    public HumanDTO create(HumanDTO dto) {
        Human human = Human.builder()
                .name(dto.getName())
                .surname(dto.getSurname())
                .age(dto.getAge())
                .build();
        return toDTO(humanDAO.save(human));
    }

    @Transactional
    @NotifyClients
    public HumanDTO update(Long id, HumanDTO dto) {
        Human existing = humanDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Губернатор с ID " + id + " не найден"));
        existing.setName(dto.getName());
        existing.setSurname(dto.getSurname());
        existing.setAge(dto.getAge());
        return toDTO(humanDAO.update(existing));
    }

    @Transactional
    @NotifyClients
    public void delete(Long id) {
        Human existing = humanDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Губернатор с ID " + id + " не найден"));
        if (humanDAO.isLinkedToAnyCity(id)) {
            throw new IllegalStateException("Невозможно удалить губернатора ID: " + id + ", так как он связан с городом");
        }
        humanDAO.delete(existing);
    }

    public HumanDTO toDTO(Human human) {
        if (human == null) return null;
        return HumanDTO.builder()
                .id(human.getId())
                .name(human.getName())
                .surname(human.getSurname())
                .age(human.getAge())
                .build();
    }
}