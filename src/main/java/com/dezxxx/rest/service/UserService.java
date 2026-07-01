package com.dezxxx.rest.service;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.User;
import com.dezxxx.rest.repository.UserRepository;
import com.dezxxx.rest.validation.EntityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(User user) {
        EntityValidator.validateForCreate(user);
        log.info("Creating user: name={}", user.getName());
        return userRepository.create(user);
    }

    public Optional<User> findById(Integer id) {
        log.info("Finding user: id={}", id);
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        log.info("Finding all users");
        return userRepository.findAll();
    }

    public User update(User patch) {
        User existing = userRepository.findById(patch.getId())
                .orElseThrow(() -> new EntityNotFoundException("User", patch.getId()));
        if (patch.getName() != null && !patch.getName().isBlank()) {
            existing.setName(patch.getName());
        }
        EntityValidator.validateForUpdate(existing);
        log.info("Updating user: id={}", existing.getId());
        return userRepository.update(existing);
    }

    public void delete(Integer id) {
        userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
        log.info("Deleting user: id={}", id);
        userRepository.delete(id);
    }
}
