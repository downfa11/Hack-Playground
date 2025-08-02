package com.ns.solve.utils.mapper;

import com.ns.solve.domain.dto.user.UserFirstBloodDto;
import com.ns.solve.domain.entity.user.Role;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.domain.dto.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;

public class UserMapper {

    public static UserDto mapperToUserDto(User user, List<String> solvedTitles) {
        return new UserDto(
                user.getId(),
                user.getNickname(),
                user.getRole(),
                user.getProvider(),
                user.getAccount(),
                user.getScore(),
                user.getFieldScores(),
                solvedTitles,
                user.getCreated(),
                user.getLastActived()
        );
    }

    public static UserDto mapperToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getNickname(),
                user.getRole(),
                user.getProvider(),
                user.getAccount(),
                user.getScore(),
                user.getFieldScores(),
                null,
                user.getCreated(),
                user.getLastActived()
        );
    }

    public static UserFirstBloodDto mapperToUserFirstDto(Long userId, String nickname, Role role, LocalDateTime firstBlood){
        return new UserFirstBloodDto(userId, nickname, role, firstBlood);
    }
}
