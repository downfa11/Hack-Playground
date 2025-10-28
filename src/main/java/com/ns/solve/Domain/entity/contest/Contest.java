package com.ns.solve.domain.entity.contest;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ns.solve.domain.entity.user.Affiliation;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.domain.vo.AffiliationType;
import com.ns.solve.domain.vo.ContestType;
import com.ns.solve.domain.vo.ContestWargameKind;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contests")
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContestType type;

    @ElementCollection(targetClass = ContestWargameKind.class)
    @JoinTable(name = "contest_problem_kinds", joinColumns = @JoinColumn(name = "contest_id"))
    @Column(name = "problem_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<ContestWargameKind> problemKinds;


    @Column(nullable = false)
    private String organizerName;

    @Column(nullable = true)
    private Integer maxTeamSize;

    @Column(nullable = true)
    private String prize;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rules;

    @Column(nullable = false)
    private boolean reviewConsent;

    @ManyToMany
    @JoinTable(name = "contest_organizers", joinColumns = @JoinColumn(name = "contest_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> organizers;

    @ManyToMany
    @JoinTable(name = "contest_participants", joinColumns = @JoinColumn(name = "contest_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> participants;

    @ElementCollection(targetClass = AffiliationType.class)
    @JoinTable(name = "contest_affiliation_types", joinColumns = @JoinColumn(name = "contest_id"))
    @Column(name = "affiliation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<AffiliationType> affiliationTypes;

    @ManyToMany
    @JoinTable(name = "contest_affiliations", joinColumns = @JoinColumn(name = "contest_id"), inverseJoinColumns = @JoinColumn(name = "affiliation_id"))
    private Set<Affiliation> affiliations;

    @JsonManagedReference
    @OneToMany(mappedBy = "contest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContestProblem> problems;

    @OneToMany(mappedBy = "contest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prize> prizes;
}