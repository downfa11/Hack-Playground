package com.ns.solve.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ns.solve.domain.entity.user.Role;
import com.ns.solve.domain.entity.user.User;
import com.ns.solve.domain.dto.user.ModifyUserDto;
import com.ns.solve.domain.dto.user.RegisterUserDto;
import com.ns.solve.domain.dto.user.UserDto;
import com.ns.solve.domain.dto.user.UserRankDto;
import com.ns.solve.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setNickname("testUser");
        testUser.setAccount("testAccount");
        testUser.setPassword("newPassword");
        testUser.setRole(Role.ROLE_MEMBER);
        testUser.setScore(100L);
        testUser.setCreated(LocalDateTime.now());
        testUser.setLastActived(LocalDateTime.now());
    }

    @Test
    void testCreateUser_Success() {
        RegisterUserDto registerUserDto = new RegisterUserDto("newUser", "newAccount", "password123");

        when(userRepository.existsByNickname(anyString())).thenReturn(false);
        when(userRepository.existsByAccount(anyString())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("crypt");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto createdUser = userService.createUser(registerUserDto);

        assertNotNull(createdUser);
        assertEquals("testUser", createdUser.getNickname());
        assertEquals("testAccount", createdUser.getAccount());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_Failure_Duplicate() {
        RegisterUserDto registerUserDto = new RegisterUserDto("existingUser", "existingAccount", "password123");

        when(userRepository.existsByNickname(anyString())).thenReturn(true);

        UserDto createdUser = userService.createUser(registerUserDto);

        assertNull(createdUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<User> users = userService.getAllUsers();

        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<UserDto> user = userService.getUserById(1L);

        assertTrue(user.isPresent());
        assertEquals("testUser", user.get().getNickname());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUsersSortedByScore() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAllByScoreGreaterThanOrderByScoreDesc(0L, any(PageRequest.class))).thenReturn(userPage);

        Page<UserRankDto> users = userService.getUsersSortedByScore(null,null, 0, 10);

        assertFalse(users.isEmpty());
        assertEquals(1, users.getTotalElements());
        verify(userRepository, times(1)).findAllByScoreGreaterThanOrderByScoreDesc(0L, any(PageRequest.class));
    }

    @Test
    void testGetUserByAccount() {
        when(userRepository.findByAccount("testAccount")).thenReturn(testUser);

        User user = userService.getUserByAccount("testAccount");

        assertNotNull(user);
        assertEquals("testUser", user.getNickname());
        verify(userRepository, times(1)).findByAccount("testAccount");
    }

    @Test
    void testUpdateUser_Success() {
        ModifyUserDto modifyUserDto = new ModifyUserDto("updatedUser", "updatedAccount", "newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByNickname(anyString())).thenReturn(false);
        when(userRepository.existsByAccount(anyString())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("crypt");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto updatedUser = userService.updateUser(1L, 1L, modifyUserDto);

        assertNotNull(updatedUser);
        assertEquals("updatedUser", updatedUser.getNickname());
        assertEquals("updatedAccount", updatedUser.getAccount());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_Failure_NotFound() {
        ModifyUserDto modifyUserDto = new ModifyUserDto("updatedUser", "updatedAccount", "newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserDto updatedUser = userService.updateUser(1L, 1L, modifyUserDto);

        assertNull(updatedUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L, 1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}
