package com.library.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;
import java.util.UUID;

@Entity @Getter
@Setter
@NoArgsConstructor
@Table(name = "Location", uniqueConstraints = {@UniqueConstraint(name = "UniqueAddress", columnNames = {"country", "city", "address"})})
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

//    Connections
    @OneToMany(mappedBy = "location")
    private List<Listing> listings;
}
