package com.ns.solve.domain;

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

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String account;
    private String password;

    private Long score; // 맞춘 문제 개수 (전체 랭킹, 분야별 랭킹)

    @ElementCollection
    @CollectionTable(name = "user_field_scores", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "field")
    @Column(name = "score")
    private Map<String, Long> fieldScores = new HashMap<>();

    private LocalDateTime created;
    private LocalDateTime lastActived;
}
