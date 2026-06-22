package com.dezxxx.rest.repository.impl;

import com.dezxxx.rest.model.File;
import com.dezxxx.rest.repository.FileRepository;
import com.dezxxx.rest.util.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class FileRepositoryImpl implements FileRepository {

    private static final Logger log = LoggerFactory.getLogger(FileRepositoryImpl.class);

    @Override
    public File create(File file) {
        return TransactionHelper.executeInTransaction(session -> {
            session.persist(file);
            log.info("File created: id={}", file.getId());
            return file;
        });
    }

    @Override
    public Optional<File> findById(Integer id) {
        return TransactionHelper.executeInSession(session -> {
            log.debug("findById file: id={}", id);
            return Optional.ofNullable(session.get(File.class, id));
        });
    }

    @Override
    public List<File> findAll() {
        return TransactionHelper.executeInSession(session -> {
            log.debug("findAll files");
            return session.createQuery("FROM File", File.class).list();
        });
    }

    @Override
    public File update(File file) {
        return TransactionHelper.executeInTransaction(session -> {
            session.merge(file);
            File updated = session.get(File.class, file.getId());
            log.info("File updated: id={}", updated.getId());
            return updated;
        });
    }

    @Override
    public void delete(Integer id) {
        TransactionHelper.executeInTransaction(session -> {
            File file = session.get(File.class, id);
            if (file != null) {
                session.remove(file);
                log.info("File deleted: id={}", id);
            }
        });
    }
}
