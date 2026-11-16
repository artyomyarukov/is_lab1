package com.yarukov.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoordinatesDTO {

    private Long id;

    @NotNull(message = "Координата x не может быть пустой")
    @Max(value = 176, message = "Максимальное значение X — 176")
    private Float x;

    @NotNull(message = "Координата y не может быть пустой")
    private Integer y;
}