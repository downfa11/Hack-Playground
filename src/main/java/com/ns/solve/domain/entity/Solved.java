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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = true)
    private Contest contest;  // 풀이 기록은 꼭 대회용이 아닐 수 있음

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true) // 풀이 기록은 꼭 대회용이 아닐 수 있음
    private Team team;

    private LocalDateTime solvedTime;
}