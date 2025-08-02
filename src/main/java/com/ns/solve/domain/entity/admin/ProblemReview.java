package com.ns.solve.domain.entity.admin;


import com.ns.solve.domain.entity.problem.ContainerResourceType;
import com.ns.solve.domain.entity.problem.Problem;
import com.ns.solve.domain.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "problem_reviews")
public class ProblemReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User reviewer;


    private Boolean isApproved;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}