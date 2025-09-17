package com.ns.solve.domain.entity;

import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.Team;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Solved {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solved_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User solvedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem solvedProblem;
    
    private boolean solve; // 풀었는지 여부

    @Column(nullable = true)
    private Contest contest; // 꼭 대회용 문제 아닐 수도 있음

    @Column(nullable = true)
    private Team team; // 꼭 대회용 문제 아닐 수도 있음

    private LocalDateTime solvedTime;
}