package com.yarukov.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.*;
import java.util.List;

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
    @Pattern(regexp = "^[A-ZА-ЯЁ][a-zа-яё]*(?:[\\s-][A-ZА-ЯЁ][a-zа-яё]*)*$", message = "Каждое слово должно начинаться с заглавной буквы")
    private String name;






    @Column(nullable = false)
    @Max(value = 100000)
    @Positive(message = "Площадь не может быть отрицательной")
    private double area;

    @Positive(message = "Население не может быть отрицательным")
    @Column(nullable = false)
    private Long population;
    private ZonedDateTime establishmentDate;


    @NotNull(message = "Значение не может быть пустым")
    private Boolean capital;

    private Double metersAboveSeaLevel;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "cities_codes",
            joinColumns = @JoinColumn(
                    name = "city_id"),
            inverseJoinColumns = @JoinColumn(name = "codes_id"))


    private List<CarCode> carCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Climate climate;

    @Enumerated(EnumType.STRING)
    private Government government;

    @ManyToOne
    @JoinColumn(name="human_id",nullable = false)
    private Human governor;


    @OneToOne
    @JoinColumn(name="coordinates_id",nullable = false)
    private Coordinates coordinates;


    private LocalDateTime creationDate;

    @PrePersist
    void makeCreationDate(){
        creationDate = LocalDateTime.now();
    }



}