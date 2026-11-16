package com.yarukov.mapper;

import com.yarukov.dto.CoordinatesDTO;
import com.yarukov.model.Coordinates;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CoordinatesMapper {

    CoordinatesDTO toDTO(Coordinates coordinates);

    @Mapping(target = "id", ignore = true)
    Coordinates toEntity(CoordinatesDTO dto);
}