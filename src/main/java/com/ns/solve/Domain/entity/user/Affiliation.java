package com.ns.solve.domain.entity.user;

import com.ns.solve.domain.vo.AffiliationType;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
public class Affiliation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AffiliationType type;

    @ManyToMany(mappedBy = "affiliations")
    private Set<User> users = new HashSet<>();
}