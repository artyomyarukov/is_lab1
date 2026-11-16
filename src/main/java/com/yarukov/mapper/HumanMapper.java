package com.yarukov.mapper;

import com.yarukov.dto.HumanDTO;
import com.yarukov.model.Human;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HumanMapper {

    HumanDTO toDTO(Human human);

    @Mapping(target = "id", ignore = true)
    Human toEntity(HumanDTO dto);
}