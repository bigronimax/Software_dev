package ru.iu3.backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "paintings")
@Access(AccessType.FIELD)
public class Painting {

    public Painting() { }
    public Painting(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    public long id;

    @Column(name = "name", nullable = false, unique = true)
    public String name;

    @ManyToOne()
    @JoinColumn(name = "museum_id")
    public Museum museum;
}
