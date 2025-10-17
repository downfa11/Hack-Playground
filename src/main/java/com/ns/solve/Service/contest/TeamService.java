package com.ns.solve.service.contest;

import com.ns.solve.domain.dto.contest.*;
import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.ContestSolved;
import com.ns.solve.domain.entity.contest.Team;
import com.ns.solve.domain.entity.user.Affiliation;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.domain.vo.ContestType;
import com.ns.solve.repository.AffiliationRepository;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.repository.contest.ContestRepository;
import com.ns.solve.repository.contest.ContestSolvedRepository;
import com.ns.solve.repository.contest.TeamRepository;
import com.ns.solve.utils.exception.ErrorCode.TeamErrorCode;
import com.ns.solve.utils.exception.SolvedException;
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
    private final ContestSolvedRepository contestSolvedRepository;
    private final AffiliationRepository affiliationRepository;

    @Transactional
    public TeamDto createTeam(Long contestId, Long userId, TeamCreateDto teamCreateDto) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.CONTEST_NOT_FOUND));

        if (teamRepository.existsByContestAndName(contest, teamCreateDto.getName())) {
            throw new SolvedException(TeamErrorCode.DUPLICATE_TEAM_NAME);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.USER_NOT_FOUND));

        Team team = Team.builder()
                .name(teamCreateDto.getName())
                .password(teamCreateDto.getPassword())
                .contest(contest)
                .createdAt(LocalDateTime.now())
                .build();

        team.getMembers().add(user);
        return TeamDto.from(teamRepository.save(team));
    }

    @Transactional
    public TeamDto joinTeam(Long contestId, Long userId, JoinTeamRequest joinRequest) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.CONTEST_NOT_FOUND));

        Team team;
        if(contest.getType().equals(ContestType.GROUP)) {
            if(joinRequest.getAffiliationId()==null)
                throw new SolvedException(TeamErrorCode.AFFILIATION_NOT_SELECTED);

            Affiliation affiliation = affiliationRepository.findById(joinRequest.getAffiliationId())
                    .orElseThrow(() -> new SolvedException(TeamErrorCode.AFFILIATION_NOT_SELECTED));

            // 팀이 없으면 소속 이름으로 만들거나, 소속 이름으로 참가
            String affiliationName = affiliation.getName();
            team = teamRepository.findByContestIdAndName(contestId, affiliationName)
                    .orElseGet(() -> {
                        Team newTeam = Team.builder()
                                .contest(contest)
                                .name(affiliationName)
                                .members(new HashSet<>())
                                .createdAt(LocalDateTime.now())
                                .build();
                        return teamRepository.save(newTeam);
                    });
        }

        else {
            team = teamRepository.findByContestIdAndName(contestId, joinRequest.getTeamName())
                    .orElseThrow(() -> new SolvedException(TeamErrorCode.TEAM_NOT_FOUND));

            String password = team.getPassword();
            if(!password.isEmpty() && !password.equals(joinRequest.getTeamPassword())){
                throw new SolvedException(TeamErrorCode.INVALID_ACCESS_TEAM);
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.USER_NOT_FOUND));

        if (team.getMembers().contains(user)) {
            throw new SolvedException(TeamErrorCode.ALREADY_JOINED_TEAM);
        }

        Integer maxTeamSize = team.getContest().getMaxTeamSize();
        if (maxTeamSize != null && team.getMembers().size() >= maxTeamSize) {
            throw new SolvedException(TeamErrorCode.TEAM_FULL);
        }

        team.getMembers().add(user);
        return TeamDto.from(teamRepository.save(team));
    }


    @Transactional(readOnly = true)
    public List<TeamUserScoreDto> getTeamMembers(Long contestId, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.TEAM_NOT_FOUND));

        List<ContestSolved> solvedList = contestSolvedRepository.findByContest_Id(contestId);

        // 해당 팀의 사용자별 점수 집계
        Map<Long, Integer> userScores = new HashMap<>();
        for (ContestSolved solve : solvedList) {
            User user = solve.getSolvedUser();
            if (team.getMembers().contains(user)) {
                int points = solve.getSolvedProblem().getPoints();
                userScores.merge(user.getId(), points, Integer::sum);
            }
        }

        // UserContestDto 변환
        return team.getMembers().stream()
                .map(user -> TeamUserScoreDto.from(user, userScores.getOrDefault(user.getId(), 0)))
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
            dto.setTime(solve.getSolvedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            teamScores.forEach((id, score) -> dto.addScore(teamNames.get(id), score));
            timeSeriesData.add(dto);
        }

        return timeSeriesData;
    }


    public boolean isTeamNameDuplicated(Long contestId, String teamName) {
        return teamRepository.existsByContestIdAndName(contestId, teamName);
    }

    @Transactional
    public Team getOrCreateTeamForContest(Contest contest, User user) {
        Optional<Team> existingTeam = teamRepository.findByContestIdAndMembers_Id(contest.getId(), user.getId());
        if (existingTeam.isPresent()) return existingTeam.get();

        String teamName;
        teamName = user.getNickname();

        Set<User> initialMembers = new HashSet<>();
        initialMembers.add(user);

        Team team = Team.builder()
                .name(teamName)
                .contest(contest)
                .createdAt(LocalDateTime.now())
                .members(initialMembers)
                .build();

        return teamRepository.save(team);
    }
}