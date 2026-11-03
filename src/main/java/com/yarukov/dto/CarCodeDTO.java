package com.yarukov.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarCodeDTO {

    @NotNull(message = "Код региона обязателен")
    @Positive(message = "Код региона должен быть больше 0")
    @Max(value = 1000, message = "Максимальный код региона - 1000")
    private Long id;
}