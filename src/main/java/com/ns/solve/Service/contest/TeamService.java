package com.ns.solve.service.contest;

import com.ns.solve.domain.dto.contest.JoinTeamRequest;
import com.ns.solve.domain.dto.contest.ScoreboardDto;
import com.ns.solve.domain.dto.contest.TeamCreateDto;
import com.ns.solve.domain.dto.contest.TeamDto;
import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.ContestSolved;
import com.ns.solve.domain.entity.contest.Team;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.contest.ContestRepository;
import com.ns.solve.repository.contest.ContestSolvedRepository;
import com.ns.solve.repository.contest.TeamRepository;
import com.ns.solve.utils.exception.ErrorCode.TeamErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import com.ns.solve.utils.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final ContestRepository contestRepository;
    private final UserRepository userRepository;
    private final ContestSolvedRepository contestSolvedRepository;

    @Transactional
    public TeamDto createTeam(Long contestId, TeamCreateDto teamCreateDto) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.CONTEST_NOT_FOUND));

        if (teamRepository.existsByContestAndName(contest, teamCreateDto.getName())) {
            throw new SolvedException(TeamErrorCode.DUPLICATE_TEAM_NAME);
        }

        Team team = Team.builder()
                .name(teamCreateDto.getName())
                .password(teamCreateDto.getPassword())
                .contest(contest)
                .createdAt(LocalDateTime.now())
                .build();

        return TeamDto.from(teamRepository.save(team));
    }

    @Transactional
    public TeamDto joinTeam(Long contestId, JoinTeamRequest joinRequest) {
        Team team = teamRepository.findById(joinRequest.getTeamId())
                .orElseThrow(() -> new SolvedException(TeamErrorCode.TEAM_NOT_FOUND));

        if (!team.getContest().getId().equals(contestId)) {
            throw new SolvedException(TeamErrorCode.CONTEST_NOT_FOUND);
        }

        User user = userRepository.findById(joinRequest.getUserId())
                .orElseThrow(() -> new SolvedException(TeamErrorCode.USER_NOT_FOUND));

        if (team.getMembers().contains(user)) {
            throw new SolvedException(TeamErrorCode.ALREADY_JOINED_TEAM);
        }

        team.getMembers().add(user);
        return TeamDto.from(teamRepository.save(team));
    }

    @Transactional(readOnly = true)
    public List<UserDto> getTeamMembers(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.TEAM_NOT_FOUND));

        return team.getMembers().stream()
                .map(UserMapper::mapperToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamDto> getTopTeams(Long contestId, int count) {
        return teamRepository.findByContestId(contestId).stream()
                .sorted(Comparator.comparing(Team::getPoints).reversed())
                .limit(count)
                .map(TeamDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamDto getMyTeam(Long contestId, Long userId) {
        return teamRepository.findByContestIdAndMembers_Id(contestId, userId)
                .map(TeamDto::from)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.TEAM_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<ScoreboardDto> getTimeSeriesData(Long contestId) {
        List<Team> teams = teamRepository.findByContestId(contestId);
        Map<Long, Integer> teamScores = teams.stream()
                .collect(Collectors.toMap(Team::getId, t -> 0));
        Map<Long, String> teamNames = teams.stream()
                .collect(Collectors.toMap(Team::getId, Team::getName));

        List<ContestSolved> solves = contestSolvedRepository.findByContest_Id(contestId);
        solves.sort(Comparator.comparing(ContestSolved::getSolvedTime));

        List<ScoreboardDto> timeSeriesData = new ArrayList<>();

        for (ContestSolved solve : solves) {
            Long teamId = solve.getTeam().getId();
            int problemScore = solve.getSolvedProblem().getPoints();
            teamScores.computeIfPresent(teamId, (k, v) -> v + problemScore);

            ScoreboardDto dto = new ScoreboardDto();
            dto.setTime(solve.getSolvedTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            teamScores.forEach((id, score) -> dto.addScore(teamNames.get(id), score));
            timeSeriesData.add(dto);
        }

        return timeSeriesData;
    }
}