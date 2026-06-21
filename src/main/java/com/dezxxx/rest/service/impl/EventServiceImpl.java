package com.dezxxx.rest.service.impl;

import com.dezxxx.rest.exception.DuplicateEntityException;
import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.model.User;
import com.dezxxx.rest.repository.EventRepository;
import com.dezxxx.rest.repository.Repository;
import com.dezxxx.rest.repository.impl.EventRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class EventServiceImpl implements EventRepository {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepositoryImpl eventRepository;
    private final Repository<User> userRepository;
    private final Repository<File> fileRepository;

    public EventServiceImpl(EventRepositoryImpl eventRepository,
                            Repository<User> userRepository,
                            Repository<File> fileRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    @Override
    public Event create(Event event) {
        Integer userId = event.getUser().getId();
        Integer fileId = event.getFile().getId();
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File", fileId));
        if (eventRepository.existsByUserAndFile(userId, fileId)) {
            throw new DuplicateEntityException("Event already exists for user " + userId + " and file " + fileId);
        }
        log.info("Creating event: userId={}, fileId={}", userId, fileId);
        return eventRepository.create(event);
    }

    @Override
    public Optional<Event> findById(Integer id) {
        log.info("Finding event: id={}", id);
        return eventRepository.findById(id);
    }

    @Override
    public List<Event> findAll() {
        log.info("Finding all events");
        return eventRepository.findAll();
    }

    @Override
    public List<Event> findByUserId(Integer userId) {
        log.info("Finding events by userId={}", userId);
        return eventRepository.findByUserId(userId);
    }

    @Override
    public Event update(Event event) {
        eventRepository.findById(event.getId())
                .orElseThrow(() -> new EntityNotFoundException("Event", event.getId()));
        log.info("Updating event: id={}", event.getId());
        return eventRepository.update(event);
    }

    @Override
    public void delete(Integer id) {
        eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event", id));
        log.info("Deleting event: id={}", id);
        eventRepository.delete(id);
    }
}
