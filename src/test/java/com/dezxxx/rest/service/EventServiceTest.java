package com.dezxxx.rest.service;

import com.dezxxx.rest.exception.DuplicateEntityException;
import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.exception.ValidationException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.model.User;
import com.dezxxx.rest.repository.EventRepository;
import com.dezxxx.rest.repository.FileRepository;
import com.dezxxx.rest.repository.UserRepository;
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
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileRepository fileRepository;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, userRepository, fileRepository);
    }

    @Test
    void create_shouldReturnCreatedEvent() {
        User user = new User("Ivan");
        user.setId(1);
        File file = new File("report.pdf", "/uploads/report.pdf");
        file.setId(2);
        Event event = new Event(user, file);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(fileRepository.findById(2)).thenReturn(Optional.of(file));
        when(eventRepository.create(event)).thenReturn(event);

        Event result = eventService.create(event);

        assertEquals(event, result);
        verify(userRepository).findById(1);
        verify(fileRepository).findById(2);
        verify(eventRepository).create(event);
    }

    @Test
    void create_shouldThrowValidationException_whenUserIsNull() {
        Event event = new Event(null, new File("report.pdf", "/uploads/report.pdf"));

        assertThrows(ValidationException.class, () -> eventService.create(event));
        verify(eventRepository, never()).create(any());
    }

    @Test
    void create_shouldThrowValidationException_whenUserIdIsNull() {
        Event event = new Event(new User("Ivan"), new File("report.pdf", "/uploads/report.pdf"));

        assertThrows(ValidationException.class, () -> eventService.create(event));
        verify(eventRepository, never()).create(any());
    }

    @Test
    void create_shouldThrowEntityNotFoundException_whenUserNotExists() {
        User user = new User("Ivan");
        user.setId(99);
        File file = new File("report.pdf", "/uploads/report.pdf");
        file.setId(2);
        Event event = new Event(user, file);

        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> eventService.create(event));
        verify(eventRepository, never()).create(any());
    }

    @Test
    void create_shouldThrowEntityNotFoundException_whenFileNotExists() {
        User user = new User("Ivan");
        user.setId(1);
        File file = new File("report.pdf", "/uploads/report.pdf");
        file.setId(99);
        Event event = new Event(user, file);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(fileRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> eventService.create(event));
        verify(eventRepository, never()).create(any());
    }

    @Test
    void create_shouldThrowDuplicateEntityException_whenAlreadyExists() {
        User user = new User("Ivan");
        user.setId(1);
        File file = new File("report.pdf", "/uploads/report.pdf");
        file.setId(2);
        Event event = new Event(user, file);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(fileRepository.findById(2)).thenReturn(Optional.of(file));
        when(eventRepository.existsByUserAndFile(1, 2)).thenReturn(true);

        assertThrows(DuplicateEntityException.class, () -> eventService.create(event));
        verify(eventRepository, never()).create(any());
    }

    @Test
    void findById_shouldReturnEvent_whenExists() {
        Event event = new Event();
        event.setId(1);
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        Optional<Event> result = eventService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(event, result.get());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(eventRepository.findById(99)).thenReturn(Optional.empty());

        Optional<Event> result = eventService.findById(99);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllEvents() {
        List<Event> events = List.of(new Event(), new Event());
        when(eventRepository.findAll()).thenReturn(events);

        List<Event> result = eventService.findAll();

        assertEquals(2, result.size());
        verify(eventRepository).findAll();
    }

    @Test
    void findByUserId_shouldReturnUserEvents() {
        List<Event> events = List.of(new Event());
        when(eventRepository.findByUserId(1)).thenReturn(events);

        List<Event> result = eventService.findByUserId(1);

        assertEquals(1, result.size());
        verify(eventRepository).findByUserId(1);
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenNotExists() {
        Event event = new Event();
        event.setId(99);
        when(eventRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> eventService.update(event));
        verify(eventRepository, never()).update(any());
    }

    @Test
    void delete_shouldCallRepository_whenExists() {
        Event event = new Event();
        event.setId(1);
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        eventService.delete(1);

        verify(eventRepository).delete(1);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotExists() {
        when(eventRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> eventService.delete(99));
        verify(eventRepository, never()).delete(any());
    }
}