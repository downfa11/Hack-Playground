package com.ns.solve.domain.entity.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDistributionResponse {
    private String name; // 분야명
    private int value;   // 문제 수
}
