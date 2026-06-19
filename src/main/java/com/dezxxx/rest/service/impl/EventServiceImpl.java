package com.dezxxx.rest.service.impl;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.repository.EventRepository;
import com.dezxxx.rest.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event create(Event event) {
        log.info("Creating event");
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
