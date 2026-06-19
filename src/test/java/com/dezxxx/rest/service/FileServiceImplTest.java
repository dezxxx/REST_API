package com.dezxxx.rest.service;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.repository.Repository;
import com.dezxxx.rest.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private Repository<File> fileRepository;

    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl(fileRepository);
    }

    @Test
    void create_shouldReturnCreatedFile() {
        File file = new File("report.pdf", "/uploads/report.pdf");
        when(fileRepository.create(file)).thenReturn(file);

        File result = fileService.create(file);

        assertEquals(file, result);
        verify(fileRepository).create(file);
    }

    @Test
    void findById_shouldReturnFile_whenExists() {
        File file = new File("report.pdf", "/uploads/report.pdf");
        when(fileRepository.findById(1)).thenReturn(Optional.of(file));

        Optional<File> result = fileService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(file, result.get());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(fileRepository.findById(99)).thenReturn(Optional.empty());

        Optional<File> result = fileService.findById(99);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllFiles() {
        List<File> files = List.of(
                new File("report.pdf", "/uploads/report.pdf"),
                new File("photo.jpg", "/uploads/photo.jpg")
        );
        when(fileRepository.findAll()).thenReturn(files);

        List<File> result = fileService.findAll();

        assertEquals(2, result.size());
        verify(fileRepository).findAll();
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenNotExists() {
        File file = new File("report.pdf", "/uploads/report.pdf");
        file.setId(99);
        when(fileRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fileService.update(file));
        verify(fileRepository, never()).update(any());
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotExists() {
        when(fileRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fileService.delete(99));
        verify(fileRepository, never()).delete(any());
    }
}
