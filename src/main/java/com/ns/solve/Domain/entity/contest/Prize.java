package com.ns.solve.domain.entity.contest;

import com.ns.solve.domain.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "prizes")
public class Prize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int rank;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int numberOfWinners;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @ManyToMany
    @JoinTable(name = "prize_winners", joinColumns = @JoinColumn(name = "prize_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> winners;
}