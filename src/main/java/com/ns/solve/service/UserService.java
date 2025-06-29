package com.ns.solve.service;

import com.ns.solve.domain.dto.user.ModifyUserDto;
import com.ns.solve.domain.dto.user.RegisterUserDto;
import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.dto.user.UserRankDto;
import com.ns.solve.domain.entity.Role;
import com.ns.solve.domain.entity.User;
import com.ns.solve.domain.entity.problem.DomainKind;
import com.ns.solve.domain.entity.problem.ProblemType;
import com.ns.solve.domain.entity.problem.WargameKind;
import com.ns.solve.repository.UserRepository;
import com.ns.solve.service.problem.ProblemService;
import com.ns.solve.utils.exception.ErrorCode.UserErrorCode;
import com.ns.solve.utils.exception.SolvedException;
import com.ns.solve.utils.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

        if (!isValidUser(nickname, account)) {
            throw new SolvedException(UserErrorCode.INVALID_NICKNAME_OR_ACCOUNT);
        }

        User user = new User();
        user.setNickname(nickname);
        user.setAccount(account);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setRole(Role.ROLE_MEMBER);
        user.setScore(0L);
        user.setCreated(LocalDateTime.now());
        user.setLastActived(LocalDateTime.now());
        user.setProvider("default");

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

    public Optional<UserDto> getUserDtoByNickname(String nickName) {
        return userRepository.findByNickname(nickName)
                .map(user -> {
                    List<String> solvedTitles = problemService.getSolvedProblemsTitle(user.getId());
                    return UserMapper.mapperToUserDto(user, solvedTitles);
                });
    }

    public User getUserByNickname(String nickName) {
        return userRepository.findByNickname(nickName)
                .orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND));
    }


    public Page<UserRankDto> getUsersSortedByScore(ProblemType type, String kind, int page, int size) {
        Page<User> userPage;
        String fieldKey = type + ":" + kind; // ex) WARGAME:WEBHACKING
        DomainKind domainKind = resolveDomainKind(type, kind);

        if (domainKind == null) {
            userPage = userRepository.findAllByScoreGreaterThanOrderByScoreDesc(0L, PageRequest.of(page, size));
        } else {
            userPage = userRepository.findUsersByFieldScore(fieldKey, PageRequest.of(page, size));
        }

        List<UserRankDto> rankedUsers = IntStream.range(0, userPage.getContent().size())
                .mapToObj(i -> {
                    User user = userPage.getContent().get(i);
                    long rank = page * size + i + 1;
                    long score = (domainKind == null) ? user.getScore() : user.getFieldScores().getOrDefault(fieldKey, 0L);
                    return new UserRankDto(rank, user.getNickname(), score, user.getCreated(), user.getLastActived());
                }).toList();

        return new PageImpl<>(rankedUsers, userPage.getPageable(), userPage.getTotalElements());
    }

    public User getUserByAccount(String account) {
        return userRepository.findByAccount(account);
    }


    public UserDto updateUser(Long currentId, Long updateId, ModifyUserDto modifyUserDto) {
        User currentUser = userRepository.findById(currentId)
                .orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND));

        if (!currentId.equals(updateId)) {
            throw new SolvedException(UserErrorCode.ACCESS_DENIED);
        }

        if (StringUtils.hasText(modifyUserDto.nickname())) {
            if (existsByNickname(modifyUserDto.nickname())) {
                throw new SolvedException(UserErrorCode.INVALID_NICKNAME_OR_ACCOUNT);
            }
            currentUser.setNickname(modifyUserDto.nickname());
        }

        if (StringUtils.hasText(modifyUserDto.account())) {
            if (existsByAccount(modifyUserDto.account())) {
                throw new SolvedException(UserErrorCode.INVALID_NICKNAME_OR_ACCOUNT);
            }
            currentUser.setAccount(modifyUserDto.account());
        }

        if (StringUtils.hasText(modifyUserDto.password())) {
            currentUser.setPassword(bCryptPasswordEncoder.encode(modifyUserDto.password()));
        }

        userRepository.save(currentUser);

        List<String> solvedTitles = problemService.getSolvedProblemsTitle(currentUser.getId());
        return UserMapper.mapperToUserDto(currentUser, solvedTitles);
    }

    public void deleteUser(Long currentId, Long deleteId) {
        User user = userRepository.findById(currentId)
                .orElseThrow(() -> new SolvedException(UserErrorCode.USER_NOT_FOUND));

        if (currentId.equals(deleteId) || user.isMemberAbove()) {
            userRepository.deleteById(deleteId);
        } else {
            throw new SolvedException(UserErrorCode.ACCESS_DENIED);
        }
    }


    private Boolean isValidUser(String nickname, String account) {
        return !(userRepository.existsByNickname(nickname) || userRepository.existsByAccount(account));
    }

    private Boolean isValidUser(Long userId, String nickname, String account) {
        boolean nicknameExists = userRepository.existsByNicknameAndIdNot(nickname, userId);
        boolean accountExists = userRepository.existsByAccountAndIdNot(account, userId);

        return !(nicknameExists || accountExists);
    }

    public Optional<User> findByUserId(Long userId) {
        return userRepository.findById(userId);
    }

    public void updateLastActived(User user) {
        user.setLastActived(LocalDateTime.now());
        userRepository.save(user);
    }

    private DomainKind resolveDomainKind(ProblemType type, String kind) {
        if (kind == null || kind.isBlank()) return null;
        return switch (type) {
            case WARGAME -> WargameKind.valueOf(kind.toUpperCase());
            // etc..
            default -> throw new IllegalArgumentException("Unsupported ProblemType: " + type);
        };
    }

    private boolean existsByNickname(String nickname){
        return userRepository.existsByNickname(nickname);
    }

    private boolean existsByAccount(String account){
        return userRepository.existsByAccount(account);
    }

    public String getNicknameByUserId(String userId) {
        Long id = Long.valueOf(userId);
        return userRepository.findNicknameByUserId(id);
    }
}
