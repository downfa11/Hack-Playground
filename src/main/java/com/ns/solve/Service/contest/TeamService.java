package com.ns.solve.service.contest;

import com.ns.solve.domain.dto.contest.JoinTeamRequest;
import com.ns.solve.domain.dto.contest.TeamCreateDto;
import com.ns.solve.domain.dto.contest.TeamDto;
import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.entity.contest.Contest;
import com.ns.solve.domain.entity.contest.Team;
import com.ns.solve.domain.entity.user.User;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final ContestRepository contestRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeamDto createTeam(Long contestId, TeamCreateDto teamCreateDto) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new SolvedException(TeamErrorCode.CONTEST_NOT_FOUND));

        if (teamRepository.existsByContestAndName(contest, teamCreateDto.getName())) {
            throw new SolvedException(TeamErrorCode.DUPLICATE_TEAM_NAME);
        }

        Team team = Team.builder()
                .name(teamCreateDto.getName())
                .description(teamCreateDto.getDescription())
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

}