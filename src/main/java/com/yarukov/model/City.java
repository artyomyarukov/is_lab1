package com.yarukov.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.*;
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "cities")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;



    private LocalDateTime creationDate;

    @Positive
    @Column(nullable = false)
    private double area;

    @Positive
    @Column(nullable = false)
    private Long population;
    private ZonedDateTime establishmentDate;

    @Column(nullable = false)
    private Boolean capital;

    private Double metersAboveSeaLevel;

    @Column(nullable = false)
    @Positive
    @Max(value = 1000)
    private Integer carCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Climate climate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Government government;

    @ManyToOne
    @JoinColumn(name="human_id",nullable = false)
    private Human governor;


    @OneToOne
    @JoinColumn(name="coordinates_id",nullable = false)
    private Coordinates coordinates;


    @PrePersist
    void makeCreationDate(){
        creationDate = LocalDateTime.now();
    }



}