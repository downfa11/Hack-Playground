package com.ns.solve.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String account;

    @Column(nullable = false)
    private String password;

    private Long score; // 맞춘 문제 개수 (전체 랭킹, 분야별 랭킹)

    @ElementCollection
    @CollectionTable(name = "user_field_scores", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "field")
    @Column(name = "score")
    private Map<String, Long> fieldScores = new HashMap<>();

    private LocalDateTime created;
    private LocalDateTime lastActived;

    private String provider;

    public boolean isMemberAbove() {
        return this.role != null && this.role.ordinal() > Role.ROLE_MEMBER.ordinal();
    }
}
