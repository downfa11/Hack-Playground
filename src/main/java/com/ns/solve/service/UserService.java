package com.ns.solve.service;

import com.ns.solve.domain.entity.Role;
import com.ns.solve.domain.entity.User;
import com.ns.solve.domain.dto.user.ModifyUserDto;
import com.ns.solve.domain.dto.user.RegisterUserDto;
import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.dto.user.UserRankDto;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.service.problem.ProblemService;
import com.ns.solve.utils.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ProblemService problemService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserDto createUser(RegisterUserDto registerUserDto) {
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

        user = userRepository.save(user);
        List<String> solvedProblemTitles = problemService.getSolvedProblemsTitle(user.getId());
        return UserMapper.mapperToUserDto(user, solvedProblemTitles);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    List<String> solvedTitles = problemService.getSolvedProblemsTitle(user.getId());
                    return UserMapper.mapperToUserDto(user, solvedTitles);
                });
    }

    public Page<UserRankDto> getUsersSortedByScore(String type, int page, int size) {
        Page<User> userPage;

        if (type == null || type.isEmpty()) {
            userPage = userRepository.findAllByOrderByScoreDesc(PageRequest.of(page, size));
        } else {
            userPage = userRepository.findUsersByFieldScore(type, PageRequest.of(page, size));
        }

        List<UserRankDto> rankedUsers = IntStream.range(0, userPage.getContent().size())
                .mapToObj(i -> {
                    User user = userPage.getContent().get(i);
                    long rank = page * size + i + 1;
                    long score = type == null || type.isEmpty() ? user.getScore() : user.getFieldScores().getOrDefault(type, 0L);
                    return new UserRankDto(rank, user.getNickname(), score, user.getCreated(), user.getLastActived());
                }).toList();

        return new PageImpl<>(rankedUsers, userPage.getPageable(), userPage.getTotalElements());
    }

    public User getUserByAccount(String account) {
        return userRepository.findByAccount(account);
    }

    public User getUserByNickname(String nickName) {
        return userRepository.findByNickname(nickName);
    }

    public UserDto updateUser(Long currentId, Long updateId, ModifyUserDto modifyUserDto) {
        User currentUser = userRepository.findById(currentId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (!currentId.equals(updateId)) {
            throw new AccessDeniedException("자기 자신만 수정할 수 있습니다.");
        }

        if (!isValidUser(modifyUserDto.nickname(), modifyUserDto.account())) {
            throw new IllegalArgumentException("Invalid nickname or account.");
        }

        currentUser.setNickname(modifyUserDto.nickname());
        currentUser.setAccount(modifyUserDto.account());
        currentUser.setPassword(bCryptPasswordEncoder.encode(modifyUserDto.password()));

        userRepository.save(currentUser);

        List<String> solvedTitles = problemService.getSolvedProblemsTitle(currentUser.getId());
        return UserMapper.mapperToUserDto(currentUser, solvedTitles);
    }

    public void deleteUser(Long currentId, Long deleteId) {
        User user = userRepository.findById(currentId).orElseThrow(() -> new RuntimeException("Not found User"));
        if (currentId.equals(deleteId) || user.isMemberAbove())
            userRepository.deleteById(deleteId);
    }


    private Boolean isValidUser(String nickname, String account) {
        return !(userRepository.existsByNickname(nickname) || userRepository.existsByAccount(account));
    }
}
