package com.ns.solve.controller.contest;

import com.ns.solve.domain.dto.contest.*;
import com.ns.solve.service.contest.TeamService;
import com.ns.solve.utils.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contests/{contestId}/teams")
public class ContestTeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamDto> createTeam(@PathVariable Long contestId, @RequestBody TeamCreateDto teamCreateDto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        TeamDto newTeam = teamService.createTeam(contestId, userId, teamCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTeam);
    }

    @PostMapping("/join")
    public ResponseEntity<TeamDto> joinTeam(@PathVariable Long contestId, @RequestBody JoinTeamRequest joinRequest, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        TeamDto joinedTeam = teamService.joinTeam(contestId, userId, joinRequest);
        return ResponseEntity.ok(joinedTeam);
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<TeamUserScoreDto>> getTeamMembers(@PathVariable Long contestId, @PathVariable Long teamId) {
        List<TeamUserScoreDto> members = teamService.getTeamMembers(contestId, teamId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/top-teams")
    public ResponseEntity<List<TeamDto>> getTopTeams(@PathVariable Long contestId, @RequestParam int count) {
        List<TeamDto> topTeams = teamService.getTopTeams(contestId, count);
        return ResponseEntity.ok(topTeams);
    }

    @GetMapping("/scoreboard/time-series")
    public ResponseEntity<List<ScoreboardDto>> getTimeSeriesData(@PathVariable Long contestId) {
        List<ScoreboardDto> timeSeriesData = teamService.getTimeSeriesData(contestId);
        return ResponseEntity.ok(timeSeriesData);
    }

    @GetMapping("/my-team")
    public ResponseEntity<TeamDto> getMyTeam(@PathVariable Long contestId, @RequestParam Long userId) {
        TeamDto team = teamService.getMyTeam(contestId, userId);
        return ResponseEntity.ok(team);
    }

    @GetMapping("/check-name")
    public ResponseEntity<Boolean> checkTeamNameDuplication(@PathVariable Long contestId, @RequestParam String teamName) {
        boolean isDuplicated = teamService.isTeamNameDuplicated(contestId, teamName);
        return ResponseEntity.ok(isDuplicated);
    }
}