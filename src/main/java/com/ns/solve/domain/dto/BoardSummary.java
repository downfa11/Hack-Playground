package com.ns.solve.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BoardSummary {
    private Long id;
    private String title;
    private String creator;
    private LocalDateTime lastModified;
    private Integer commentCount;
}
