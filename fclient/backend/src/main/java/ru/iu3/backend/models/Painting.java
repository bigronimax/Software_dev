package ru.iu3.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "paintings")
public class Painting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @ManyToOne
    @JoinColumn(name = "artistId", referencedColumnName = "id")
    private Artist artist;

    @ManyToOne
    @JoinColumn(name = "museumId", referencedColumnName = "id")
    private Museum museum;

    @Column(name = "year")
    private int year;
}
