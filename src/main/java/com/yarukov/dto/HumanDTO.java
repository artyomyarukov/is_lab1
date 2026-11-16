package com.yarukov.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HumanDTO {

    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Pattern(regexp = "^[A-ZА-ЯЁ][a-zа-яё]*$", message = "Имя должно начинаться с заглавной буквы и содержать только буквы")
    private String name;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Pattern(regexp = "^[A-ZА-ЯЁ][a-zа-яё]*$", message = "Фамилия должна начинаться с заглавной буквы и содержать только буквы")
    private String surname;

    @NotNull(message = "Возраст обязателен")
    @Min(value = 1, message = "Минимальный возраст — 1")
    private Long age;
}