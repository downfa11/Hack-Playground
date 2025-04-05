package com.ns.solve.domain.problem;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "problem_case")
@Builder
public class Case {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long caseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    public enum CaseKind {
        HIDDEN, EVUALATION, EXAMPLE
    }

    private CaseKind caseKind;

    private String input;
    private String expectedOutput;

    private Long bias;
    private String caseDetail;
}
