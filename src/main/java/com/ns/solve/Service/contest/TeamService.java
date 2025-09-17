package com.ns.solve.service.contest;

import com.ns.solve.domain.dto.contest.JoinTeamRequest;
import com.ns.solve.domain.dto.contest.ScoreboardDto;
import com.ns.solve.domain.dto.contest.TeamCreateDto;
import com.ns.solve.domain.dto.contest.TeamDto;
import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.entity.Solved;
import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.Team;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.repository.SolvedRepository;
import com.ns.solve.repository.contest.ContestRepository;
import com.ns.solve.repository.contest.TeamRepository;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.utils.exception.ErrorCode.TeamErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import com.ns.solve.utils.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final ContestRepository contestRepository;
    private final UserRepository userRepository;
    private final SolvedRepository solvedRepository;

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

        Team newTeam = teamRepository.save(team);
        return TeamDto.from(newTeam);
    }

    @Transactional
    public TeamDto joinTeam(Long contestId, JoinTeamRequest joinRequest) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.CONTEST_NOT_FOUND));

        Team team = teamRepository.findById(joinRequest.getTeamId())
                .orElseThrow(() -> new SolvedException(TeamErrorCode.TEAM_NOT_FOUND));

        User user = userRepository.findById(joinRequest.getUserId())
                .orElseThrow(() -> new SolvedException(TeamErrorCode.USER_NOT_FOUND));

        Set<User> existingMembers = team.getMembers();
        if (existingMembers.contains(user)) {
            throw new SolvedException(TeamErrorCode.ALREADY_JOINED_TEAM);
        }

        existingMembers.add(user);
        team.setMembers(existingMembers);
        Team newTeam = teamRepository.save(team);
        return TeamDto.from(newTeam);
    }


    @Transactional(readOnly = true)
    public List<UserDto> getTeamMembers(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.TEAM_NOT_FOUND));

        return team.getMembers().stream()
                .map(user -> UserMapper.mapperToUserDto(user))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamDto> getTopTeams(Long contestId) {
        // 실제 구현에서는 대회 ID에 해당하는 팀만 조회해야 함
        // 여기서는 임시로 모든 팀을 조회하고 점수 순으로 정렬
        return teamRepository.findAll().stream()
                .sorted(Comparator.comparing(Team::getPoints).reversed())
                .limit(5) // 상위 5개 팀만
                .map(TeamDto::from)
                .collect(Collectors.toList());
    }

    public List<ScoreboardDto> getTimeSeriesData(Long contestId) {
        List<Solved> solves = solvedRepository.findByContestId(contestId);

        Map<Long, Integer> teamScores = teamRepository.findByContestId(contestId).stream()
                .collect(Collectors.toMap(Team::getId, team -> 0));

        List<ScoreboardDto> timeSeriesData = new ArrayList<>();
        solves.sort(Comparator.comparing(Solved::getSolvedTime));

//        for (Solved solve : solves) {
//            teamScores.computeIfPresent(solve.getTeamId().getId(), (key, score) -> score + solve.getProblemScore());
//            ScoreboardDto dto = new ScoreboardDto();
//            dto.setTime(solve.getSolvedTime().format(DateTimeFormatter.ofPattern("HH:mm")));
//
//            teamScores.forEach((teamId, score) -> {
//                String teamName = teamRepository.findById(teamId).orElseThrow().getName();
//                dto.addScore(teamName, score);
//            });
//
//            timeSeriesData.add(dto);
//        }

        return timeSeriesData;
    }

}