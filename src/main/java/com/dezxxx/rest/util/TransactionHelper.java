package com.dezxxx.rest.util;

import com.dezxxx.rest.exception.RepositoryException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

public final class TransactionHelper {

    private static final Logger log = LoggerFactory.getLogger(TransactionHelper.class);

    private TransactionHelper() {
    }

    public static <T> T executeInTransaction(Function<Session, T> action) {
        Session session = HibernateUtil.openSession();
        Transaction tx = HibernateUtil.beginTransaction(session);
        try {
            T result = action.apply(session);
            HibernateUtil.commitAndClose(session, tx);
            return result;
        } catch (Exception e) {
            HibernateUtil.rollbackAndClose(session, tx);
            log.error("Transaction failed", e);
            throw new RepositoryException("Transaction failed: " + e.getMessage(), e);
        }
    }

    public static void executeInTransaction(Consumer<Session> action) {
        Session session = HibernateUtil.openSession();
        Transaction tx = HibernateUtil.beginTransaction(session);
        try {
            action.accept(session);
            HibernateUtil.commitAndClose(session, tx);
        } catch (Exception e) {
            HibernateUtil.rollbackAndClose(session, tx);
            log.error("Transaction failed", e);
            throw new RepositoryException("Transaction failed: " + e.getMessage(), e);
        }
    }

    public static <T> T executeInSession(Function<Session, T> action) {
        try (Session session = HibernateUtil.openSession()) {
            return action.apply(session);
        }
    }
}