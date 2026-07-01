package com.dezxxx.rest.service;

import com.dezxxx.rest.exception.DependencyException;
import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.repository.EventRepository;
import com.dezxxx.rest.repository.FileRepository;
import com.dezxxx.rest.util.FileStorageUtil;
import com.dezxxx.rest.validation.EntityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final EventRepository eventRepository;
    private final String storagePath;

    public FileService(FileRepository fileRepository, EventRepository eventRepository, String storagePath) {
        this.fileRepository = fileRepository;
        this.eventRepository = eventRepository;
        this.storagePath = storagePath;
    }

    public File upload(String originalName, String contentType, long size, InputStream content) throws IOException {
        String savedPath = FileStorageUtil.save(content, originalName, storagePath);
        File file = new File(originalName, savedPath);
        file.setContentType(contentType != null ? contentType : "application/octet-stream");
        file.setSize(size);
        EntityValidator.validate(file);
        log.info("Uploading file: name={}, size={}", originalName, size);
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

    public File update(File patch) {
        File existing = fileRepository.findById(patch.getId())
                .orElseThrow(() -> new EntityNotFoundException("File", patch.getId()));
        if (patch.getName() != null && !patch.getName().isBlank()) {
            existing.setName(patch.getName());
        }
        EntityValidator.validate(existing);
        log.info("Updating file name: id={}", existing.getId());
        return fileRepository.update(existing);
    }

    public void delete(Integer id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("File", id));
        if (eventRepository.existsByFileId(id)) {
            throw new DependencyException("Cannot delete file " + id + ": it has associated events");
        }
        log.info("Deleting file: id={}", id);
        fileRepository.delete(id);
        try {
            FileStorageUtil.delete(file.getFilePath());
        } catch (IOException e) {
            log.warn("Failed to delete file from disk: {}", file.getFilePath(), e);
        }
    }
}
