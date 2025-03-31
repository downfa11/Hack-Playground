package com.ns.solve.service;

import com.ns.solve.domain.Role;
import com.ns.solve.domain.User;
import com.ns.solve.domain.dto.ModifyUserDto;
import com.ns.solve.domain.dto.RegisterUserDto;
import com.ns.solve.domain.dto.UserDto;
import com.ns.solve.domain.dto.UserRankDto;
import com.ns.solve.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public User createUser(RegisterUserDto registerUserDto) {
        String nickname = registerUserDto.nickname();
        String account = registerUserDto.account();
        String password = registerUserDto.password();

        if (!isValidUser(nickname, account))
            return null;

        User user = new User();
        user.setNickname(nickname);
        user.setAccount(account);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setRole(Role.ROLE_MEMBER);
        user.setScore(0L);
        user.setCreated(LocalDateTime.now());
        user.setLastActived(LocalDateTime.now());

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Page<UserRankDto> getUsersSortedByScore(String type, int page, int size) {
        Page<User> userPage;

        if (type == null || type.isEmpty()) {
            userPage = userRepository.findAllByOrderByScoreDesc(PageRequest.of(page, size));
        } else {
            userPage = userRepository.findUsersByFieldScore(type, PageRequest.of(page, size));
        }

        return userPage.map(user -> new UserRankDto(
                user.getNickname(),
                type == null || type.isEmpty() ? user.getScore() : user.getFieldScores().getOrDefault(type, 0L),
                user.getCreated(),
                user.getLastActived()));
    }


    public User getUserByAccount(String account) {
        return userRepository.findByAccount(account);
    }

    public User updateUser(Long id, ModifyUserDto modifyUserDto) {
        Optional<User> existingUser = userRepository.findById(id);

        if (existingUser.isPresent()) {
            String nickname = modifyUserDto.nickname();
            String account = modifyUserDto.account();

            if (!isValidUser(nickname, account))
                return null;

            User updatedUser = existingUser.get();
            updatedUser.setNickname(nickname);
            updatedUser.setAccount(account);
            updatedUser.setPassword(bCryptPasswordEncoder.encode(modifyUserDto.password()));
            return userRepository.save(updatedUser);
        }
        return null;
    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


    private Boolean isValidUser(String nickname, String account){
        return !(userRepository.existsByNickname(nickname) || userRepository.existsByAccount(account));
    }
}
