package com.yarukov.mapper;

import com.yarukov.dto.CarCodeDTO;
import com.yarukov.model.CarCode;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CarCodeMapper {

    CarCodeDTO toDTO(CarCode carCode);

    CarCode toEntity(CarCodeDTO dto);
}