package com.dezxxx.rest.service;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.exception.ValidationException;
import com.dezxxx.rest.model.User;
import com.dezxxx.rest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void create_shouldReturnCreatedUser() {
        User user = new User("Ivan");
        when(userRepository.create(user)).thenReturn(user);

        User result = userService.create(user);

        assertEquals(user, result);
        verify(userRepository).create(user);
    }

    @Test
    void create_shouldThrowValidationException_whenNameIsBlank() {
        User user = new User("   ");

        assertThrows(ValidationException.class, () -> userService.create(user));
        verify(userRepository, never()).create(any());
    }

    @Test
    void create_shouldThrowValidationException_whenNameIsNull() {
        User user = new User();

        assertThrows(ValidationException.class, () -> userService.create(user));
        verify(userRepository, never()).create(any());
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        User user = new User("Ivan");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(99);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        List<User> users = List.of(new User("Ivan"), new User("Maria"));
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void update_shouldReturnUpdatedUser_whenExists() {
        User user = new User("Ivan");
        user.setId(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.update(user)).thenReturn(user);

        User result = userService.update(user);

        assertEquals(user, result);
        verify(userRepository).update(user);
    }

    @Test
    void update_shouldThrowValidationException_whenNameIsBlank() {
        User user = new User("   ");
        user.setId(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> userService.update(user));
        verify(userRepository, never()).update(any());
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenNotExists() {
        User user = new User("Ivan");
        user.setId(99);
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.update(user));
        verify(userRepository, never()).update(any());
    }

    @Test
    void delete_shouldCallRepository_whenExists() {
        User user = new User("Ivan");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.delete(1);

        verify(userRepository).delete(1);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotExists() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.delete(99));
        verify(userRepository, never()).delete(any());
    }
}