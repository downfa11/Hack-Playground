// src/main/java/com/ns/solve/controller/contest/ContestTeamController.java
package com.ns.solve.controller.contest;

import com.ns.solve.domain.dto.contest.JoinTeamRequest;
import com.ns.solve.domain.dto.contest.TeamCreateDto;
import com.ns.solve.domain.dto.contest.TeamDto;
import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.service.contest.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contests/{contestId}/teams")
public class ContestTeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamDto> createTeam(@PathVariable Long contestId, @RequestBody TeamCreateDto teamCreateDto) {
        TeamDto newTeam = teamService.createTeam(contestId, teamCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTeam);
    }

    @PostMapping("/join")
    public ResponseEntity<TeamDto> joinTeam(@PathVariable Long contestId, @RequestBody JoinTeamRequest joinRequest) {
        TeamDto joinedTeam = teamService.joinTeam(contestId, joinRequest);
        return ResponseEntity.ok(joinedTeam);
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<UserDto>> getTeamMembers(@PathVariable Long contestId, @PathVariable Long teamId) {
        List<UserDto> members = teamService.getTeamMembers(teamId);
        return ResponseEntity.ok(members);
    }
}