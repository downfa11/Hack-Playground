package com.ns.solve.domain.entity.problem;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ns.solve.domain.entity.Comment;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.domain.vo.ProblemType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Entity
@Data
@SuperBuilder
@Table(name = "problems")
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isChecked = false;  // 검수전, 완료

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProblemType type;  // wargame, assignment, algorithm

    public Optional<DomainKind> getDomainKind() {
        return Optional.empty();
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User creator;

    @Column(nullable = false, length = 2000)
    private String detail;

    private Double entireCount;
    private Double correctCount;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Comment> commentList;

    private String source;

    @ElementCollection
    private List<String> tags;


    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Enumerated(EnumType.STRING)
    private ContainerResourceType containerResourceType; // dedicated, shared

    @Column(nullable = true)
    private Integer portNumber;

    @ElementCollection
    @CollectionTable(name = "container_resource_limits", joinColumns = @JoinColumn(name = "problem_id"))
    @MapKeyColumn(name = "resource")
    @Column(name = "limits")
    private Map<String, Integer> resourceLimit; // CPU: 250m, Memory: 128Mi



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