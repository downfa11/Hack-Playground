package com.ns.solve.domain.problem;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ns.solve.domain.Comment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Entity
@Data
@Table(name = "problems")
@Inheritance(strategy = InheritanceType.JOINED)
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Boolean isChecked = false;  // 검수전, 완료

    @Column(nullable = false)
    private ProblemType type;  // wargame, assignment, algorithm

    private String creator;
    private String detail;


    private Integer attemptCount;

    private Double entireCount;
    private Double correctCount;


    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Comment> commentList;

    private String source;

    private String reviewer;

    @ElementCollection
    private List<String> tags;

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


    public void incrementEntireCount() {
        if (this.entireCount == null) {
            this.entireCount = 0.0;
        }
        this.entireCount++;
    }

    public void incrementCorrectCount() {
        if (this.correctCount == null) {
            this.correctCount = 0.0;
        }
        this.correctCount++;
    }
}