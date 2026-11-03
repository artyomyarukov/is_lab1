package com.yarukov.service;

import com.yarukov.aop.NotifyClients;
import com.yarukov.dao.CarCodeDAO;
import com.yarukov.dao.CityDAO;
import com.yarukov.dao.CoordinatesDAO;
import com.yarukov.dao.HumanDAO;
import com.yarukov.dto.CarCodeDTO;
import com.yarukov.dto.CityDTO;
import com.yarukov.dto.CoordinatesDTO;
import com.yarukov.dto.HumanDTO;
import com.yarukov.model.CarCode;
import com.yarukov.model.City;
import com.yarukov.model.Coordinates;
import com.yarukov.model.Human;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CityService {

    private final CityDAO cityDAO;
    private final CoordinatesDAO coordinatesDAO;
    private final HumanDAO humanDAO;
    private final CarCodeDAO carCodeDAO;
    private final CoordinatesService coordinatesService;
    private final HumanService humanService;
    private final CarCodeService carCodeService;


    public List<CityDTO> findAll() {
        return cityDAO.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CityDTO findById(Long id) {
        City city = cityDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Город с ID " + id + " не найден"));
        return toDTO(city);
    }

    @Transactional
    @NotifyClients
    public CityDTO create(CityDTO dto) {
        String warning = null;
        if (Boolean.TRUE.equals(dto.getCapital())) {
            Optional<City> existingCapital = cityDAO.findCapital();
            if (existingCapital.isPresent()) {
                City cap = existingCapital.get();
                dto.setCapital(false);
                warning = "Город сохранен без статуса столицы, так как в системе уже есть столица: "
                        + cap.getName() + " (ID: " + cap.getId()
                        + "). Чтобы сделать этот город столицей, сначала снимите флаг у текущей столицы.";

            }
        }

        City city = new City();
        fillCityFields(city, dto);
        CityDTO result = toDTO(cityDAO.save(city));
        result.setWarningMessage(warning);
        return result;
    }

    @Transactional
    @NotifyClients
    public CityDTO update(Long id, CityDTO dto) {
        City existing = cityDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Город с ID " + id + " не найден"));

        String warning = null;

        if (Boolean.TRUE.equals(dto.getCapital())) {
            Optional<City> existingCapital = cityDAO.findCapital();
            if (existingCapital.isPresent() && !existingCapital.get().getId().equals(id)) {
                City cap = existingCapital.get();
                dto.setCapital(false);
                warning = "Изменения сохранены, но без статуса столицы! В системе уже есть столица: "
                        + cap.getName() + " (ID: " + cap.getId()
                        + "). Сначала снимите флаг столицы у этого города.";
            }
        }

        fillCityFields(existing, dto);
        CityDTO result = toDTO(cityDAO.update(existing));
        result.setWarningMessage(warning);
        return result;
    }

    @Transactional
    @NotifyClients
    public void delete(Long id) {
        City existing = cityDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Город с ID " + id + " не найден"));
        cityDAO.delete(existing);
    }

    private void fillCityFields(City city, CityDTO dto) {
        city.setName(dto.getName());
        city.setArea(dto.getArea());
        city.setPopulation(dto.getPopulation());
        city.setEstablishmentDate(dto.getEstablishmentDate());
        city.setCapital(dto.getCapital());
        city.setMetersAboveSeaLevel(dto.getMetersAboveSeaLevel());
        city.setClimate(dto.getClimate());
        city.setGovernment(dto.getGovernment());
        city.setCoordinates(resolveCoordinates(city, dto));
        city.setGovernor(resolveGovernor(dto));
        city.setCarCode(resolveCarCodes(dto.getCarCodeIds()));
    }

    private Coordinates resolveCoordinates(City city, CityDTO dto) {
        Long coordsId = dto.getCoordinatesId() != null
                ? dto.getCoordinatesId()
                : coordinatesService.create(dto.getCoordinates()).getId();

        Coordinates coords = coordinatesDAO.findById(coordsId)
                .orElseThrow(() -> new IllegalArgumentException("Координаты с ID " + coordsId + " не найдены"));

        boolean isSameCoords = city.getCoordinates() != null && city.getCoordinates().getId().equals(coordsId);
        if (!isSameCoords && coordinatesDAO.isLinkedToAnyCity(coordsId)) {
            throw new IllegalStateException("Координаты с ID " + coordsId + " уже заняты другим городом!");
        }
        return coords;
    }

    private Human resolveGovernor(CityDTO dto) {
        Long govId = dto.getGovernorId() != null
                ? dto.getGovernorId()
                : humanService.create(dto.getGovernor()).getId();

        return humanDAO.findById(govId)
                .orElseThrow(() -> new IllegalArgumentException("Губернатор с ID " + govId + " не найден"));
    }


    private List<CarCode> resolveCarCodes(List<Long> codeIds) {
        if (codeIds == null) return Collections.emptyList();
        return codeIds.stream()
                .map(id -> carCodeDAO.findById(id)
                        .orElseGet(() -> carCodeDAO.save(new CarCode(id))))
                .collect(Collectors.toList());
    }


    public CityDTO toDTO(City city) {
        if (city == null) return null;
        List<CarCodeDTO> codeDTOs = city.getCarCode() != null
                ? city.getCarCode().stream().map(carCodeService::toDTO).collect(Collectors.toList())
                : Collections.emptyList();

        List<Long> codeIds = city.getCarCode() != null
                ? city.getCarCode().stream().map(CarCode::getId).collect(Collectors.toList())
                : Collections.emptyList();

        return CityDTO.builder()
                .id(city.getId())
                .name(city.getName())
                .area(city.getArea())
                .population(city.getPopulation())
                .establishmentDate(city.getEstablishmentDate())
                .capital(city.getCapital())
                .metersAboveSeaLevel(city.getMetersAboveSeaLevel())
                .climate(city.getClimate())
                .government(city.getGovernment())
                .creationDate(city.getCreationDate())
                .coordinates(coordinatesService.toDTO(city.getCoordinates()))
                .governor(humanService.toDTO(city.getGovernor()))
                .carCode(codeDTOs)
                .coordinatesId(city.getCoordinates() != null ? city.getCoordinates().getId() : null)
                .governorId(city.getGovernor() != null ? city.getGovernor().getId() : null)
                .carCodeIds(codeIds)
                .build();
    }
}