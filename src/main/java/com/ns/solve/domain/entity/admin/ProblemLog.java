package com.ns.solve.domain.entity.admin;

import com.ns.solve.domain.vo.OperationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "problem_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long problemId;

    private String problemTitle;

    private String creatorUsername;

    @Enumerated(EnumType.STRING)
    private OperationType operationType; // CREATE, MODIFY, FILE_UPLOAD

    private LocalDateTime createdAt;

    private boolean reported; // 이메일 보고 여부
}
