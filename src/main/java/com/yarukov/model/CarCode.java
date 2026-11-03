package com.yarukov.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "codes")
public class CarCode {
    @Id
    @Positive
    @Max(value = 1000)
    @NotNull
    private Long id;

}
