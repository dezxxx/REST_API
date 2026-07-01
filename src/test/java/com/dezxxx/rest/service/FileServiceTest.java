package com.dezxxx.rest.service;

import com.dezxxx.rest.exception.DependencyException;
import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.exception.ValidationException;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.repository.EventRepository;
import com.dezxxx.rest.repository.FileRepository;
import com.dezxxx.rest.util.FileStorageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private EventRepository eventRepository;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(fileRepository, eventRepository, "/tmp/storage");
    }

    @Test
    void upload_shouldSaveFileAndReturnMetadata() throws IOException {
        InputStream content = new ByteArrayInputStream("data".getBytes());
        File saved = new File("doc.txt", "/tmp/storage/uuid_doc.txt");
        saved.setId(1);

        try (MockedStatic<FileStorageUtil> fsUtil = mockStatic(FileStorageUtil.class)) {
            fsUtil.when(() -> FileStorageUtil.save(any(), eq("doc.txt"), anyString()))
                    .thenReturn("/tmp/storage/uuid_doc.txt");
            when(fileRepository.create(any())).thenReturn(saved);

            File result = fileService.upload("doc.txt", "text/plain", 4L, content);

            assertEquals(saved, result);
            verify(fileRepository).create(any());
        }
    }

    @Test
    void upload_shouldThrowValidationException_whenNameIsBlank() {
        InputStream content = new ByteArrayInputStream("data".getBytes());

        try (MockedStatic<FileStorageUtil> fsUtil = mockStatic(FileStorageUtil.class)) {
            fsUtil.when(() -> FileStorageUtil.save(any(), anyString(), anyString()))
                    .thenReturn("/tmp/storage/uuid_");

            assertThrows(ValidationException.class,
                    () -> fileService.upload("   ", "text/plain", 4L, content));
            verify(fileRepository, never()).create(any());
        }
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
    void update_shouldReturnUpdatedFile_whenExists() {
        File file = new File("report.pdf", "/uploads/report.pdf");
        file.setId(1);
        when(fileRepository.findById(1)).thenReturn(Optional.of(file));
        when(fileRepository.update(file)).thenReturn(file);

        File result = fileService.update(file);

        assertEquals(file, result);
        verify(fileRepository).update(file);
    }

    @Test
    void update_shouldThrowValidationException_whenNameIsBlank() {
        File file = new File("   ", "/uploads/report.pdf");
        file.setId(1);
        when(fileRepository.findById(1)).thenReturn(Optional.of(file));

        assertThrows(ValidationException.class, () -> fileService.update(file));
        verify(fileRepository, never()).update(any());
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
    void delete_shouldCallRepository_whenExists() {
        File file = new File("report.pdf", "/uploads/report.pdf");
        when(fileRepository.findById(1)).thenReturn(Optional.of(file));
        when(eventRepository.existsByFileId(1)).thenReturn(false);

        fileService.delete(1);

        verify(fileRepository).delete(1);
    }

    @Test
    void delete_shouldThrowDependencyException_whenEventsExist() {
        File file = new File("report.pdf", "/uploads/report.pdf");
        when(fileRepository.findById(1)).thenReturn(Optional.of(file));
        when(eventRepository.existsByFileId(1)).thenReturn(true);

        assertThrows(DependencyException.class, () -> fileService.delete(1));
        verify(fileRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotExists() {
        when(fileRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fileService.delete(99));
        verify(fileRepository, never()).delete(any());
    }
}
