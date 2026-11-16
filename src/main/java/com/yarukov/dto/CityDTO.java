package com.yarukov.dto;

import com.yarukov.model.Climate;
import com.yarukov.model.Government;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityDTO {

    private Long id;

    @NotBlank(message = "Название города не может быть пустым")
    @Pattern(regexp = "^[A-ZА-ЯЁ][a-zа-яё]*(?:[\\s-][A-ZА-ЯЁ][a-zа-яё]*)*$",
            message = "Каждое слово должно начинаться с заглавной буквы")
    private String name;

    @Positive(message = "Площадь не может быть отрицательной или нулевой")
    @Max(value = 100000, message = "Максимальная площадь — 100000")
    private double area;

    @NotNull(message = "Население не может быть пустым")
    @Positive(message = "Население должно быть больше 0")
    private Long population;

    private ZonedDateTime establishmentDate;

    @NotNull(message = "Поле 'столица' не может быть пустым")
    private Boolean capital;

    private Double metersAboveSeaLevel;

    @NotNull(message = "Климат не может быть пустым")
    private Climate climate;

    private Government government;
    private LocalDateTime creationDate;


    private CoordinatesDTO coordinates;
    private HumanDTO governor;
    private List<CarCodeDTO> carCode;
    private Long coordinatesId;
    private Long governorId;
    private List<Long> carCodeIds;
    private String warningMessage;
}