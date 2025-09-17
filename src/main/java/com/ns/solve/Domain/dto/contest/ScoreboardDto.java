package com.ns.solve.domain.dto.contest;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ScoreboardDto {
    private String time;
    private Map<String, Integer> scores = new HashMap<>();

    public void addScore(String teamName, Integer score) {
        this.scores.put(teamName, score);
    }
}