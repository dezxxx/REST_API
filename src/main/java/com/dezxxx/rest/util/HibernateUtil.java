package com.dezxxx.rest.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HibernateUtil {

    private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Exception e) {
            log.error("Failed to create SessionFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Session openSession() {
        return SESSION_FACTORY.openSession();
    }

    public static Transaction beginTransaction(Session session) {
        Transaction tx = session.beginTransaction();
        log.debug("Transaction started");
        return tx;
    }

    public static void commitAndClose(Session session, Transaction tx) {
        try {
            tx.commit();
            log.debug("Transaction committed");
        } catch (Exception e) {
            tx.rollback();
            log.error("Transaction rolled back", e);
            throw e;
        } finally {
            session.close();
        }
    }

    public static void rollbackAndClose(Session session, Transaction tx) {
        try {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                log.error("Transaction rolled back");
            }
        } finally {
            session.close();
        }
    }

    public static void shutdown() {
        SESSION_FACTORY.close();
        log.info("SessionFactory closed");
    }
}