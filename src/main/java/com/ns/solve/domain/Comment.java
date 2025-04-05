package com.ns.solve.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ns.solve.domain.problem.Problem;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    private String content;

    @Column(nullable = false)
    private String type; // problem, board

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User creator;

    @ManyToOne(optional = true)
    @JsonBackReference
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @ManyToOne(optional = true)
    @JsonBackReference
    @JoinColumn(name = "board_id")
    private Board board;



    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
