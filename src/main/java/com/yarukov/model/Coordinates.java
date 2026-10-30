package com.yarukov.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(
        columnNames = {"x","y"})
})
public class Coordinates {
    @Id
    @Max(value = 176)
    @Column(nullable = false)
    private Float x;
    @Id
    @Column(nullable = false)
    private Integer y;

}
