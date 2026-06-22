package com.dezxxx.rest.service;

import com.dezxxx.rest.exception.DependencyException;
import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.repository.EventRepository;
import com.dezxxx.rest.repository.FileRepository;
import com.dezxxx.rest.validation.EntityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final EventRepository eventRepository;

    public FileService(FileRepository fileRepository, EventRepository eventRepository) {
        this.fileRepository = fileRepository;
        this.eventRepository = eventRepository;
    }

    public File create(File file) {
        EntityValidator.validate(file);
        log.info("Creating file: name={}", file.getName());
        return fileRepository.create(file);
    }

    public Optional<File> findById(Integer id) {
        log.info("Finding file: id={}", id);
        return fileRepository.findById(id);
    }

    public List<File> findAll() {
        log.info("Finding all files");
        return fileRepository.findAll();
    }

    public File update(File file) {
        fileRepository.findById(file.getId())
                .orElseThrow(() -> new EntityNotFoundException("File", file.getId()));
        EntityValidator.validate(file);
        log.info("Updating file: id={}", file.getId());
        return fileRepository.update(file);
    }

    public void delete(Integer id) {
        fileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("File", id));
        if (eventRepository.existsByFileId(id)) {
            throw new DependencyException("Cannot delete file " + id + ": it has associated events");
        }
        log.info("Deleting file: id={}", id);
        fileRepository.delete(id);
    }
}