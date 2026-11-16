package com.yarukov.service;

import com.yarukov.aop.NotifyClients;
import com.yarukov.dao.CityDAO;
import com.yarukov.model.City;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CitySpecialOperationsService {

    private final CityDAO cityDAO;


    public double sumMetersAboveSeaLevel() {
        return cityDAO.findAll().stream()
                .map(City::getMetersAboveSeaLevel)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public long countByMetersAboveSeaLevelGreaterThan(double threshold) {
        return cityDAO.findAll().stream()
                .map(City::getMetersAboveSeaLevel)
                .filter(Objects::nonNull)
                .filter(m -> m > threshold)
                .count();
    }

    public List<Double> getUniqueMetersAboveSeaLevel() {
        return cityDAO.findAll().stream()
                .map(City::getMetersAboveSeaLevel)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }


    @Transactional
    @NotifyClients
    public String relocatePopulationFromCity(Long sourceCityId) {
        City source = cityDAO.findById(sourceCityId)
                .orElseThrow(() -> new IllegalArgumentException("Город с ID " + sourceCityId + " не найден"));

        List<City> smallestCities = cityDAO.findCitiesWithSmallestPopulation(sourceCityId, 1);
        if (smallestCities.isEmpty()) {
            throw new IllegalStateException("В системе нет других городов для переселения!");
        }
        City target = smallestCities.get(0);
        long movedPeople = source.getPopulation() - 1;
        if (movedPeople <= 0) {
            throw new IllegalStateException("В городе '" + source.getName() + "' всего 1 житель, переселять некого");
        }

        source.setPopulation(1L);
        target.setPopulation(target.getPopulation() + movedPeople);

        cityDAO.update(source);
        cityDAO.update(target);

        return String.format("Переселено %d чел. из города '%s' в город '%s' (новое население: %d)",
                movedPeople, source.getName(), target.getName(), target.getPopulation());
    }


    @Transactional
    @NotifyClients
    public String relocateHalfCapitalPopulation() {
        City capital = cityDAO.findCapital()
                .orElseThrow(() -> new IllegalStateException("В системе сейчас не назначена столица!"));

        List<City> smallestCities = cityDAO.findCitiesWithSmallestPopulation(capital.getId(), 3);
        if (smallestCities.size() < 3) {
            throw new IllegalStateException("Для этой операции в системе должно быть минимум 3 других города помимо столицы (сейчас: " + smallestCities.size() + ")!");
        }

        long halfPopulation = capital.getPopulation() / 2;
        if (halfPopulation < 3) {
            throw new IllegalStateException("Население столицы слишком мало для разделения на 3 города!");
        }

        long perCity = halfPopulation / 3;
        long remainder = halfPopulation % 3;

        capital.setPopulation(capital.getPopulation() - halfPopulation);
        cityDAO.update(capital);

        for (int i = 0; i < 3; i++) {
            City target = smallestCities.get(i);
            long add = perCity + (i == 0 ? remainder : 0);
            target.setPopulation(target.getPopulation() + add);
            cityDAO.update(target);
        }

        String targetNames = smallestCities.stream().map(City::getName).collect(Collectors.joining(", "));
        return String.format("Из столицы '%s' переселено %d чел. (50%%) в три города: %s",
                capital.getName(), halfPopulation, targetNames);
    }
}