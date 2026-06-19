package com.dezxxx.rest.service.impl;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.repository.Repository;
import com.dezxxx.rest.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private final Repository<File> fileRepository;

    public FileServiceImpl(Repository<File> fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public File create(File file) {
        log.info("Creating file: name={}", file.getName());
        return fileRepository.create(file);
    }

    @Override
    public Optional<File> findById(Integer id) {
        log.info("Finding file: id={}", id);
        return fileRepository.findById(id);
    }

    @Override
    public List<File> findAll() {
        log.info("Finding all files");
        return fileRepository.findAll();
    }

    @Override
    public File update(File file) {
        fileRepository.findById(file.getId())
                .orElseThrow(() -> new EntityNotFoundException("File", file.getId()));
        log.info("Updating file: id={}", file.getId());
        return fileRepository.update(file);
    }

    @Override
    public void delete(Integer id) {
        fileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("File", id));
        log.info("Deleting file: id={}", id);
        fileRepository.delete(id);
    }
}
