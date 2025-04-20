package com.ns.solve.utils.mapper;

import com.ns.solve.domain.entity.User;
import com.ns.solve.domain.dto.user.UserDto;

import java.util.List;

public class UserMapper {

    public static UserDto mapperToUserDto(User user, List<String> solvedTitles) {
        return new UserDto(
                user.getId(),
                user.getNickname(),
                user.getRole(),
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
                user.getAccount(),
                user.getScore(),
                user.getFieldScores(),
                null,
                user.getCreated(),
                user.getLastActived()
        );
    }
}
