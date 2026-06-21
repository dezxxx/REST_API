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

    private static final String FETCH_BY_ID =
            "SELECT e FROM Event e JOIN FETCH e.user JOIN FETCH e.file WHERE e.id = :id";

    @Override
    public Event create(Event event) {
        return TransactionHelper.executeInTransaction(session -> {
            session.persist(event);
            // После persist получаем id, но поля user и file частично null (только id из запроса).
            // Перечитываем из БД чтобы вернуть полный объект.
            Event created = session.createQuery(FETCH_BY_ID, Event.class)
                    .setParameter("id", event.getId())
                    .uniqueResult();
            log.info("Event created: id={}", created.getId());
            return created;
        });
    }

    @Override
    public Optional<Event> findById(Integer id) {
        return TransactionHelper.executeInSession(session -> {
            log.debug("findById event: id={}", id);
            return session.createQuery(FETCH_BY_ID, Event.class)
                    .setParameter("id", id)
                    .uniqueResultOptional();
        });
    }

    @Override
    public List<Event> findAll() {
        return TransactionHelper.executeInSession(session -> {
            log.debug("findAll events");
            return session.createQuery(
                            "SELECT e FROM Event e JOIN FETCH e.user JOIN FETCH e.file",
                            Event.class)
                    .list();
        });
    }

    @Override
    public List<Event> findByUserId(Integer userId) {
        return TransactionHelper.executeInSession(session -> {
            log.debug("findByUserId: userId={}", userId);
            return session.createQuery(
                            "SELECT e FROM Event e JOIN FETCH e.user JOIN FETCH e.file WHERE e.user.id = :userId",
                            Event.class)
                    .setParameter("userId", userId)
                    .list();
        });
    }

    @Override
    public Event update(Event event) {
        return TransactionHelper.executeInTransaction(session -> {
            session.merge(event);
            Event updated = session.createQuery(FETCH_BY_ID, Event.class)
                    .setParameter("id", event.getId())
                    .uniqueResult();
            log.info("Event updated: id={}", updated.getId());
            return updated;
        });
    }

    public boolean existsByUserAndFile(Integer userId, Integer fileId) {
        return TransactionHelper.executeInSession(session -> {
            Long count = session.createQuery(
                            "SELECT COUNT(e) FROM Event e WHERE e.user.id = :userId AND e.file.id = :fileId",
                            Long.class)
                    .setParameter("userId", userId)
                    .setParameter("fileId", fileId)
                    .uniqueResult();
            return count > 0;
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
