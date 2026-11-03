package com.yarukov.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;



@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "humans")
public class Human {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    @NotBlank
    @Pattern(regexp = "^[A-ZА-ЯЁ][a-zа-яё]*$", message = "Должно начинаться с заглавной буквы и содержать только буквы")
    private String name;

    @Column(nullable = false)
    @NotBlank
    @Pattern(regexp = "^[A-ZА-ЯЁ][a-zа-яё]*$", message = "Должно начинаться с заглавной буквы и содержать только буквы")
    private String surname;

    @Column(nullable = false)
    @Min(value = 1,message = "Минимальный возраст - 1")
    private Long age;

}
