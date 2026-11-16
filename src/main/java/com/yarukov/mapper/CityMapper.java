package com.yarukov.mapper;

import com.yarukov.dto.CityDTO;
import com.yarukov.model.CarCode;
import com.yarukov.model.City;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CoordinatesMapper.class, HumanMapper.class, CarCodeMapper.class})
public interface CityMapper {

    @Mapping(source = "coordinates.id", target = "coordinatesId")
    @Mapping(source = "governor.id", target = "governorId")
    @Mapping(target = "carCodeIds", expression = "java(mapCarCodeIds(city.getCarCode()))")
    @Mapping(target = "warningMessage", ignore = true)
    CityDTO toDTO(City city);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "coordinates", ignore = true)
    @Mapping(target = "governor", ignore = true)
    @Mapping(target = "carCode", ignore = true)
    void updateFieldsFromDTO(CityDTO dto, @MappingTarget City city);

    default List<Long> mapCarCodeIds(List<CarCode> carCodes) {
        if (carCodes == null) return Collections.emptyList();
        return carCodes.stream().map(CarCode::getId).collect(Collectors.toList());
    }
}