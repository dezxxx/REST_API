package com.dezxxx.rest.service.impl;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.User;
import com.dezxxx.rest.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements Repository<User> {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final Repository<User> userRepository;

    public UserServiceImpl(Repository<User> userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(User user) {
        log.info("Creating user: name={}", user.getName());
        return userRepository.create(user);
    }

    @Override
    public Optional<User> findById(Integer id) {
        log.info("Finding user: id={}", id);
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        log.info("Finding all users");
        return userRepository.findAll();
    }

    @Override
    public User update(User user) {
        userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User", user.getId()));
        log.info("Updating user: id={}", user.getId());
        return userRepository.update(user);
    }

    @Override
    public void delete(Integer id) {
        userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
        log.info("Deleting user: id={}", id);
        userRepository.delete(id);
    }
}
