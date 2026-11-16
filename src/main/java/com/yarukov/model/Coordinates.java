package com.yarukov.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(
        columnNames = {"x","y"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Max(value = 176,message = "Максимальное значение 176")
    @NotNull(message = "Координата x не может быть пустой ")
    private Float x;

    @NotNull(message = "Координата y не может быть пустой ")
    private Integer y;

}
