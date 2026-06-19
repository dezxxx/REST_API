package com.dezxxx.rest.repository.impl;

import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.repository.EventRepository;
import com.dezxxx.rest.util.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class EventRepositoryImpl implements EventRepository {

    private static final Logger log = LoggerFactory.getLogger(EventRepositoryImpl.class);

    @Override
    public Event create(Event event) {
        return TransactionHelper.executeInTransaction(session -> {
            session.persist(event);
            log.info("Event created: id={}", event.getId());
            return event;
        });
    }

    @Override
    public Optional<Event> findById(Integer id) {
        return TransactionHelper.executeInSession(session -> {
            log.debug("findById event: id={}", id);
            return Optional.ofNullable(session.get(Event.class, id));
        });
    }

    @Override
    public List<Event> findAll() {
        return TransactionHelper.executeInSession(session -> {
            log.debug("findAll events");
            return session.createQuery("FROM Event", Event.class).list();
        });
    }

    @Override
    public List<Event> findByUserId(Integer userId) {
        return TransactionHelper.executeInSession(session -> {
            log.debug("findByUserId: userId={}", userId);
            return session.createQuery("FROM Event e WHERE e.user.id = :userId", Event.class)
                    .setParameter("userId", userId)
                    .list();
        });
    }

    @Override
    public Event update(Event event) {
        return TransactionHelper.executeInTransaction(session -> {
            Event updated = session.merge(event);
            log.info("Event updated: id={}", event.getId());
            return updated;
        });
    }

    @Override
    public void delete(Integer id) {
        TransactionHelper.executeInTransaction(session -> {
            Event event = session.get(Event.class, id);
            if (event != null) {
                session.remove(event);
                log.info("Event deleted: id={}", id);
            }
        });
    }
}
