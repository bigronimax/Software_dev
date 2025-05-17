package ru.iu3.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "artists")
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 128, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "countryId", referencedColumnName = "id")
    @JsonIgnore
    private Country country;

    @Column(name = "age")
    private int age;

    @OneToMany(mappedBy = "artist")
    @JsonIgnore
    private List<Painting> paintings;

}
