package com.dezxxx.rest.service;

import com.dezxxx.rest.exception.DuplicateEntityException;
import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.repository.EventRepository;
import com.dezxxx.rest.repository.FileRepository;
import com.dezxxx.rest.repository.UserRepository;
import com.dezxxx.rest.validation.EntityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        FileRepository fileRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    public Event create(Event event) {
        EntityValidator.validate(event);
        Integer userId = event.getUser().getId();
        Integer fileId = event.getFile().getId();
        requireReferencesExist(userId, fileId);
        if (eventRepository.existsByUserAndFile(userId, fileId)) {
            throw new DuplicateEntityException("Event already exists for user " + userId + " and file " + fileId);
        }
        log.info("Creating event: userId={}, fileId={}", userId, fileId);
        return eventRepository.create(event);
    }

    private void requireReferencesExist(Integer userId, Integer fileId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File", fileId));
    }

    public Optional<Event> findById(Integer id) {
        log.info("Finding event: id={}", id);
        return eventRepository.findById(id);
    }

    public List<Event> findAll() {
        log.info("Finding all events");
        return eventRepository.findAll();
    }

    public List<Event> findByUserId(Integer userId) {
        log.info("Finding events by userId={}", userId);
        return eventRepository.findByUserId(userId);
    }

    public Event update(Event event) {
        eventRepository.findById(event.getId())
                .orElseThrow(() -> new EntityNotFoundException("Event", event.getId()));
        EntityValidator.validate(event);
        requireReferencesExist(event.getUser().getId(), event.getFile().getId());
        log.info("Updating event: id={}", event.getId());
        return eventRepository.update(event);
    }

    public void delete(Integer id) {
        eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event", id));
        log.info("Deleting event: id={}", id);
        eventRepository.delete(id);
    }
}
