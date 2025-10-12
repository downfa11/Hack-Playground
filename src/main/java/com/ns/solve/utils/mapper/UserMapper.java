package com.ns.solve.utils.mapper;

import com.ns.solve.domain.dto.contest.UserContestDto;
import com.ns.solve.domain.dto.user.AffiliationDto;
import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.dto.user.UserFirstBloodDto;
import com.ns.solve.domain.entity.user.Role;
import com.ns.solve.domain.entity.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDto mapperToUserDto(User user, long rank, List<String> solvedTitles, List<UserContestDto> contestDtos) {
        return new UserDto(
                user.getId(),
                user.getNickname(),
                user.getRole(),
                user.getProvider(),
                user.getAccount(),
                rank,
                user.getScore(),
                user.getFieldScores(),
                solvedTitles,
                user.getCreated(),
                user.getLastActived(),
                user.getAffiliations() != null ? user.getAffiliations().stream()
                        .map(AffiliationDto::from)
                        .collect(Collectors.toList()) : List.of(),
                contestDtos
        );
    }

    public static UserDto mapperToUserDto(User user, List<UserContestDto> contestDtos) {
        return new UserDto(
                user.getId(),
                user.getNickname(),
                user.getRole(),
                user.getProvider(),
                user.getAccount(),
                0l,
                user.getScore(),
                user.getFieldScores(),
                null,
                user.getCreated(),
                user.getLastActived(),
                user.getAffiliations() != null ? user.getAffiliations().stream()
                        .map(AffiliationDto::from)
                        .collect(Collectors.toList()) : List.of(),
                contestDtos
        );
    }

    public static UserFirstBloodDto mapperToUserFirstDto(Long userId, String nickname, Role role, LocalDateTime firstBlood){
        return new UserFirstBloodDto(userId, nickname, role, firstBlood);
    }
}
