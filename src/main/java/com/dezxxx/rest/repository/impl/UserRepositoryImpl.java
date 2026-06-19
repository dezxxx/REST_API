package com.dezxxx.rest.repository.impl;

import com.dezxxx.rest.model.User;
import com.dezxxx.rest.repository.Repository;
import com.dezxxx.rest.util.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl implements Repository<User> {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);

    @Override
    public User create(User user) {
        return TransactionHelper.executeInTransaction(session -> {
            session.persist(user);
            log.info("User created: id={}", user.getId());
            return user;
        });
    }

    @Override
    public Optional<User> findById(Integer id) {
        return TransactionHelper.executeInSession(session -> {
            log.debug("findById user: id={}", id);
            return session.createQuery(
                            "SELECT u FROM User u LEFT JOIN FETCH u.events ev LEFT JOIN FETCH ev.file WHERE u.id = :id",
                            User.class)
                    .setParameter("id", id)
                    .uniqueResultOptional();
        });
    }

    @Override
    public List<User> findAll() {
        return TransactionHelper.executeInSession(session -> {
            log.debug("findAll users");
            return session.createQuery(
                            "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.events ev LEFT JOIN FETCH ev.file",
                            User.class)
                    .list();
        });
    }

    @Override
    public User update(User user) {
        return TransactionHelper.executeInTransaction(session -> {
            User updated = session.merge(user);
            log.info("User updated: id={}", user.getId());
            return updated;
        });
    }

    @Override
    public void delete(Integer id) {
        TransactionHelper.executeInTransaction(session -> {
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                log.info("User deleted: id={}", id);
            }
        });
    }
}
