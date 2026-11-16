package com.yarukov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 3, max = 30, message = "Логин должен быть от 3 до 30 символов")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 4, message = "Пароль должен содержать минимум 4 символа")
    private String password;
}